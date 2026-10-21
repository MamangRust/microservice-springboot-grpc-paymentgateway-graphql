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


POST {{base_url}}/api/roles/create
Authorization: Bearer {{admin_token}}
Content-Type: application/json
{
  "name": "ROLE__"
}
HTTP 200
[Captures]
role_id: jsonpath "$.data.id"
[Asserts]



GET {{base_url}}/api/roles?page=1&pageSize=10
Authorization: Bearer {{staff_token}}
HTTP 200
[Asserts]
jsonpath "$.status" == "success"


GET {{base_url}}/api/roles/{{role_id}}
Authorization: Bearer {{admin_token}}
HTTP 200
[Asserts]
jsonpath "$.data.id" == {{role_id}}


GET {{base_url}}/api/roles/by-name?name=ROLE__
Authorization: Bearer {{admin_token}}
HTTP 200



POST {{base_url}}/api/roles/update
Authorization: Bearer {{admin_token}}
Content-Type: application/json
{
  "roleId": {{role_id}},
  "name": "ROLE___UPDATED"
}
HTTP 200

POST {{base_url}}/api/roles/trash/{{role_id}}
Authorization: Bearer {{admin_token}}
HTTP 200


GET {{base_url}}/api/roles/trashed?page=1
Authorization: Bearer {{admin_token}}
HTTP 200


POST {{base_url}}/api/roles/restore/{{role_id}}
Authorization: Bearer {{admin_token}}
HTTP 200



DELETE {{base_url}}/api/roles/{{role_id}}
Authorization: Bearer {{admin_token}}
HTTP 200



GET {{base_url}}/api/roles/{{role_id}}
Authorization: Bearer {{admin_token}}
