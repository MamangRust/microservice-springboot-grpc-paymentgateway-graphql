POST {{base_url}}/api/auth/login
Content-Type: application/json
{
  "username": "testuser_hurl",
  "password": "password123"
}
HTTP 200
[Captures]
access_token: jsonpath "$.data.access_token"
refresh_token: jsonpath "$.data.refresh_token"


GET {{base_url}}/api/auth/me
Authorization: Bearer {{access_token}}
HTTP 200
[Asserts]
status == 200
jsonpath "$.data.username" == "testuser_hurl"


POST {{base_url}}/api/auth/refresh-token
Authorization: Bearer {{access_token}}
Content-Type: application/json
{
  "refreshToken": "{{refresh_token}}"
}
HTTP 200
[Asserts]
status == 200
jsonpath "$.message" == "Access token refreshed successfully"
jsonpath "$.data.access_token" exists