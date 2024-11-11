const amqp = require("amqplib");

let ch;
async function configProducer() {
  try {
    const conn = await amqp.connect({
      protocol: process.env.RABBITMQ_PROTOCOL,
      username: process.env.RABBITMQ_USER,
      password: process.env.RABBITMQ_PASSWORD,
      hostname: process.env.RABBITMQ_HOST,
      port: process.env.RABBITMQ_PORT,
    });
    console.log(`PRODUCER Connected: ${process.env.RABBITMQ_HOST}`)
    ch = await conn.createChannel();
    await ch.assertExchange("orders", "topic");
  } catch (err) {
    console.log(err);
  }
}

async function publish(id, event, data) {
  try {
    const rk = `orders.${event}`;
    ch.publish("orders", rk, Buffer.from(JSON.stringify(data)), {
      correlationId: `${id}`
    });
    console.log('buffer', Buffer.from(JSON.stringify(data)));
    console.log("Published %s event for transaction id: %s", event, id);
    return true;
  } catch (err) {
    console.log(err);
  }
  return false;
}

async function publishOrderCreated(id, data) {
  console.log('data is ', data);
  console.log("published orderCreated event", data);
  return await publish(id, "created", data);
}

async function publishOrderSuccessful(id, data) {
  console.log('published orderSuccessful event', data)
  return await publish(id, "success", data);
}

module.exports = {
  configProducer,
  publishOrderCreated,
  publishOrderSuccessful,
};
