const dotenv = require('dotenv')
dotenv.config()

const auth_url = process.env.cognito_subdomain + "." + process.env.cognito_domain

async function exchangeCode(code) {
    // console.log(process.env["cognito_client-id"])
    return await fetch(`https://${auth_url}/oauth2/token`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams({
            grant_type: 'authorization_code',
            client_id: process.env["cognito_client-id"],
            redirect_uri: process.env.cognito_callback_url,
            code: code,

        })
    });


}

// async function logout() {
//     // console.log(process.env["cognito_client-id"])
//     // console.log(process.env.cognito_callback_url)
//     return await fetch(`https://${auth_url}/oauth2/token`, {
//         method: 'POST',
//         headers: {
//             'Content-Type': 'application/x-www-form-urlencoded',
//         },
//         body: new URLSearchParams({
//             grant_type: 'authorization_code',
//             client_id: process.env["cognito_client-id"],
//             redirect_uri: process.env.cognito_callback_url,
//             code: code,
//         })
//     });
//
//
// }
// GET https://mydomain.auth.us-east-1.amazoncognito.com/logout?
//     client_id=1example23456789&
// logout_uri=https%3A%2F%2Fwww.example.com%2Fwelcome

async function refreshTokens(refreshToken) {
    const result =  await fetch(`https://${auth_url}/oauth2/token`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({
            grant_type: 'refresh_token',
            client_id: process.env["cognito_client-id"],
            refresh_token: refreshToken,
        }),
    });
    // console.log(result.status)
    return result;

}



module.exports = {exchangeCode, refreshTokens}
