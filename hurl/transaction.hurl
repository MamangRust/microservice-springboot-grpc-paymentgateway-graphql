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


POST {{base_url}}/api/transaction/create
Authorization: Bearer {{user_token}}
X-api-Key: 2e74a01e-ea69-41b8-9305-68185b13c1fe
Content-Type: application/json
{
  "cardNumber": "5555666677778888",
  "amount": 100000,
  "paymentMethod": "CARD_PAYMENT",
  "merchantId": 1
}
HTTP 200
[Captures]
transaction_id: jsonpath "$.data.id"


GET {{base_url}}/api/transaction?page=1&pageSize=10
Authorization: Bearer {{admin_token}}
HTTP 200
[Asserts]
jsonpath "$.status" == "success"


GET {{base_url}}/api/transaction/{{transaction_id}}
Authorization: Bearer {{user_token}}
HTTP 200
[Asserts]
jsonpath "$.data.id" == {{transaction_id}}


GET {{base_url}}/api/transaction/merchant/1
Authorization: Bearer {{admin_token}}
HTTP 200
[Asserts]
jsonpath "$.data[0].merchantId" == 1


POST {{base_url}}/api/transaction/update
Authorization: Bearer {{admin_token}}
X-api-Key: 2e74a01e-ea69-41b8-9305-68185b13c1fe
Content-Type: application/json
{
  "transactionId": {{transaction_id}},
  "cardNumber": "5555666677778888",
  "amount": 100000,
  "paymentMethod": "CARD_PAYMENT",
  "merchantId": 1
}
HTTP 200


GET {{base_url}}/api/transaction/stats/amount/monthly?year=2024
Authorization: Bearer {{admin_token}}
HTTP 200


POST {{base_url}}/api/transaction/trash/{{transaction_id}}
Authorization: Bearer {{admin_token}}
HTTP 200



GET {{base_url}}/api/transaction/trashed?page=1
Authorization: Bearer {{admin_token}}
HTTP 200


POST {{base_url}}/api/transaction/restore/{{transaction_id}}
Authorization: Bearer {{admin_token}}
HTTP 200



DELETE {{base_url}}/api/transaction/{{transaction_id}}
Authorization: Bearer {{admin_token}}
HTTP 200