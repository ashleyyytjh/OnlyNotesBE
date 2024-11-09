const { exchangeCode, refreshTokens } = require('../../services/auth.service');

describe('Auth functions', () => {
  const originalEnv = process.env;

  beforeAll(() => {
    process.env = {
      ...originalEnv,
      cognito_subdomain: 'authd',
      cognito_domain: 'onlynotes.net',
      'cognito_client-id': 'testClientId',
      'cognito_client-secret': 'testClientSecret',
      cognito_callback_url: 'https://callback.example.com',
    };
    console.error = jest.fn();
  });

  afterAll(() => {
    process.env = originalEnv;
    console.error.mockRestore();
  });

  beforeEach(() => {
    fetch.resetMocks(); 
  });

  describe('exchangeCode', () => {
    it('should call fetch with the correct parameters for exchangeCode', async () => {
        const mockCode = 'sampleCode';
        const authUrl = 'https://authd.onlynotes.net/oauth2/token';

        const expectedBody = new URLSearchParams({
            grant_type: 'authorization_code',
            client_id: process.env['cognito_client-id'],
            client_secret: process.env['cognito_client-secret'],
            redirect_uri: process.env.cognito_callback_url,
            code: mockCode,
        }).toString(); 

        fetch.mockResponseOnce(JSON.stringify({ access_token: 'sampleAccessToken' }));

        const response = await exchangeCode(mockCode);

  
        const actualCall = fetch.mock.calls[0][1]; 

        // Check the response
        const data = await response.json();
        expect(data).toEqual({ access_token: 'sampleAccessToken' });
    });
});

  describe('refreshTokens', () => {
    it('should call fetch with the correct parameters for refreshTokens', async () => {
      const mockRefreshToken = 'sampleRefreshToken';
      const authUrl = 'https://authd.onlynotes.net/oauth2/token';
      const expectedBody = new URLSearchParams({
        grant_type: 'refresh_token',
        client_id: process.env['cognito_client-id'],
        client_secret: process.env['cognito_client-secret'],
        refresh_token: mockRefreshToken,
      }).toString();


      fetch.mockResponseOnce(JSON.stringify({ access_token: 'newAccessToken' }));

      const response = await refreshTokens(mockRefreshToken);

      const data = await response.json();
      expect(data).toEqual({ access_token: 'newAccessToken' });
    });

    it('should handle error responses correctly', async () => {
      const mockRefreshToken = 'sampleRefreshToken';

      // Mock a 400 error response from fetch
      fetch.mockResponseOnce('{"error": "invalid_token"}', { status: 400 });

      // Call the function and check for the error status
      const response = await refreshTokens(mockRefreshToken);
      expect(response.status).toBe(400);
    });
  });
});
