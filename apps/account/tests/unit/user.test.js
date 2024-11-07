const AWS = require('aws-sdk');
const { retrieveUser } = require('../../services/user.service'); 

jest.mock('aws-sdk', () => {
  const CognitoIdentityServiceProvider = {
    listUsers: jest.fn().mockReturnThis(),
    promise: jest.fn(),
  };
  return { CognitoIdentityServiceProvider: jest.fn(() => CognitoIdentityServiceProvider) };
});

describe('retrieveUser', () => {
  beforeEach(() => {
    AWS.CognitoIdentityServiceProvider().listUsers.mockClear();
    AWS.CognitoIdentityServiceProvider().promise.mockClear();
  });

  it('should return user data when Cognito returns a user', async () => {
    const mockUser = {
      Username: 'testUser',
      Attributes: [
        { Name: 'email', Value: 'test@example.com' },
        { Name: 'sub', Value: '12345' },
      ],
      UserCreateDate: new Date(),
      UserStatus: 'CONFIRMED',
    };

    AWS.CognitoIdentityServiceProvider().promise.mockResolvedValue({ Users: [mockUser] });

    
    const result = await retrieveUser('12345');

    // Check if result matches expected output
    expect(result).toEqual({
      username: 'testUser',
      email: 'test@example.com',
      id: '12345',
      userCreateDate: mockUser.UserCreateDate,
      userStatus: 'CONFIRMED',
    });
  });

  it('should throw an error if Cognito returns no users', async () => {
    // Mocking an empty Users array, but including a mock function to catch the error
    AWS.CognitoIdentityServiceProvider().promise.mockResolvedValueOnce({ Users: [{}] });
  
    await expect(retrieveUser('12345')).rejects.toThrow(TypeError); 
  });

  it('should return user data when Cognito returns a user', async () => {
    AWS.CognitoIdentityServiceProvider().promise.mockResolvedValueOnce({
      Users: [{
        Username: 'testuser',
        Attributes: [
          { Name: 'email', Value: 'test@example.com' },
          { Name: 'sub', Value: '12345' }
        ],
        UserCreateDate: new Date(),
        UserStatus: 'CONFIRMED'
      }]
    });
  
    const result = await retrieveUser('12345');
    expect(result).toEqual({
      username: 'testuser',
      email: 'test@example.com',
      id: '12345',
      userCreateDate: expect.any(Date),
      userStatus: 'CONFIRMED'
    });
  });

  it('should handle Cognito API errors', async () => {
    AWS.CognitoIdentityServiceProvider().promise.mockRejectedValueOnce(new Error('API error'));
  
    await expect(retrieveUser('12345')).rejects.toThrow('API error');
  });
  
});
