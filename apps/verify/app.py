import json
import os
import time
from typing import Any, Dict
import logging
import queue
from urllib.parse import urlparse
from dataclasses import dataclass
import pika
import threading
import boto3

from pika.channel import Channel
from dotenv import load_dotenv
from flask import Flask, jsonify
from openai import APIConnectionError, InternalServerError, OpenAI

from opentelemetry import trace
from opentelemetry.instrumentation.system_metrics import SystemMetricsInstrumentor
from opentelemetry.sdk.trace import SpanProcessor, Span

from flask_cors import CORS

# OpenTelemetry System Metrics Exporting
system_instrumentor = SystemMetricsInstrumentor()
system_instrumentor.instrument()

# OpenTelemetry span propagation across threads
tracer = tracer = trace.get_tracer(__name__)

logging.basicConfig(level=logging.INFO)
pika_logger = logging.getLogger("pika")
pika_logger.setLevel(logging.INFO)

# Obtain environment variables
load_dotenv()
OPENAI_KEY = os.getenv("OPENAI_KEY")
VERIFIER_ASSISTANT_ID = os.getenv("VERIFIER_ASSISTANT_ID")
VECTOR_STORE_ID = os.getenv("VECTOR_STORE_ID")

# AMQP Pub/Sub
protocol = os.getenv("RABBITMQ_PROTOCOL")
host = os.getenv('RABBITMQ_HOST')
username = os.getenv('RABBITMQ_USERNAME')
password = os.getenv('RABBITMQ_PASSWORD')
url = f"{protocol}://{username}:{password}@{host}"
exchange = os.getenv("RABBITMQ_EXCHANGE")
amqp_queue = os.getenv("RABBITMQ_QUEUE")

# Specify global variables
API_HEADER = '/api/v1'
UPLOAD_FOLDER = os.getcwd() + '/uploads'
ALLOWED_EXTENSIONS = {'txt', 'pdf', 'json', 'xlsx',
                      'doc', 'docx', 'ppt', 'pptx'}
MAX_COMPLETION_TOKENS = 100
MAX_TRIES = 3

# Specify common errors
fileNotUploadedError = "No file uploaded error"
invalidFileFormat = f"Filename cannot be empty or file extension is not allowed. Allowed extensions: {ALLOWED_EXTENSIONS}"

# Create uploads folder if not present
if not os.path.exists(UPLOAD_FOLDER):
    os.mkdir(UPLOAD_FOLDER)

# Verify credentials with OpenAI
client = OpenAI(api_key=OPENAI_KEY)

# Specifies app to run for flask
app = Flask(__name__)
CORS(app)
queue = queue.Queue()
s3 = boto3.client('s3')

# Set up RabbitMq
parameters = pika.URLParameters(url)
parameters.socket_timeout = None
parameters.heartbeat = 200


# Specify Listing Status Object
@dataclass
class ListingStatus():
    _id: str
    status: str
    url: str
    price: int = None
    categoryCode: str = None

    def to_json(self) -> Dict[str, Any]:
        return {
            "_id": self._id,
            "status": self.status,
            "price": self.price,
            "categoryCode": self.categoryCode,
            "url": self.url
        }


@app.route("/health")
def health_check():
    logging.info("health check")
    return jsonify({"message": "Verify Service is healthy"}), 200


def upload_file_to_openai(path: str):
    # Upload file onto OpenAI
    file_to_upload = open(path, "rb")
    # If file is not found, return from function
    if file_to_upload is None:
        logging.info("File not found in local directory")
        return None
    file = client.files.create(file=file_to_upload, purpose="assistants")
    vector_store_file = client.beta.vector_stores.files.create(file_id=file.id,
                                                               vector_store_id=VECTOR_STORE_ID)
    time.sleep(0.5)
    logging.info(f"Vector Store File Run Status: {vector_store_file.status}")
    while vector_store_file.status != "completed":
        # Handle failed file upload by reuploading the file
        if vector_store_file.status == "cancelled" or vector_store_file.status == "failed":
            logging.warning("Vector Store File upload failed. Retrying...")
            vector_store_file = client.beta.vector_stores.files.create(file_id=file.id, vector_store_id=VECTOR_STORE_ID)
        time.sleep(0.5)
        # Re-retrieve file status
        vector_store_file = client.beta.vector_stores.files.retrieve(file_id=file.id, vector_store_id=VECTOR_STORE_ID)
        logging.debug(f"Vector Store File Run Status: {vector_store_file.status}")
    # Close the file_to_upload
    file_to_upload.close()
    return file


