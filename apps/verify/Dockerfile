FROM python:3.11-alpine3.19

WORKDIR /usr/src/app

COPY requirements.txt ./

RUN apk add --no-cache gcc python3-dev musl-dev linux-headers

RUN python -m pip install --upgrade pip

RUN pip install --no-cache-dir -r requirements.txt

COPY . .

EXPOSE 5000

# Command to start the server
# CMD ["opentelemetry-instrument", "gunicorn", "-w", "1", "-b", "0.0.0.0:5000", "--log-level", "info", "--capture-output", "app:app"]
CMD ["python", "app.py"]