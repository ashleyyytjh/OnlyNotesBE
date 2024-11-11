const RequestItemService = require('../services/requestItemService');
const {getId, getEmail, getUsername} = require('token-verifier-mee-rebus');

async function getRequestItemById(req, res) {
  try {
    const { requestId } = req.params;
    const requestItem = await RequestItemService.findById(requestId, getId(req))
    res.status(200).json(requestItem);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
}

async function createRequest(req, res) {
  const userId = getId(req);
  const email = getEmail(req);

  try {
    const requestItem = await RequestItemService.createRequest(userId, req.body, email);
    res.status(201).json({ message: 'Request created successfully', requestItem });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
}

async function getAllRequestItem(req, res) {
  try {
      const user = getId(req)
      const requestItems = await RequestItemService.findAllByUser(user);
      res.status(200).json(requestItems);
  } catch (error) {
    res.status(500).json({ message: 'Internal server error' });
  }
}

async function updateRequestById(req, res) {

  const {requestId} = req.params;
  const updateData = req.body;
  const userId = getId(req);

  try {
    const updatedRequest = await RequestItemService.update(userId, requestId, updateData);
    res.status(200).json({ message: 'Reqeust updated successfully', updatedRequest });
  } catch (error) {
    res.status(500).json({ message: 'Internal server error' });
  }
}

async function deleteRequestById(req, res) {
  try {
    const {requestId} = req.params;
    const requestItem = await RequestItemService.delete(requestId, getId(req));
    res.status(200).json({ message: 'Request deleted successfully', requestItem });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
}

async function notifyRequest(req, res) {
  try {
    const {tag} = req.params;
    const notifyList = await RequestItemService.notifyRequest(tag);
    res.status(200).json(notifyList);
  } catch (error) {
    res.status(500).json({ message: 'Internal server error' });
  }
}

module.exports = {
  getRequestItemById,
  createRequest,
  getAllRequestItem,
  updateRequestById,
  deleteRequestById,
  notifyRequest
};