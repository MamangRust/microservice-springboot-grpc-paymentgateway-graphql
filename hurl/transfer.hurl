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


POST {{base_url}}/api/transfers/create
Authorization: Bearer {{user_token}}
Content-Type: application/json
{
  "transferFrom": "1111222233334444",
  "transferTo": "5555666677778888",
  "transferAmount": 200000
}
HTTP 200
[Captures]
transfer_id: jsonpath "$.data.id"


GET {{base_url}}/api/transfers?page=1&pageSize=10
Authorization: Bearer {{admin_token}}
HTTP 200
[Asserts]
jsonpath "$.status" == "success"


GET {{base_url}}/api/transfers/{{transfer_id}}
Authorization: Bearer {{admin_token}}
HTTP 200
[Asserts]
jsonpath "$.data.id" == {{transfer_id}}


GET {{base_url}}/api/transfers/from/1111222233334444
Authorization: Bearer {{user_token}}
HTTP 200



GET {{base_url}}/api/transfers/to/5555666677778888
Authorization: Bearer {{admin_token}}
HTTP 200
[Asserts]
jsonpath "$.data[0].transferTo" == "5555666677778888"


POST {{base_url}}/api/transfers/update
Authorization: Bearer {{admin_token}}
Content-Type: application/json
{
  "transferId": {{transfer_id}},
  "transferFrom": "1111222233334444",
  "transferTo": "5555666677778888",
  "transferAmount": 200000
}
HTTP 200
[Asserts]
jsonpath "$.data.transferAmount" == 200000


GET {{base_url}}/api/transfers/stats/amount/monthly?year=2024
Authorization: Bearer {{admin_token}}
HTTP 200


POST {{base_url}}/api/transfers/trash/{{transfer_id}}
Authorization: Bearer {{admin_token}}
HTTP 200


GET {{base_url}}/api/transfers/trashed?page=1
Authorization: Bearer {{admin_token}}
HTTP 200


POST {{base_url}}/api/transfers/restore/{{transfer_id}}
Authorization: Bearer {{admin_token}}
HTTP 200


DELETE {{base_url}}/api/transfers/{{transfer_id}}
Authorization: Bearer {{admin_token}}
HTTP 200
[Asserts]
jsonpath "$.data" == true