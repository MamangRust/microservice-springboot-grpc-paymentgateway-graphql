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


POST {{base_url}}/api/topups/create
Authorization: Bearer {{user_token}}
Content-Type: application/json
{
  "cardNumber": "1111222233334444",
  "topupAmount": 100000,
  "topupMethod": "BANK_TRANSFER"
}
HTTP 200
[Captures]
topup_id: jsonpath "$.data.id"
[Asserts]
jsonpath "$.status" == "success"
jsonpath "$.data.topupAmount" == 100000


POST {{base_url}}/api/topups/create
Authorization: Bearer {{admin_token}}
Content-Type: application/json
{
  "cardNumber": "5555666677778888",
  "topupAmount": 50000,
  "topupMethod": "E_WALLET"
}
HTTP 200
[Asserts]
jsonpath "$.status" == "success"


GET {{base_url}}/api/topups?page=1&pageSize=10
Authorization: Bearer {{admin_token}}
HTTP 200
[Asserts]
jsonpath "$.status" == "success"


GET {{base_url}}/api/topups/{{topup_id}}
Authorization: Bearer {{user_token}}
HTTP 200
[Asserts]
jsonpath "$.data.id" == {{topup_id}}


GET {{base_url}}/api/topups/card/1111222233334444
Authorization: Bearer {{user_token}}
HTTP 200
[Asserts]
jsonpath "$.data[0].cardNumber" == "1111222233334444"


POST {{base_url}}/api/topups/update
Authorization: Bearer {{admin_token}}
Content-Type: application/json
{
  "topupId": {{topup_id}},
  "cardNumber": "1111222233334444",
  "topupAmount": 150000,
  "topupMethod": "BANK_TRANSFER"
}
HTTP 200
[Asserts]
jsonpath "$.data.topupAmount" == 150000


GET {{base_url}}/api/topups/stats/amount/monthly?year=2024
Authorization: Bearer {{admin_token}}
HTTP 200


GET {{base_url}}/api/topups/stats/status/monthly/success?year=2024&month=3
Authorization: Bearer {{admin_token}}
HTTP 200


POST {{base_url}}/api/topups/trash/{{topup_id}}
Authorization: Bearer {{admin_token}}
HTTP 200
[Asserts]
jsonpath "$.data.deletedAt" exists


GET {{base_url}}/api/topups/trashed?page=1
Authorization: Bearer {{admin_token}}
HTTP 200


POST {{base_url}}/api/topups/restore/{{topup_id}}
Authorization: Bearer {{admin_token}}
HTTP 200



DELETE {{base_url}}/api/topups/{{topup_id}}
Authorization: Bearer {{admin_token}}
HTTP 200
