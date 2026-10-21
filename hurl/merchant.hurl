POST {{base_url}}/api/auth/login
Content-Type: application/json
{
  "username": "admin",
  "password": "password123"
}
HTTP 200
[Captures]
admin_token: jsonpath "$.data.access_token"


POST {{base_url}}/api/auth/login
Content-Type: application/json
{
  "username": "user1",
  "password": "password123"
}
HTTP 200
[Captures]
user_token: jsonpath "$.data.access_token"


POST {{base_url}}/api/merchants/create
Authorization: Bearer {{admin_token}}
Content-Type: application/json
{
  "name": "Toko Dotaaa",
  "userId": 1
}
HTTP 200
[Captures]
merchant_id: jsonpath "$.data.id"


POST {{base_url}}/api/merchants/update
Authorization: Bearer {{admin_token}}
Content-Type: application/json
{
  "merchantId": {{merchant_id}},
  "name": "Toko Hurl Updated",
  "userId": 1,
  "status": "pending"
}
HTTP 200


POST {{base_url}}/api/merchants/create
Authorization: Bearer {{user_token}}
Content-Type: application/json
{
  "name": "Toko Liku",
  "userId": 2
}
HTTP 403


GET {{base_url}}/api/merchants?page=1&pageSize=10
Authorization: Bearer {{admin_token}}
HTTP 200
[Asserts]
jsonpath "$.status" == "success"


GET {{base_url}}/api/merchants/{{merchant_id}}
Authorization: Bearer {{admin_token}}
HTTP 200
[Asserts]
jsonpath "$.data.id" == {{merchant_id}}


POST {{base_url}}/api/merchants/trash/{{merchant_id}}
Authorization: Bearer {{admin_token}}
HTTP 200
[Asserts]
jsonpath "$.data.deletedAt" exists


GET {{base_url}}/api/merchants/trashed?page=1
Authorization: Bearer {{admin_token}}
HTTP 200


POST {{base_url}}/api/merchants/restore/{{merchant_id}}
Authorization: Bearer {{admin_token}}
HTTP 200
[Asserts]
jsonpath "$.data.deletedAt" == "null"


DELETE {{base_url}}/api/merchants/{{merchant_id}}
Authorization: Bearer {{admin_token}}
HTTP 200