def create_run(thread_id: str):
    try:
        # Create run
        run = client.beta.threads.runs.create(thread_id=thread_id, assistant_id=VERIFIER_ASSISTANT_ID, max_completion_tokens=MAX_COMPLETION_TOKENS)
        time.sleep(0.5)
        logging.info(f"Verifier Run Status: {run.status}")
        # Ensure that the run has been completed before moving on
        while run.status != "completed":
            # Handle incomplete run status
            if run.status == "incomplete":
                logging.info(f"Verifier Run Status: Fixing {run.status} run status...")
                # Add new message to run again
                client.beta.threads.messages.create(
                    thread_id=thread_id,
                    role="user",
                    content='''Please ensure that response is in the """JSON format""" of {"verified": <verified>}.'''
                )
                # Start a new run
                run = client.beta.threads.runs.create(thread_id=thread_id, assistant_id=VERIFIER_ASSISTANT_ID, max_completion_tokens=MAX_COMPLETION_TOKENS)
            elif run.status == "failed":
                logging.warning(f"Verifier Run Status: '{run.last_error.code}'-'{run.last_error.message}'. Fixing {run.status} run status...")
                # Identify whats the error
                if (run.last_error.code == "rate_limit_exceeded"):
                    logging.warning(f"Verifier Run Status: {run.last_error.code}, Sleeping for 1min before trying again...")
                    time.sleep(60)
                # Start a new run
                run = client.beta.threads.runs.create(thread_id=thread_id,
                                                      assistant_id=VERIFIER_ASSISTANT_ID,
                                                      max_completion_tokens=MAX_COMPLETION_TOKENS)
            elif run.status == "expired":
                logging.warning(f"Verifier Run Status: Fixing {run.status} run status...")
                # Start a new run
                run = client.beta.threads.runs.create(thread_id=thread_id,
                                                      assistant_id=VERIFIER_ASSISTANT_ID,
                                                      max_completion_tokens=MAX_COMPLETION_TOKENS)
            else:
                run = client.beta.threads.runs.retrieve(thread_id=thread_id,
                                                        run_id=run.id)
        time.sleep(0.5)
        # Print run status
        logging.info(f"Verifier Run Status: {run.status}")
        # Returns from function once run has completed
        if run.status == "completed":
            return
        else:
            raise ValueError("Verifier could not complete")
    except InternalServerError:
        logging.warning("OpenAI has experienced some internal server error. Retrying...")
    except APIConnectionError:
        logging.warning("OpenAI has experienced difficulties connecting to API. Retrying...")


def run_assistant(file):
    if file is None:
        raise ValueError(fileNotUploadedError)
    # Create messages
    messages = [{"role": "user",
                 "content": f'''Given the file id """{file.id}""", verify if the file is appropriate and return response in """JSON""" format of {{"verified":<verified>}}.'''}]
    # Create thread to run
    thread = client.beta.threads.create(messages=messages)
    # Create run
    create_run(thread_id=thread.id)
    # Retrieve the latest message
    messages = client.beta.threads.messages.list(thread_id=thread.id)
    curr = 1
    while curr < MAX_TRIES:
        # Obtain response from messages
        response = messages.data[0]
        new_message = response.content[0].text.value
        # Check if there is the common JSON parsing error
        if new_message.startswith("```json"):
            logging.debug("Debug: Remove ```json")
            new_message = new_message[7:]
        if new_message.endswith("```"):
            logging.debug("Debug: Remove ```")
            new_message = new_message[:len(new_message) - 3]
        logging.info(f"Verifier message: {new_message}")
        # Jsonify output
        try:
            # Attempt to jsonify output
            json_response: dict = json.loads(new_message)
            if "verified" not in json_response.keys() or json_response['verified'] is None:
                logging.warning("Verified field not found in JSON output.")
                client.beta.threads.messages.create(thread_id=thread.id,
                                                    role="user",
                                                    content='''Please follow JSON format in system instructions.''')
                create_run(thread_id=thread.id)
            elif json_response['verified'] not in ["true", "false"]:
                logging.warning("Verified field has no desired outputs.")
                client.beta.threads.messages.create(thread_id=thread.id,
                                                    role="user",
                                                    content='''Verified field in JSON should only be "true" or "false".''')
                create_run(thread_id=thread.id)
            else:
                return json_response

        except json.JSONDecodeError:
            logging.warning('''Error found when parsing response not in JSON format. Retrying...''')
        curr += 1
    # Reaches here if num tries is more than curr tries
    logging.error('Max Tries has been reached.')


