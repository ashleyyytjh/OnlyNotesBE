const mongoose = require('mongoose');

const stripeSchema = new mongoose.Schema({
  stripeId: {  
    type: String,
    required: true,
    trim: true
  },
    
  userAccountEmail: {  
    type: String,
    required: true,
    trim: true
  }
});

module.exports = mongoose.model('Stripe', stripeSchema);
