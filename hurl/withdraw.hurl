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


POST {{base_url}}/api/withdraws/create
Authorization: Bearer {{admin_token}}
Content-Type: application/json
{
  "cardNumber": "1111222233334444",
  "withdrawAmount": 100000,
  "withdrawTime": "2024-03-07T14:30:00"
}
HTTP 200
[Captures]
withdraw_id: jsonpath "$.data.id"
[Asserts]
jsonpath "$.status" == "success"
jsonpath "$.data.withdrawAmount" == 100000



GET {{base_url}}/api/withdraws?page=1&pageSize=10
Authorization: Bearer {{admin_token}}
HTTP 200
[Asserts]
jsonpath "$.status" == "success"


GET {{base_url}}/api/withdraws/{{withdraw_id}}
Authorization: Bearer {{user_token}}
HTTP 200
[Asserts]
jsonpath "$.data.id" == {{withdraw_id}}


GET {{base_url}}/api/withdraws/by-card/1111222233334444
Authorization: Bearer {{admin_token}}
HTTP 200
[Asserts]
jsonpath "$.data[0].cardNumber" == "1111222233334444"



POST {{base_url}}/api/withdraws/update
Authorization: Bearer {{admin_token}}
Content-Type: application/json
{
  "withdrawId": {{withdraw_id}},
  "cardNumber": "1111222233334444",
  "withdrawAmount": 150000,
  "withdrawTime": "2024-03-07T15:00:00"
}
HTTP 200
[Asserts]
jsonpath "$.data.withdrawAmount" == 150000


GET {{base_url}}/api/withdraws/stats/amount/monthly?year=2024
Authorization: Bearer {{admin_token}}
HTTP 200


POST {{base_url}}/api/withdraws/trash/{{withdraw_id}}
Authorization: Bearer {{admin_token}}
HTTP 200


GET {{base_url}}/api/withdraws/trashed?page=1
Authorization: Bearer {{admin_token}}
HTTP 200


POST {{base_url}}/api/withdraws/restore/{{withdraw_id}}
Authorization: Bearer {{admin_token}}
HTTP 200


DELETE {{base_url}}/api/withdraws/{{withdraw_id}}
Authorization: Bearer {{admin_token}}
HTTP 200