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


POST {{base_url}}/api/cards/create
Authorization: Bearer {{admin_token}}
Content-Type: application/json
{
  "userId": 2,
  "cardNumber": "1122334455667788",
  "cardType": "DEBIT",
  "cardProvider": "VISA",
  "expireDate": "2027-12-31",
  "cvv": "123"
}
HTTP 200
[Captures]
card_id: jsonpath "$.data.id"
card_number: jsonpath "$.data.cardNumber"
[Asserts]
jsonpath "$.message" == "Card created successfully"

POST {{base_url}}/api/cards/update
Authorization: Bearer {{admin_token}}
Content-Type: application/json
{
  "cardId": {{card_id}},
  "cardType": "CREDIT",
  "cardProvider": "MASTERCARD",
  "expireDate": "2028-12-31",
  "cvv": "999",
  "user_id": 1
}
HTTP 200


# Find All Cards (Admin/Staff Only)
GET {{base_url}}/api/cards
Authorization: Bearer {{admin_token}}
HTTP 200
[Asserts]
jsonpath "$.message" == "Cards retrieved successfully"


GET {{base_url}}/api/cards/{{card_id}}
Authorization: Bearer {{user_token}}
HTTP 200
[Asserts]
jsonpath "$.data.id" == {{card_id}}


GET {{base_url}}/api/cards/by-user/2
Authorization: Bearer {{user_token}}
HTTP 200
[Asserts]
jsonpath "$.data.id" == 2


GET {{base_url}}/api/cards/dashboard
Authorization: Bearer {{admin_token}}
HTTP 200


GET {{base_url}}/api/cards/stats/balance/monthly/by-card?year=2024&cardNumber={{card_number}}
Authorization: Bearer {{user_token}}
HTTP 200


POST {{base_url}}/api/cards/trash/{{card_id}}
Authorization: Bearer {{admin_token}}
HTTP 200
[Asserts]


GET {{base_url}}/api/cards/trashed
Authorization: Bearer {{admin_token}}
HTTP 200


DELETE {{base_url}}/api/cards/{{card_id}}
Authorization: Bearer {{admin_token}}
HTTP 200


POST {{base_url}}/api/cards/restore/{{card_id}}
Authorization: Bearer {{admin_token}}
HTTP 200



