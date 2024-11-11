const RequestItemService = require("../../services/requestItemService");
const RequestItem = require("../../models/RequestItem");
const AWSMock = require("aws-sdk-mock");
const AWS = require("aws-sdk");

jest.mock("../../models/RequestItem");

describe("RequestItemService", () => {
  const notifyQueueUrl = process.env["NOTIFY_SQS"] = "https://mock-queue-url";
  const user = "testUser";
  const email = "test@example.com";
  const requestId = "12345";
  const requestData = { tag: "testTag" };
  const noteId = "note123";

  beforeAll(() => {
    // Mock AWS SQS
    AWSMock.setSDKInstance(AWS);
  });

  afterEach(() => {
    jest.clearAllMocks(); // Clear mocks between tests
  });

  afterAll(() => {
    AWSMock.restore("SQS");
  });

  describe("createRequest", () => {
    it("should create a request item successfully", async () => {
      RequestItem.find = jest.fn().mockResolvedValue([]); 
    
      const saveMock = jest.fn().mockResolvedValue({ userId: user, email, tag: requestData.tag });
      RequestItem.mockImplementation(() => ({ save: saveMock }));
      const result = await RequestItemService.createRequest(user, requestData, email);
    
      expect(RequestItem.find).toHaveBeenCalled();  
      expect(saveMock).toHaveBeenCalled();  
      expect(result.userId).toBe(user);  
      expect(result.tag).toBe(requestData.tag); 
    });

    it("should throw a validation error for invalid tag", async () => {
      await expect(RequestItemService.createRequest(user, { tag: "invalid@tag" }, email))
        .rejects
        .toThrow("validation error request tag");
    });

    it("should throw an error if request already exists", async () => {
      RequestItem.find = jest.fn().mockResolvedValue([{ tag: "testTag" }]); 
      await expect(RequestItemService.createRequest(user, requestData, email))
        .rejects
        .toThrow("Request already exist");
    });
  });

  describe("findById", () => {
    it("should return a request item by ID", async () => {
      const findByIdMock = jest.fn().mockResolvedValue({ _id: requestId, userId: user, tag: requestData.tag });
      RequestItem.findById = findByIdMock;

      const result = await RequestItemService.findById(requestId, user);

      expect(findByIdMock).toHaveBeenCalledWith(requestId);
      expect(result.tag).toBe(requestData.tag);
    });

    it("should throw an error if the request is not found or not owned by the user", async () => {
      const findByIdMock = jest.fn().mockResolvedValue({ userId: "otherUser" });
      RequestItem.findById = findByIdMock;

      await expect(RequestItemService.findById(requestId, user)).rejects.toThrow("Request not found");
    });

    it("should return null if the item is not found", async () => {
      RequestItem.findById = jest.fn().mockResolvedValue(null);
      const result = await RequestItemService.findById("invalidId", user);
      expect(result).toBeNull();
    });
  });

  describe("notifyRequest", () => {
    it("should send messages to SQS for each matching request item", async () => {
      RequestItem.exists = jest.fn().mockResolvedValue(true);
      RequestItem.find = jest.fn().mockResolvedValue([{ email: "test@example.com", tag: "testTag" }]);
      RequestItemService.notifyToQueue = jest.fn().mockResolvedValue(true);

      await RequestItemService.notifyRequest("testTag", noteId);

      expect(RequestItem.exists).toHaveBeenCalledWith({ tag: "testTag" });
      expect(RequestItem.find).toHaveBeenCalledWith({ tag: "testTag" });
      expect(RequestItemService.notifyToQueue).toHaveBeenCalledWith("test@example.com", "testTag", noteId);
    });

    it("should throw an error if no matching tag is found", async () => {
      RequestItem.exists = jest.fn().mockRejectedValue(new Error("Tag not found"));

      await expect(RequestItemService.notifyRequest("nonExistentTag", noteId)).rejects.toThrow("Tag not found");

      expect(RequestItem.exists).toHaveBeenCalledWith({ tag: "nonExistentTag" });
    });
  });

  describe("delete", () => {
    it("should delete a request item by ID", async () => {
      const findByIdMock = jest.fn().mockResolvedValue({ _id: requestId, userId: user });
      const findByIdAndDeleteMock = jest.fn().mockResolvedValue({ _id: requestId });
      RequestItem.findById = findByIdMock;
      RequestItem.findByIdAndDelete = findByIdAndDeleteMock;

      const result = await RequestItemService.delete(requestId, user);

      expect(findByIdAndDeleteMock).toHaveBeenCalledWith(requestId);
      expect(result._id).toBe(requestId);
    });

    it("should throw an error if request does not belong to the user", async () => {
      RequestItem.findById = jest.fn().mockResolvedValue({ userId: "otherUser" });

      await expect(RequestItemService.delete(requestId, user)).rejects.toThrow("Request not found");
    });

    it("should throw an error if deletion fails", async () => {
      RequestItem.findByIdAndDelete = jest.fn().mockRejectedValue(new Error("Request not found"));
    
      await expect(RequestItemService.delete(requestId, user)).rejects.toThrow("Request not found");
    });
  });
});
