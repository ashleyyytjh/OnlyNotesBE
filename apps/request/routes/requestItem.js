const express = require('express');
const router = express.Router();
const requestItemController = require('../controllers/requestItemController');
const {tokenValid} = require('token-verifier-mee-rebus');

router.get('/:requestId',tokenValid, requestItemController.getRequestItemById);
router.get('/',tokenValid, requestItemController.getAllRequestItem);
router.post('/',tokenValid, requestItemController.createRequest);
router.put('/:requestId',tokenValid, requestItemController.updateRequestById);
router.delete('/:requestId',tokenValid, requestItemController.deleteRequestById);

module.exports = router;
