const amqp = require("amqplib");
const sns = require("../aws/sns");
const { retrieveUser } = require("../../services/user.service");
require('dotenv').config();

let ch;
const orderSuccessQ = 'order-email';

async function configMQ() {
  url = `${process.env.RABBITMQ_PROTOCOL}://` +
    `${process.env.RABBITMQ_USERNAME}:` +
    `${process.env.RABBITMQ_PASSWORD}@` +
    `${process.env.RABBITMQ_HOST}:` +
    `${process.env.RABBITMQ_PORT}`
  try {
    console.log(url)
    const conn = await amqp.connect(url);

    console.log(`CONSUMER Connected: ${process.env.RABBITMQ_HOST}`)
    ch = await conn.createChannel();
    ch.assertExchange("orders", "topic");

    await ch.assertQueue(orderSuccessQ);
    ch.bindQueue(orderSuccessQ, 'orders', 'orders.email');
    ch.consume(orderSuccessQ, handleOrderSuccess)

  } catch (err) {
    console.log(err);
  }
}

const handleOrderSuccess = async (message) => {
  ch.ack(message);
  try {
    const data = JSON.parse(message.content.toString());
    console.log(data);
    const buyer = await retrieveUser(data.buyerId);
    const seller = await retrieveUser(data.fkAccountOwner);
    const url = data.url;
    const file_key = url.split('/').pop();
    const bucket = url.replace(`/${file_key}`, '');
    console.log("File key: " + file_key);
    console.log(bucket);
    
    const payload = {
      orderID: data._id.toString(),
      userId: data.userId,
      bucket: bucket,
      file_key: file_key,
      course: data.categoryCode,
      notes: data.title,
      buyerEmail: buyer.email,
      buyerName: buyer.username,
      sellerEmail: seller.email,
      sellerName: seller.username,
      price: data.price,
    }
    const ok = await sns.publish(JSON.stringify(payload))
    if (!ok) {
      console.log('Error publishing message!');
    }
  } catch (err) {
    console.log(err);
  }
}

module.exports = configMQ;
