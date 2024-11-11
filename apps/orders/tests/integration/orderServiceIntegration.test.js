const supertest = require('supertest');
const app = require('../../app'); // Import your Express app
const mongoose = require('mongoose');
const Order = require('../../models/Order'); // Assuming you have a model at models/order.js

describe('Order Service Integration Tests', () => {
  let createdOrderId;

  // Before each test, create a new order in the database
  beforeEach(async () => {
    // Remove all existing orders to ensure a clean state
    await Order.deleteMany({});

    // Create a new order
    const newOrder = new Order({
      stripeTransactionId: 'txn123',
      noteId: 'note123',
      buyerId: 'buyer123',
      orderStatus: 'created',
      orderPrice: 100
    });
    const savedOrder = await newOrder.save();
    createdOrderId = savedOrder._id; // Store the created order's ID
  });

  // After all tests, cleanup any data and close DB connection
  afterAll(async () => {
    await mongoose.connection.close(); // Close DB connection
  });

  it('should fetch an order successfully', async () => {
    const response = await supertest(app).get(`/api/v1/orders/${createdOrderId}`);
    expect(response.status).toBe(200);
    expect(response.body._id).toBe(createdOrderId.toString());
  });

  it('should return 404 if order not found', async () => {
    const nonExistentId = '60f8f1c9e0c72a7a94d937e4'; // Example of an invalid ID
    const response = await supertest(app).get(`/api/v1/orders/${nonExistentId}`);
    expect(response.status).toBe(404);
  });

  it('should create a new order successfully', async () => {
    const newOrder = {
      stripeTransactionId: 'txn124',
      noteId: 'note124',
      buyerId: 'buyer124',
      orderStatus: 'created',
      orderPrice: 150
    };
    const response = await supertest(app).post('/api/v1/orders').send(newOrder);
    expect(response.status).toBe(201);
    expect(response.body.message).toBe('Order created successfully');
  });

  it('should return 404 if order not found during delete', async () => {
    const nonExistentId = '60f8f1c9e0c72a7a94d937e4'; // Example of an invalid ID
    const response = await supertest(app).delete(`/api/v1/orders/${nonExistentId}`);
    expect(response.status).toBe(404);
  });
  

  it('should return 404 if no orders found for account ID', async () => {
    const accountId = 'nonExistentAccount';
    const response = await supertest(app).get(`/api/v1/orders/account/${accountId}`);
    expect(response.status).toBe(404);
  });

  it('should update an order successfully', async () => {
    const updatedOrder = {
      orderStatus: 'shipped',
      orderPrice: 200
    };
    const response = await supertest(app)
      .put(`/api/v1/orders/${createdOrderId}`)
      .send(updatedOrder);

    expect(response.status).toBe(200);
    expect(response.body.message).toBe('Order updated successfully');
  });
});