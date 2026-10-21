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
  "username": "staff",
  "password": "password123"
}
HTTP 200
[Captures]
staff_token: jsonpath "$.data.access_token"


GET {{base_url}}/api/saldos?page=1&pageSize=10
Authorization: Bearer {{staff_token}}
HTTP 200
[Captures]
saldo_id: jsonpath "$.data[0].id"
saldo_card_number: jsonpath "$.data[0].cardNumber"


GET {{base_url}}/api/saldos/{{saldo_id}}
Authorization: Bearer {{admin_token}}
HTTP 200
[Captures]
saldo_id: jsonpath "$.data.id"
saldo_card_number: jsonpath "$.data.cardNumber"
[Asserts]
jsonpath "$.data.id" == {{saldo_id}}


GET {{base_url}}/api/saldos/by-card/{{saldo_card_number}}
Authorization: Bearer {{admin_token}}
HTTP 200
[Asserts]
jsonpath "$.data.cardNumber" == "{{saldo_card_number}}"


GET {{base_url}}/api/saldos/balance/monthly?year=2024
Authorization: Bearer {{staff_token}}
HTTP 200


GET {{base_url}}/api/saldos/balance/yearly?year=2024
Authorization: Bearer {{admin_token}}
HTTP 200


POST {{base_url}}/api/saldos/update
Authorization: Bearer {{admin_token}}
Content-Type: application/json
{
  "saldoId": {{saldo_id}},
  "totalBalance": 900000
}
HTTP 200


POST {{base_url}}/api/saldos/trash/{{saldo_id}}
Authorization: Bearer {{admin_token}}
HTTP 200


GET {{base_url}}/api/saldos/trashed?page=1
Authorization: Bearer {{admin_token}}
HTTP 200


POST {{base_url}}/api/saldos/restore/{{saldo_id}}
Authorization: Bearer {{admin_token}}
HTTP 200


DELETE {{base_url}}/api/saldos/{{saldo_id}}
Authorization: Bearer {{admin_token}}
HTTP 200