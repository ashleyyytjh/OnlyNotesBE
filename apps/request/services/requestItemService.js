const RequestItem = require("../models/RequestItem");
const AWS = require('aws-sdk');
const {getId} = require("token-verifier-mee-rebus");

const sqs = new AWS.SQS({ apiVersion: '2012-11-05' });
const notifyQueueUrl = process.env["NOTIFY_SQS"];

AWS.config.update({ region: process.env["AWS_REGION"] });

class RequestItemService {
  static async createRequest(user, requestData, email) {
    const regex = /^[a-zA-Z0-9 _-]+$/;

    // Only allow these characters ( alpha numbers _ - space )
    if (!regex.test(requestData.tag)) {
      throw new Error("validation error request tag");
    }

    const itemToCreate = await RequestItem.find({userId: user, tag: requestData.tag})
    if(itemToCreate.length > 0) {
      throw new Error("Request already exist");
    }

    try {
      const request = new RequestItem();
      request.userId = user;
      request.email = email;
      request.tag = requestData.tag;

      await request.save();
      return request;
    } catch (error) {
      console.log(error);
      throw new Error("internal server error");
    }
  }

  static async findById(requestId, user) {

    const itemToFind = await RequestItem.findById(requestId)
    if (itemToFind && itemToFind['userId'] !== user){
      throw new Error("Request not found");
    }

    try {
      // const exists = await RequestItem.exists({ _id: requestId })
      return await RequestItem.findById(requestId);
    } catch (error) {
      throw new Error("internal server error");
    }
  }

  static async findByUserId(userId) {
    await RequestItem.exists({ userId: userId }).catch((err) => {
      throw new Error("user not found");
    });

    try {
      return await RequestItem.find({ userId: userId });
    } catch (error) {
      throw new Error("internal server error");
    }
  }

  static async findAll() {
    try {
      return await RequestItem.find({});
    } catch (error) {
      throw new Error("Internal server error");
    }
  }

  static async findAllByUser(user) {
    try {
      return await RequestItem.find({userId: user});
    } catch (error) {
      throw new Error("Internal server error");
    }
  }

  static async update(user, requestId, requestData) {
    try {
      return await RequestItem.findByIdAndUpdate(requestId, requestData, {
        new: true,
      });
    } catch (error) {
      throw new Error("Internal server error");
    }
  }

  static async delete(requestId, user) {

    const itemToDelete = await RequestItem.findById(requestId)
    console.log(itemToDelete['userId'])
    console.log(user)
    if (itemToDelete && itemToDelete['userId'] !== user){
      throw new Error("Request not found");
    }

    try {
      return await RequestItem.findByIdAndDelete(requestId);
    } catch (error) {
      throw new Error("Internal server error");
    }
  }


  static async notifyRequest(tag, noteId) {
    console.log("NOTIFY: ", tag, noteId)

    await RequestItem.exists({ tag: tag }).catch((err) => {
      throw new Error("Tag not found");
    });

    try {
      const requestItems = await RequestItem.find({ tag: tag });

      for (let i = 0; i < requestItems.length; i++) {
        await RequestItemService.notifyToQueue(requestItems[i].email, tag, noteId)
      }

      return;
    } catch (error) {
      throw new Error("internal server error");
    }
  }

  static async notifyToQueue(email, tag, noteId){

    const messageBody = {
      sellerEmail: email,
      tag: tag,
      note_id: noteId
    };

    const params = {
      MessageBody: JSON.stringify(messageBody),
      QueueUrl: notifyQueueUrl,
    };

    console.log("PARAMETER: ", params);


    try {
      const data = await sqs.sendMessage(params).promise();
      console.log('Message sent successfully:', data.MessageId);
      return true;
    } catch (err) {
      console.error('Error sending message to SQS:', err);
      return false;
    }
  }


}

module.exports = RequestItemService;







