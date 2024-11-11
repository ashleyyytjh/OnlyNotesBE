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
    const { file_key, bucket } = getFileDetails(data.url);
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

function getFileDetails(s3Url) {
  const url = new URL(s3Url);
  
  const match = url.hostname.match(/^(.+)\.s3\..*\.amazonaws\.com$/);
  let bucket, file_key;

  if (match) {
      bucket = match[1];
      file_key = url.pathname.slice(1);
  } else {
      const parts = url.pathname.split('/');
      bucket = parts[1];
      file_key = parts.slice(2).join('/');
  }

  return { bucket, file_key };
}

module.exports = configMQ;