def delete_file_from_local(path: str):
    if os.path.exists(path):
        os.remove(path)
    else:
        logging.info("The file does not exist")


def delete_file_from_vector_store(file):
    client.files.delete(file_id=file.id)


def on_message(ch: Channel, method, properties, body: bytes) -> None:
    ch.basic_ack(delivery_tag=method.delivery_tag)
    logging.info('Acknowledged message')
    with tracer.start_as_current_span("message_processing") as span:
        span.set_attribute("message_id", properties.correlation_id)
        data = json.loads(body)
        listing: ListingStatus = ListingStatus(**data)
        logging.info('Listing, %s', listing)
        app.logger.info(listing)

        # time.sleep(90)
        parsed_url = urlparse(listing.url)
        bucket = parsed_url.netloc.split('.')[0]
        client_id, key = parsed_url.path.lstrip('/').split("/")
        print("Client id:", client_id, ", Key:", key)

        if not os.path.exists(f"{os.getcwd()}/tmp"):
            os.mkdir(f"{os.getcwd()}/tmp")
            if not os.path.exists(f"{os.getcwd()}/tmp/{client_id}"):
                os.mkdir(f"{os.getcwd()}/tmp/{client_id}")

        local_path = os.getcwd() + '\\tmp\\' + key
        print('Key:', key)
        print('LocalPath:', local_path)

        try:
            s3.download_file(bucket, client_id + "/" + key, local_path)
            file = upload_file_to_openai(local_path)
            json_response = run_assistant(file=file)
            delete_file_from_vector_store(file=file)
            verified = json_response['verified']

            if verified:
                listing.status = "Verified"
            else:
                listing.status = "Rejected"
            queue.put(listing)
            # ch.basic_ack(delivery_tag=method.delivery_tag)
        except Exception as e:
            logging.error(e)
        finally:
            delete_file_from_local(local_path)


def consumer() -> None:
    conn = pika.BlockingConnection(parameters)
    logging.info(f"Consumer Connected: {host}")
    ch = conn.channel()

    ch.exchange_declare(exchange=exchange, exchange_type="topic", durable=True)
    ch.queue_declare(queue=amqp_queue)
    ch.queue_bind(exchange=exchange,
                  queue=amqp_queue,
                  routing_key="listings.uploaded")
    ch.basic_consume(queue=amqp_queue,
                     on_message_callback=on_message,
                     auto_ack=False)
    ch.start_consuming()


def producer() -> None:
    conn = pika.BlockingConnection(parameters)
    logging.info(f"Producer Connected: {host}")
    ch = conn.channel()
    ch.exchange_declare(exchange=exchange, exchange_type="topic", durable=True)
    while True:
        try:
            listing: ListingStatus = queue.get(block=True)
            if listing is None:
                break
            logging.info(f"Listing content: {listing.to_json()}")
            properties = pika.BasicProperties(correlation_id=listing._id)
            ch.basic_publish(
                exchange=exchange,
                routing_key="listings.verified",
                body=json.dumps(listing.to_json()),
                properties=properties
            )

        except Exception as e:
            logging.error("Caught: %s", e)


consumer = threading.Thread(target=consumer, daemon=True)
producer = threading.Thread(target=producer, daemon=True)
consumer.start()
producer.start()

if __name__ == "__main__":
    app.run()
