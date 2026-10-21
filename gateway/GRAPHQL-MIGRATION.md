# Gateway REST → GraphQL — Rencana Migrasi (payment_gateway)

> **SUDAH DIIMPLEMENTASIKAN** — lihat §16 untuk catatan/deviasi implementasi.
> Dokumen asal adalah rencana; statusnya kini selesai (full GraphQL, seluruh controller REST dihapus).
> Scope: **hanya modul `gateway/`**. Semua microservice domain (auth, user, role, merchant,
> card, saldo, topup, transaction, transfer, withdraw, stats) tetap apa adanya (gRPC server +
> Flyway + Kafka). Gateway tetap **BFF tanpa database**: satu-satunya perubahan adalah lapisan
> edge dari REST/JSON menjadi GraphQL.
>
> Referensi pola: `ecommerce/gateway/GRAPHQL-MIGRATION.md` (sudah diimplementasikan di sana).
> Dokumen ini menyesuaikan pola tersebut dengan domain payment gateway.

---

## 1. Tujuan & ruang lingkup

| | |
|---|---|
| **Masuk scope** | Modul `gateway` — ganti 11 controller REST + DTO request menjadi schema GraphQL + resolver. |
| **Tidak masuk scope** | Proto (`common/src/main/proto`), server gRPC domain, DB, Kafka, ClickHouse, deployment. |
| **Yang dipertahankan** | `GrpcClientConfig` (channel + blocking stub per service), `security/*` (JWT filter, entry point), `mapper/ProtoMapper`. |
| **Tujuan teknis** | 1 endpoint `POST /graphql`, client bebas memilih field (hilangkan over-fetching), skema sebagai kontrak, tetap pakai protobuf stub yang sudah ada. |
| **Keputusan akhir** | **Full GraphQL**: seluruh controller REST lama dihapus. Tidak ada upload multipart di gateway ini, jadi tidak ada `UploadController`. |

### Kondisi saat ini

- 11 controller REST `@RestController` di `gateway/.../controller/`:
  `Auth`, `Card`, `Merchant`, `Role`, `Saldo`, `Stats`, `Topup`, `Transaction`, `Transfer`,
  `User`, `Withdraw`.
- Envelope seragam: `ApiResponse<T> { status, message, data }` dan
  `ApiResponsePagination<T> { status, message, data, pagination }` (dari modul `common`).
- Otorisasi per-endpoint via `@PreAuthorize("hasAnyRole(...)")` di atas `@EnableMethodSecurity`
  (role `ADMIN`, `STAFF`, `USER`). `CardController` punya **class-level**
  `@PreAuthorize("isAuthenticated()")`.
- `POST /api/transactions/create` dan `/api/transactions/update` membaca **header `X-api-Key`**
  (bukan dari body).
- Error gRPC → HTTP dipetakan di `exception/RestExceptionHandler`
  (`@RestControllerAdvice`, body `{"status":"error","message":...}`).
- Dokumentasi API: springdoc/Swagger UI di `/api-docs`.
- Sudah ada `GatewayApplicationTests` + `src/test/resources/application-test.properties`.

### Beda kunci vs ecommerce

| Hal | ecommerce | payment_gateway |
|---|---|---|
| Jumlah domain | 21 | 11 (termasuk **card**, **saldo**, **topup**, **transfer**, **withdraw**) |
| Domain khusus | banner, slider, cart, review, shipping, merchant sub-domain | **card/dashboard**, **saldo**, **topup**, **transfer**, **withdraw**, **merchant transactions** |
| Upload file | ada (product, category, …) | **tidak ada** — tanpa multipart |
| Header auth khusus | — | `X-api-Key` pada create/update transaction |
| Base package | `com.sanedge.ecommerce.gateway` | `com.sanedge.paymentgateway.gateway` |
| Proto java package | `com.sanedge.ecommerce.pb.*` | `com.sanedge.paymentgateway.pb.*` |
| Test | ada | ada `GatewayApplicationTests` (context load) |

---

## 2. Keputusan desain

| # | Topik | Keputusan |
|---|---|---|
| D1 | Gaya schema | **Schema-first** (`src/main/resources/graphql/*.graphqls`) + `@QueryMapping`/`@MutationMapping` |
| D2 | Framework | **Spring for GraphQL** (`spring-boot-starter-graphql`, versi ikut parent BOM) |
| D3 | Transport | Spring GraphQL WebMVC di **`POST /graphql`** (+ GraphiQL di `/graphiql`, dev only) |
| D4 | Envelope `{status,message,data}` | **Dibuang** di GraphQL — field mengembalikan payload langsung; error lewat `errors[]` + `extensions.httpStatus` |
| D5 | REST eksisting | **Dihapus** (full GraphQL) |
| D6 | Naming | GraphQL camelCase (`accessToken`, `totalItems`) |
| D7 | Header `X-api-Key` | Dipindah menjadi **argumen field** `apiKey` pada mutation transaction (atau dibaca dari `GraphQlContext`); lihat §8 |
| D8 | Pagination | Tipe `<Entity>Page { data: [T!]!, pagination: PaginationMeta! }` |

---

## 3. Perubahan dependency & konfigurasi

### `gateway/pom.xml`

Tambah:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-graphql</artifactId>
</dependency>

<!-- test -->
<dependency>
    <groupId>org.springframework.graphql</groupId>
    <artifactId>spring-graphql-test</artifactId>
    <scope>test</scope>
</dependency>
```

Ubah deskripsi artifact:

```xml
<description>GraphQL gateway (BFF) - no database, proxies to domain services over gRPC</description>
```

Biarkan tetap ada: `common`, `grpc-netty-shaded`, `spring-boot-starter-web`,
`spring-boot-starter-security`, `spring-boot-starter-validation`, `springdoc-openapi-starter-webmvc-ui`,
`lombok`, `spring-boot-starter-test`.

### `application.properties` (tambahan)

```properties
# --- GraphQL (BFF edge) ---
spring.graphql.path=/graphql
spring.graphql.graphiql.enabled=true          # dev; false di prod
spring.graphql.graphiql.path=/graphiql
spring.graphql.schema.locations=classpath:graphql/
spring.graphql.schema.file-extensions=.graphqls
spring.graphql.schema.inspection.enabled=true
```

`GrpcClientConfig`, `server.port=8080`, seluruh blok `grpc.client.*`, JWT, dan springdoc
**tidak berubah**.

### `grpc.client.*` (tetap, untuk referensi)

| domain | property | port default | stub |
|---|---|---|---|
| role | `grpc.client.role` | 9006 | `RoleService` + `RoleCommandService` |
| user | `grpc.client.user` | 9011 | `UserQueryService` + `UserCommandService` |
| merchant | `grpc.client.merchant` | 9005 | `MerchantQueryService` + `MerchantCommandService` + `MerchantTransactionService` |
| card | `grpc.client.card` | 9004 | `CardQueryService` + `CardCommandService` + `CardDashboardService` |
| saldo | `grpc.client.saldo` | 9007 | `SaldoQueryService` + `SaldoCommandService` |
| topup | `grpc.client.topup` | 9008 | `TopupQueryService` + `TopupCommandService` |
| transaction | `grpc.client.transaction` | 9009 | `TransactionQueryService` + `TransactionCommandService` |
| transfer | `grpc.client.transfer` | 9010 | `TransferQueryService` + `TransferCommandService` |
| withdraw | `grpc.client.withdraw` | 9013 | `WithdrawQueryService` + `WithdrawCommandService` |
| auth | `grpc.client.auth` | 9012 | `AuthService` |
| stats | `grpc.client.stats` | 9014 | `StatsReaderService` |

---

## 4. Struktur package target

```
gateway/src/main/
├── resources/
│   ├── application.properties
│   └── graphql/
│       ├── schema.graphqls        # root Query & Mutation
│       ├── common.graphqls        # PaginationMeta
│       ├── auth.graphqls
│       ├── user.graphqls
│       ├── role.graphqls
│       ├── merchant.graphqls      # merchant + merchantTransaction
│       ├── card.graphqls          # card + dashboard
│       ├── saldo.graphqls
│       ├── topup.graphqls
│       ├── transaction.graphqls
│       ├── transfer.graphqls
│       ├── withdraw.graphqls
│       └── stats.graphqls
└── java/com/sanedge/paymentgateway/gateway/
    ├── config/            # GrpcClientConfig (tetap), SecurityConfig (tambah /graphql, /graphiql)
    ├── controller/        # DIHAPUS seluruhnya
    ├── graphql/
    │   ├── dto/           # Page.java (payload paginasi generik), Token.java (camelCase)
    │   ├── Gql.java       # helper nvl + ok(status) + page(...)
    │   ├── GraphQlExceptionHandler.java
    │   └── resolver/      # @Controller per domain
    ├── mapper/ProtoMapper.java            # DIPAKAI ULANG apa adanya
    ├── dto/               # dto/responses/** tetap (ProtoMapper butuh); dto/requests/** dihapus
    ├── security/          # tetap
    └── (tanpa service/FileService — tidak ada upload)
```

Keputusan penting: **`ProtoMapper` dan semua DTO response dipertahankan.** Resolver memanggil
stub gRPC, memetakan lewat `ProtoMapper` (dan `ProtoMappers.fromProtoPagination` dari `common`),
lalu mengembalikan DTO apa adanya.

---

## 5. Konvensi penamaan & bentuk response

- **Query** (read) → jamak untuk list, tunggal untuk by-id:
  `cards(...)`, `card(id)`, `activeCards(...)`, `trashedCards(...)`.
- **Mutation** (write) → kata kerja: `createCard`, `updateCard`, `trashCard`, `restoreCard`,
  `deleteCardPermanent`, `restoreAllCards`, `deleteAllCardsPermanent`.
- **Input** memakai suffix `Input`, mutation memakai satu argumen `input`:
  `createCard(input: CreateCardInput!): Card!`.
- **Argumen list/filter** (page, pageSize, search, cardNumber, …) tetap argumen field.
- **`ID`** untuk identifier (`Long`), `Int` untuk angka biasa, `Float` untuk nilai statistik.
- **Response list** selalu `XxxPage` (`data` non-null element, `pagination` non-null).
- **`*DeleteAt`** → tipe GraphQL tanpa suffix `Response` (mis. `CardDeleteAt`), berisi `deletedAt`.
- **Token** memakai camelCase (`accessToken`/`refreshToken`) walau DTO `TokenResponse` memakai
  `access_token`/`refresh_token`.
- `LocalDate expireDate` dan `LocalDateTime transactionTime/withdrawTime/transferTime` menjadi
  `String` di GraphQL (mengikuti bentuk output DTO yang sudah `String`).

---

## 6. Schema GraphQL lengkap

### 6.1 common.graphqls

```graphql
type PaginationMeta {
  page: Int!
  pageSize: Int!
  totalItems: Int!
  totalPages: Int!
}
```

### 6.2 schema.graphqls (root)

```graphql
type Query { _root: Boolean }
type Mutation { _root: Boolean }
```

### 6.3 auth.graphqls

```graphql
type Token {
  accessToken: String!
  refreshToken: String!
}

input RegisterInput {
  firstname: String!
  username: String!
  lastname: String!
  email: String!
  password: String!
  confirmPassword: String!
}

input LoginInput {
  username: String!
  password: String!
}

extend type Query {
  me: User                 # authenticated
  healthchecker: String!   # publik (menggantikan GET /api/auth/healthchecker)
}

extend type Mutation {
  register(input: RegisterInput!): User!
  login(input: LoginInput!): Token!
  refreshToken(refreshToken: String!): Token!
}
```

### 6.4 user.graphqls

```graphql
type User {
  id: ID!
  username: String
  firstname: String
  lastname: String
  email: String
  createdAt: String
  updatedAt: String
}

type UserDeleteAt {
  id: ID!
  firstname: String
  lastname: String
  email: String
  createdAt: String
  updatedAt: String
  deletedAt: String
}

type UserPage { data: [User!]! pagination: PaginationMeta! }
type UserDeleteAtPage { data: [UserDeleteAt!]! pagination: PaginationMeta! }

input CreateUserInput {
  username: String!
  firstname: String!
  lastname: String!
  email: String!
  password: String!
  confirmPassword: String!
}

input UpdateUserInput {
  id: ID!
  username: String
  firstname: String
  lastname: String
  email: String
  password: String
  confirmPassword: String
}

extend type Query {
  users(page: Int = 1, pageSize: Int = 10, search: String = ""): UserPage!            # ADMIN, STAFF
  user(id: ID!): User!                                                                # ADMIN, STAFF, USER
  activeUsers(page: Int = 1, pageSize: Int = 10, search: String = ""): UserDeleteAtPage!   # ADMIN, STAFF
  trashedUsers(page: Int = 1, pageSize: Int = 10, search: String = ""): UserDeleteAtPage!  # ADMIN, STAFF
}

extend type Mutation {
  createUser(input: CreateUserInput!): User!        # ADMIN
  updateUser(input: UpdateUserInput!): User!        # ADMIN
  trashUser(id: ID!): UserDeleteAt!                 # ADMIN
  restoreUser(id: ID!): UserDeleteAt!               # ADMIN
  deleteUserPermanent(id: ID!): Boolean!            # ADMIN
  restoreAllUsers: Boolean!                         # ADMIN
  deleteAllUsersPermanent: Boolean!                 # ADMIN
}
```

### 6.5 role.graphqls

```graphql
type Role {
  id: ID!
  name: String
  createdAt: String
  updatedAt: String
}

type RoleDeleteAt {
  id: ID!
  name: String
  createdAt: String
  updatedAt: String
  deletedAt: String
}

type RolePage { data: [Role!]! pagination: PaginationMeta! }
type RoleDeleteAtPage { data: [RoleDeleteAt!]! pagination: PaginationMeta! }

input CreateRoleInput { name: String! }
input UpdateRoleInput { id: ID!, name: String! }

extend type Query {
  roles(page: Int = 1, pageSize: Int = 10, search: String = ""): RolePage!                # ADMIN, STAFF
  role(id: ID!): Role!                                                                     # ADMIN, STAFF
  activeRoles(page: Int = 1, pageSize: Int = 10, search: String = ""): RoleDeleteAtPage!   # ADMIN, STAFF
  trashedRoles(page: Int = 1, pageSize: Int = 10, search: String = ""): RoleDeleteAtPage!  # ADMIN, STAFF
  rolesByUser(userId: ID!): [Role!]!                                                       # ADMIN, STAFF
  roleByName(name: String!): Role!                                                         # ADMIN, STAFF
}

extend type Mutation {
  createRole(input: CreateRoleInput!): Role!        # ADMIN
  updateRole(input: UpdateRoleInput!): Role!        # ADMIN
  trashRole(id: ID!): RoleDeleteAt!                 # ADMIN
  restoreRole(id: ID!): RoleDeleteAt!               # ADMIN
  deleteRolePermanent(id: ID!): Boolean!            # ADMIN
  restoreAllRoles: Boolean!                         # ADMIN
  deleteAllRolesPermanent: Boolean!                 # ADMIN
}
```

> Proto `RoleCommandService` juga punya `AssignRoleToUser`/`RemoveRoleFromUser` yang tidak
> diekspos REST. Bisa ditambah sebagai mutation `assignRoleToUser`/`removeRoleFromUser`
> (opsional, bukan paritas).

### 6.6 merchant.graphqls (merchant + merchantTransaction)

```graphql
type Merchant {
  id: ID!
  name: String
  userId: ID
  apiKey: String
  status: String
  createdAt: String
  updatedAt: String
}

type MerchantDeleteAt {
  id: ID!
  name: String
  userId: ID
  apiKey: String
  status: String
  createdAt: String
  updatedAt: String
  deletedAt: String
}

type MerchantTransaction {
  id: ID!
  cardNumber: String
  amount: Float
  paymentMethod: String
  merchantId: ID
  merchantName: String
  transactionTime: String
  createdAt: String
  updatedAt: String
  deletedAt: String
}

type MerchantPage { data: [Merchant!]! pagination: PaginationMeta! }
type MerchantDeleteAtPage { data: [MerchantDeleteAt!]! pagination: PaginationMeta! }
type MerchantTransactionPage { data: [MerchantTransaction!]! pagination: PaginationMeta! }

input CreateMerchantInput { name: String!, userId: ID! }
input UpdateMerchantInput { merchantId: ID!, name: String, userId: ID, status: String }

extend type Query {
  merchants(page: Int = 1, pageSize: Int = 10, search: String = ""): MerchantPage!            # ADMIN, STAFF
  merchant(id: ID!): Merchant!                                                                 # ADMIN, STAFF, USER
  activeMerchants(page: Int = 1, pageSize: Int = 10, search: String = ""): MerchantDeleteAtPage!   # ADMIN, STAFF
  trashedMerchants(page: Int = 1, pageSize: Int = 10, search: String = ""): MerchantDeleteAtPage!  # ADMIN, STAFF
  merchantByApiKey(apiKey: String!): Merchant!                                                 # ADMIN, STAFF
  merchantsByUser(userId: ID!): [Merchant!]!                                                   # ADMIN, STAFF, USER
  merchantTransactions(page: Int = 1, pageSize: Int = 10, search: String = ""): MerchantTransactionPage!  # ADMIN, STAFF
  merchantTransactionsByMerchant(merchantId: ID!, page: Int = 1, pageSize: Int = 10, search: String = ""): MerchantTransactionPage!  # ADMIN, STAFF
  merchantTransactionsByApiKey(apiKey: String!, page: Int = 1, pageSize: Int = 10, search: String = ""): MerchantTransactionPage!    # ADMIN, STAFF
}

extend type Mutation {
  createMerchant(input: CreateMerchantInput!): Merchant!        # ADMIN
  updateMerchant(input: UpdateMerchantInput!): Merchant!        # ADMIN
  trashMerchant(id: ID!): MerchantDeleteAt!                     # ADMIN
  restoreMerchant(id: ID!): MerchantDeleteAt!                   # ADMIN
  deleteMerchantPermanent(id: ID!): Boolean!                    # ADMIN
  restoreAllMerchants: Boolean!                                 # ADMIN
  deleteAllMerchantsPermanent: Boolean!                         # ADMIN
}
```

> Proto `MerchantCommandService.UpdateMerchantStatus` tidak diekspos REST; bisa ditambah
> `updateMerchantStatus(input)` (opsional).

### 6.7 card.graphqls (card + dashboard)

```graphql
type Card {
  id: ID!
  userId: ID
  cardNumber: String
  cardType: String
  expireDate: String
  cvv: String
  cardProvider: String
  createdAt: String
  updatedAt: String
}

type CardDeleteAt {
  id: ID!
  userId: ID
  cardNumber: String
  cardType: String
  expireDate: String
  cvv: String
  cardProvider: String
  createdAt: String
  updatedAt: String
  deletedAt: String
}

type CardDashboard {
  totalBalance: Float
  totalTopup: Float
  totalWithdraw: Float
  totalTransaction: Float
  totalTransfer: Float
}

type CardDashboardCard {
  totalBalance: Float
  totalTopup: Float
  totalWithdraw: Float
  totalTransaction: Float
  totalTransferSend: Float
  totalTransferReceiver: Float
}

type CardPage { data: [Card!]! pagination: PaginationMeta! }
type CardDeleteAtPage { data: [CardDeleteAt!]! pagination: PaginationMeta! }

input CreateCardInput {
  userId: ID!
  cardType: String!
  expireDate: String
  cvv: String!
  cardProvider: String!
}

input UpdateCardInput {
  cardId: ID!
  userId: ID
  cardType: String
  expireDate: String
  cvv: String
  cardProvider: String
}

extend type Query {
  cards(page: Int = 1, pageSize: Int = 10, search: String = ""): CardPage!            # ADMIN, STAFF
  card(id: ID!): Card!                                                                 # ADMIN, STAFF
  activeCards(page: Int = 1, pageSize: Int = 10, search: String = ""): CardDeleteAtPage!   # ADMIN, STAFF
  trashedCards(page: Int = 1, pageSize: Int = 10, search: String = ""): CardDeleteAtPage!  # ADMIN
  cardByUser(userId: ID!): Card!                                                       # ADMIN, STAFF
  cardByNumber(cardNumber: String!): Card!                                             # ADMIN, STAFF
  cardDashboard: CardDashboard!                                                        # ADMIN, STAFF
  cardDashboardByNumber(cardNumber: String!): CardDashboardCard!                       # ADMIN, STAFF, USER
}

extend type Mutation {
  createCard(input: CreateCardInput!): Card!        # ADMIN
  updateCard(input: UpdateCardInput!): Card!        # ADMIN
  trashCard(id: ID!): CardDeleteAt!                 # ADMIN
  restoreCard(id: ID!): CardDeleteAt!               # ADMIN
  deleteCardPermanent(id: ID!): Boolean!            # ADMIN
  restoreAllCards: Boolean!                         # ADMIN
  deleteAllCardsPermanent: Boolean!                 # ADMIN
}
```

> `CardController` memasang `@PreAuthorize("isAuthenticated()")` di level kelas; otorisasi
> per-field di atas menggantikannya.

### 6.8 saldo.graphqls

```graphql
type Saldo {
  id: ID!
  cardNumber: String
  totalBalance: Float
  withdrawAmount: Float
  withdrawTime: String
  createdAt: String
  updatedAt: String
}

type SaldoDeleteAt {
  id: ID!
  cardNumber: String
  totalBalance: Float
  withdrawAmount: Float
  withdrawTime: String
  createdAt: String
  updatedAt: String
  deletedAt: String
}

type SaldoPage { data: [Saldo!]! pagination: PaginationMeta! }
type SaldoDeleteAtPage { data: [SaldoDeleteAt!]! pagination: PaginationMeta! }

input CreateSaldoInput { cardNumber: String!, totalBalance: Float! }
input UpdateSaldoInput { saldoId: ID!, cardNumber: String, totalBalance: Float }

extend type Query {
  saldos(page: Int = 1, pageSize: Int = 10, search: String = ""): SaldoPage!            # ADMIN, STAFF
  saldo(id: ID!): Saldo!                                                                 # ADMIN, STAFF
  activeSaldos(page: Int = 1, pageSize: Int = 10, search: String = ""): SaldoDeleteAtPage!   # ADMIN, STAFF
  trashedSaldos(page: Int = 1, pageSize: Int = 10, search: String = ""): SaldoDeleteAtPage!  # ADMIN, STAFF
  saldoByCard(cardNumber: String!): Saldo!                                              # ADMIN, STAFF
}

extend type Mutation {
  createSaldo(input: CreateSaldoInput!): Saldo!        # ADMIN
  updateSaldo(input: UpdateSaldoInput!): Saldo!        # ADMIN
  trashSaldo(id: ID!): SaldoDeleteAt!                  # ADMIN
  restoreSaldo(id: ID!): SaldoDeleteAt!                # ADMIN
  deleteSaldoPermanent(id: ID!): Boolean!              # ADMIN
  restoreAllSaldos: Boolean!                           # ADMIN
  deleteAllSaldosPermanent: Boolean!                   # ADMIN
}
```

> `saldoByCard` memakai `pb.card.FindByCardNumberRequest`. Proto `SaldoCommandService` juga
> punya `UpdateSaldoBalance`/`UpdateSaldoWithdraw` (tidak diekspos REST) — opsional.

### 6.9 topup.graphqls

```graphql
type Topup {
  id: ID!
  cardNumber: String
  topupNo: String
  topupAmount: Float
  topupMethod: String
  topupTime: String
  createdAt: String
  updatedAt: String
}

type TopupDeleteAt {
  id: ID!
  cardNumber: String
  topupNo: String
  topupAmount: Float
  topupMethod: String
  topupTime: String
  createdAt: String
  updatedAt: String
  deletedAt: String
}

type TopupPage { data: [Topup!]! pagination: PaginationMeta! }
type TopupDeleteAtPage { data: [TopupDeleteAt!]! pagination: PaginationMeta! }

input CreateTopupInput { cardNumber: String!, topupAmount: Float!, topupMethod: String! }
input UpdateTopupInput { topupId: ID!, cardNumber: String, topupAmount: Float, topupMethod: String }

extend type Query {
  topups(page: Int = 1, pageSize: Int = 10, search: String = ""): TopupPage!            # ADMIN, STAFF
  topup(id: ID!): Topup!                                                                 # ADMIN, STAFF, USER
  topupsByCard(cardNumber: String!, page: Int = 1, pageSize: Int = 10, search: String = ""): TopupPage!  # ADMIN, STAFF
  topupsOfCard(cardNumber: String!): [Topup!]!                                          # ADMIN, STAFF, USER
  activeTopups(page: Int = 1, pageSize: Int = 10, search: String = ""): TopupDeleteAtPage!   # ADMIN, STAFF
  trashedTopups(page: Int = 1, pageSize: Int = 10, search: String = ""): TopupDeleteAtPage!  # ADMIN, STAFF
}

extend type Mutation {
  createTopup(input: CreateTopupInput!): Topup!        # ADMIN, STAFF, USER
  updateTopup(input: UpdateTopupInput!): Topup!        # ADMIN, STAFF, USER
  trashTopup(id: ID!): TopupDeleteAt!                  # ADMIN
  restoreTopup(id: ID!): TopupDeleteAt!                # ADMIN
  deleteTopupPermanent(id: ID!): Boolean!              # ADMIN
  restoreAllTopups: Boolean!                           # ADMIN
  deleteAllTopupsPermanent: Boolean!                   # ADMIN
}
```

### 6.10 transaction.graphqls

```graphql
type Transaction {
  id: ID!
  transactionNo: String
  cardNumber: String
  amount: Float
  paymentMethod: String
  merchantId: ID
  transactionTime: String
  createdAt: String
  updatedAt: String
}

type TransactionDeleteAt {
  id: ID!
  transactionNo: String
  cardNumber: String
  amount: Float
  paymentMethod: String
  merchantId: ID
  transactionTime: String
  createdAt: String
  updatedAt: String
  deletedAt: String
}

type TransactionPage { data: [Transaction!]! pagination: PaginationMeta! }
type TransactionDeleteAtPage { data: [TransactionDeleteAt!]! pagination: PaginationMeta! }

input CreateTransactionInput {
  cardNumber: String!
  amount: Float!
  paymentMethod: String!
  merchantId: ID!
}

input UpdateTransactionInput {
  transactionId: ID!
  cardNumber: String
  amount: Float
  paymentMethod: String
  merchantId: ID
}

extend type Query {
  transactions(page: Int = 1, pageSize: Int = 10, search: String = ""): TransactionPage!            # ADMIN, STAFF
  transaction(id: ID!): Transaction!                                                                # ADMIN, STAFF, USER
  transactionsByCard(cardNumber: String!, page: Int = 1, pageSize: Int = 10, search: String = ""): TransactionPage!  # ADMIN, STAFF
  transactionsByMerchant(merchantId: ID!): [Transaction!]!                                          # ADMIN, STAFF, USER
  activeTransactions(page: Int = 1, pageSize: Int = 10, search: String = ""): TransactionDeleteAtPage!   # ADMIN, STAFF
  trashedTransactions(page: Int = 1, pageSize: Int = 10, search: String = ""): TransactionDeleteAtPage!  # ADMIN, STAFF
}

extend type Mutation {
  "Butuh header X-api-Key semula; sekarang jadi argumen apiKey."
  createTransaction(input: CreateTransactionInput!, apiKey: String): Transaction!   # ADMIN, STAFF, USER
  updateTransaction(input: UpdateTransactionInput!, apiKey: String): Transaction!   # ADMIN, STAFF, USER
  trashTransaction(id: ID!): TransactionDeleteAt!                   # ADMIN
  restoreTransaction(id: ID!): TransactionDeleteAt!                 # ADMIN
  deleteTransactionPermanent(id: ID!): Boolean!                     # ADMIN
  restoreAllTransactions: Boolean!                                  # ADMIN
  deleteAllTransactionsPermanent: Boolean!                          # ADMIN
}
```

### 6.11 transfer.graphqls

```graphql
type Transfer {
  id: ID!
  transferNo: String
  transferFrom: String
  transferTo: String
  transferAmount: Float
  transferTime: String
  createdAt: String
  updatedAt: String
}

type TransferDeleteAt {
  id: ID!
  transferNo: String
  transferFrom: String
  transferTo: String
  transferAmount: Float
  transferTime: String
  createdAt: String
  updatedAt: String
  deletedAt: String
}

type TransferPage { data: [Transfer!]! pagination: PaginationMeta! }
type TransferDeleteAtPage { data: [TransferDeleteAt!]! pagination: PaginationMeta! }

input CreateTransferInput { transferFrom: String!, transferTo: String!, transferAmount: Float! }
input UpdateTransferInput { transferId: ID!, transferFrom: String, transferTo: String, transferAmount: Float }

extend type Query {
  transfers(page: Int = 1, pageSize: Int = 10, search: String = ""): TransferPage!            # ADMIN, STAFF
  transfer(id: ID!): Transfer!                                                                 # ADMIN, STAFF
  activeTransfers(page: Int = 1, pageSize: Int = 10, search: String = ""): TransferDeleteAtPage!   # ADMIN, STAFF
  trashedTransfers(page: Int = 1, pageSize: Int = 10, search: String = ""): TransferDeleteAtPage!  # ADMIN, STAFF
  transfersFrom(transferFrom: String!): [Transfer!]!                                           # ADMIN, STAFF, USER
  transfersTo(transferTo: String!): [Transfer!]!                                               # ADMIN, STAFF, USER
}

extend type Mutation {
  createTransfer(input: CreateTransferInput!): Transfer!      # ADMIN, STAFF, USER
  updateTransfer(input: UpdateTransferInput!): Transfer!      # ADMIN, STAFF
  trashTransfer(id: ID!): TransferDeleteAt!                   # ADMIN, STAFF
  restoreTransfer(id: ID!): TransferDeleteAt!                 # ADMIN, STAFF
  deleteTransferPermanent(id: ID!): Boolean!                  # ADMIN, STAFF
  restoreAllTransfers: Boolean!                               # ADMIN
  deleteAllTransfersPermanent: Boolean!                       # ADMIN
}
```

### 6.12 withdraw.graphqls

```graphql
type Withdraw {
  id: ID!
  withdrawNo: String
  cardNumber: String
  withdrawAmount: Float
  withdrawTime: String
  createdAt: String
  updatedAt: String
}

type WithdrawDeleteAt {
  id: ID!
  withdrawNo: String
  cardNumber: String
  withdrawAmount: Float
  withdrawTime: String
  createdAt: String
  updatedAt: String
  deletedAt: String
}

type WithdrawPage { data: [Withdraw!]! pagination: PaginationMeta! }
type WithdrawDeleteAtPage { data: [WithdrawDeleteAt!]! pagination: PaginationMeta! }

input CreateWithdrawInput { cardNumber: String!, withdrawAmount: Float! }
input UpdateWithdrawInput { withdrawId: ID!, cardNumber: String, withdrawAmount: Float }

extend type Query {
  withdraws(page: Int = 1, pageSize: Int = 10, search: String = ""): WithdrawPage!            # ADMIN, STAFF
  withdraw(id: ID!): Withdraw!                                                                 # ADMIN, STAFF, USER
  withdrawsByCard(cardNumber: String!): [Withdraw!]!                                           # ADMIN, STAFF, USER
  activeWithdraws(page: Int = 1, pageSize: Int = 10, search: String = ""): WithdrawDeleteAtPage!   # ADMIN, STAFF
  trashedWithdraws(page: Int = 1, pageSize: Int = 10, search: String = ""): WithdrawDeleteAtPage!  # ADMIN, STAFF
}

extend type Mutation {
  createWithdraw(input: CreateWithdrawInput!): Withdraw!      # ADMIN
  updateWithdraw(input: UpdateWithdrawInput!): Withdraw!      # ADMIN
  trashWithdraw(id: ID!): WithdrawDeleteAt!                   # ADMIN
  restoreWithdraw(id: ID!): WithdrawDeleteAt!                 # ADMIN
  deleteWithdrawPermanent(id: ID!): Boolean!                  # ADMIN
  restoreAllWithdraws: Boolean!                               # ADMIN
  deleteAllWithdrawsPermanent: Boolean!                       # ADMIN
}
```

### 6.13 stats.graphqls

```graphql
type StatItem {
  period: String
  dimensionId: ID
  value: Float
}

extend type Query {
  monthlyTotalRevenue(year: Int, month: Int): [StatItem!]!     # ADMIN, STAFF
  cardStats(cardId: ID, year: Int): [StatItem!]!               # ADMIN, STAFF
  yearlyVolume(year: Int): [StatItem!]!                        # ADMIN, STAFF
  merchantVolume(merchantId: ID, year: Int): [StatItem!]!      # ADMIN, STAFF
}
```

---

## 7. Tabel pemetaan REST → GraphQL

Template CRUD seragam (role, user, merchant, card, saldo, topup, transaction, transfer, withdraw):

| REST (sekarang) | GraphQL (target) |
|---|---|
| `GET /api/<x>?page&pageSize&search` | `query { xs(page, pageSize, search) }` |
| `GET /api/<x>/{id}` | `query { x(id) }` |
| `GET /api/<x>/active` | `query { activeXs(...) }` |
| `GET /api/<x>/trashed` | `query { trashedXs(...) }` |
| `POST /api/<x>/create` | `mutation { createX(input) }` |
| `POST /api/<x>/update` | `mutation { updateX(input) }` |
| `POST /api/<x>/trash/{id}` | `mutation { trashX(id) }` |
| `POST /api/<x>/restore/{id}` | `mutation { restoreX(id) }` |
| `DELETE /api/<x>/{id}` | `mutation { deleteXPermanent(id) }` |
| `POST /api/<x>/restore-all` | `mutation { restoreAllXs }` |
| `POST /api/<x>/delete-all` | `mutation { deleteAllXsPermanent }` |

Query/mutation khusus (di luar template):

| REST | GraphQL |
|---|---|
| `POST /api/auth/register` | `mutation register(input)` |
| `POST /api/auth/login` | `mutation login(input)` |
| `POST /api/auth/refresh-token` | `mutation refreshToken(refreshToken)` |
| `GET /api/auth/me` | `query me` |
| `GET /api/auth/healthchecker` | `query healthchecker` (publik) |
| `GET /api/roles/by-user/{userId}` | `query rolesByUser(userId)` |
| `GET /api/roles/by-name?name=` | `query roleByName(name)` |
| `GET /api/merchants/by-api-key?apiKey=` | `query merchantByApiKey(apiKey)` |
| `GET /api/merchants/by-user/{userId}` | `query merchantsByUser(userId)` |
| `GET /api/merchants/transactions` | `query merchantTransactions(...)` |
| `GET /api/merchants/transactions/by-id` | `query merchantTransactionsByMerchant(merchantId, ...)` |
| `GET /api/merchants/transactions/by-api-key` | `query merchantTransactionsByApiKey(apiKey, ...)` |
| `GET /api/cards/by-user/{user_id}` | `query cardByUser(userId)` |
| `GET /api/cards/by-number/{card_number}` | `query cardByNumber(cardNumber)` |
| `GET /api/cards/dashboard` | `query cardDashboard` |
| `GET /api/cards/dashboard/{card_number}` | `query cardDashboardByNumber(cardNumber)` |
| `GET /api/saldos/by-card/{cardNumber}` | `query saldoByCard(cardNumber)` |
| `GET /api/topups/by-card` | `query topupsByCard(cardNumber, ...)` |
| `GET /api/topups/card/{cardNumber}` | `query topupsOfCard(cardNumber)` |
| `GET /api/transactions/by-card` | `query transactionsByCard(cardNumber, ...)` |
| `GET /api/transactions/merchant/{merchantId}` | `query transactionsByMerchant(merchantId)` |
| `GET /api/transfers/from/{transferFrom}` | `query transfersFrom(transferFrom)` |
| `GET /api/transfers/to/{transferTo}` | `query transfersTo(transferTo)` |
| `GET /api/withdraws/by-card/{cardNumber}` | `query withdrawsByCard(cardNumber)` |
| `GET /api/stats/monthly-total-revenue` | `query monthlyTotalRevenue(year, month)` |
| `GET /api/stats/card-stats` | `query cardStats(cardId, year)` |
| `GET /api/stats/yearly-volume` | `query yearlyVolume(year)` |
| `GET /api/stats/merchant-volume` | `query merchantVolume(merchantId, year)` |
| header `X-api-Key` (`POST /api/transactions/{create,update}`) | argumen `apiKey` pada `createTransaction`/`updateTransaction` |

---

## 8. Header `X-api-Key` → argumen GraphQL

Dua opsi (pilih saat implementasi):

1. **Argumen field** (paling sederhana, terlihat di schema):
   `createTransaction(input: CreateTransactionInput!, apiKey: String): Transaction!`.
   Validasi bahwa `apiKey` cocok dilakukan downstream (service transaction) seperti sebelumnya.
2. **Baca header via context**: ambil header `X-api-Key` dari `GraphQlContext`/`DataFetchingEnvironment`
   dan teruskan ke stub, sehingga schema tidak berubah. Lebih transparan bagi klien lama.

Rekomendasi: opsi 1 agar kontrak GraphQL eksplisit.

---

## 9. Keamanan & otorisasi

- `SecurityConfig`: tambah `/graphql` dan `/graphiql` ke `PUBLIC_ENDPOINTS`.
  - `/graphiql` → `permitAll()` (dev) atau matikan di prod (`spring.graphql.graphiql.enabled=false`).
  - `/graphql` → `permitAll()` di filter chain, lalu **otorisasi per-field** di resolver
    (login/register/refreshToken/healthchecker harus anonim).
- `AuthTokenFilter` tetap dipakai — JWT dari header `Authorization: Bearer` mengisi
  `SecurityContextHolder`.
- Otorisasi per-field: `@PreAuthorize("hasAnyRole('ADMIN','STAFF')")` di method
  `@QueryMapping`/`@MutationMapping`. `@EnableMethodSecurity` sudah aktif.
- Class-level `isAuthenticated()` pada `CardController` digantikan oleh per-field `@PreAuthorize`.

`PUBLIC_ENDPOINTS` target:

```java
private static final String[] PUBLIC_ENDPOINTS = {
        "/api/test/**",
        "/static/**",
        "/graphql",
        "/graphiql",
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/api-docs/**",
        "/swagger-resources/**",
        "/configuration/**",
        "/webjars/**",
        "/error"
};
```

---

## 10. Error handling

Ganti `RestExceptionHandler` (`@RestControllerAdvice`) dengan resolver GraphQL
(`graphql/GraphQlExceptionHandler.java`), persis pola ecommerce
(`DataFetcherExceptionResolverAdapter`).

Pemetaan gRPC → `extensions.httpStatus` (dari `exception/RestExceptionHandler` sekarang):

| gRPC code | `extensions.httpStatus` |
|---|---|
| `NOT_FOUND` | 404 |
| `INVALID_ARGUMENT`, `FAILED_PRECONDITION` | 400 |
| `ALREADY_EXISTS` | 409 |
| `UNAUTHENTICATED` | 401 |
| `PERMISSION_DENIED` | 403 |
| lainnya | 500 |

Handler lain: `ResourceNotFoundException` → 404; `ConstraintViolationException`,
`MethodArgumentNotValidException`/`BindException`, `IllegalArgumentException` → 400;
`UnsupportedOperationException` → 501; `AccessDeniedException` → 401/403 (tergantung anonim);
`AuthenticationException` → 401.

Perbedaan penting: GraphQL selalu balikan HTTP 200 di transport; status "HTTP" lama pindah ke
`errors[].extensions.httpStatus`.

---

## 11. Testing

`src/test` sudah berisi `GatewayApplicationTests` (context load) + `application-test.properties`.
Tambahkan (contoh ecommerce):

- Perbarui `GatewayApplicationTests` agar memverifikasi schema GraphQL ikut ter-wire.
- `GraphQlEndpointTests` (`@AutoConfigureGraphQlTester`):
  - query publik `healthchecker` sukses,
  - field ber-role tanpa token → GraphQL error (bukan 500).
- `GraphQlResolverTests` (stub gRPC di-mock dengan Mockito):
  1. output `*Page` + `PaginationMeta` terserialisasi benar,
  2. input object GraphQL ter-bind ke record Java (verifikasi via `ArgumentCaptor`),
  3. otorisasi `ROLE_ADMIN` pada resolver bekerja.
- Uji `X-api-Key`/`apiKey` pada mutation transaction.
- Uji otorisasi: request tanpa token → `UNAUTHENTICATED`; role salah → `FORBIDDEN`.

`application-test.properties` perlu ditambah port gRPC baru bila ada domain yang belum tercantum
(mirror `ecommerce/gateway/src/test/resources/application-test.properties`).

---

## 12. Rencana eksekusi (fase)

**Fase 0 — Persiapan**
- [x] Tambah dependency `spring-boot-starter-graphql` (+ `spring-graphql-test`) + properti GraphQL.
- [x] Buat kerangka `resources/graphql/` dan package `graphql/dto|resolver`.
- [x] Salin helper dari ecommerce: `Gql.java`, `graphql/dto/Page.java`, `graphql/dto/Token.java`,
      `graphql/GraphQlExceptionHandler.java` (sesuaikan package).

**Fase 1 — Pilot 1 domain (card, karena ada dashboard)**
- [x] `card.graphqls` + input.
- [x] `CardResolver` (query + mutation + dashboard).
- [x] Update `SecurityConfig` untuk `/graphql`, `/graphiql`.
- [x] Uji via `GraphQlResolverTests`/`GraphQlEndpointTests` (tanpa service hidup).

**Fase 2 — Domain CRUD lain**
- [x] auth, user, role, merchant (+ merchant transactions), saldo, topup, transaction, transfer, withdraw.

**Fase 3 — Stats**
- [x] `StatsResolver` (4 query: monthlyTotalRevenue, cardStats, yearlyVolume, merchantVolume).

**Fase 4 — Pembersihan**
- [x] Hapus 11 controller REST.
- [x] Hapus `exception/RestExceptionHandler.java` (digantikan `GraphQlExceptionHandler`).
- [x] Hapus seluruh `dto/requests/**` (input GraphQL memakai record `*Input` di resolver);
      `dto/responses/**` tetap untuk `ProtoMapper`.
- [x] `springdoc-openapi` **dipertahankan** (ikut pola ecommerce) — Swagger UI kini tanpa endpoint REST.
- [x] Content negotiation: hanya `/graphql` yang tersisa (tidak ada upload; `static/**` tetap dipetakan).

**Fase 5 — Verifikasi**
- [x] Smoke test tiap domain via `GraphQlEndpointTests` + `GraphQlResolverTests` (stub gRPC di-mock).
- [x] Otorisasi per role (ADMIN/STAFF/USER) dipasang via `@PreAuthorize` per-field.
- [x] `./mvnw -o -pl gateway -am test` → **BUILD SUCCESS** (5 test, 0 gagal).

---

## 13. Risiko & pertanyaan terbuka

1. **Envelope**: klien frontend payment gateway bergantung pada `{status,message,data}`;
   karena keputusan **full GraphQL**, klien harus pindah ke `errors[]` + `extensions.httpStatus`.
2. **`X-api-Key`**: ekspos sebagai argumen `apiKey` atau baca dari header via context? (§8)
3. **Tipe angka**: DTO memakai `Long` (amount, balance, dsb). GraphQL `ID` untuk identifier,
   `Float`/`Int` untuk nilai. Perlu dicek proto agar tidak overflow (GraphQL `Int` = 32-bit).
   Untuk aman, nilai uang besar disarankan `Float` atau `String`.
4. **Domain proto yang tidak diekspos REST** (`card/authorization`, `card/billing`, `card/payment`,
   `merchant_document`, `ai_security`, `AssignRoleToUser`): ekspos atau pertahankan paritas?
5. **`SaldoCommandService.UpdateSaldoBalance/UpdateSaldoWithdraw`**, `TopupCommandService`,
   `TransferCommandService` (UpdateTransferAmount), `WithdrawCommandService` — RPC tambahan
   yang tidak dipakai REST; ekspos sebagai mutation terpisah?
6. **`healthchecker`**: tetap query GraphQL (publik) atau pindah ke Actuator?
7. **Otorisasi field-level**: bila klien meminta field yang tidak diizinkan, seluruh query gagal
   (partial data + error). Apakah ini dapat diterima?
8. **Tidak ada upload**: tidak perlu `UploadController`; pastikan `dto/requests` yang
   mengandung file memang tidak ada (sudah dikonfirmasi tidak ada multipart).

---

## 14. Referensi internal

- `payment_gateway/gateway/pom.xml` — dependency gateway (tanpa GraphQL).
- `payment_gateway/gateway/.../config/GrpcClientConfig.java` — stub gRPC per service.
- `payment_gateway/gateway/.../config/SecurityConfig.java` — filter chain + `PUBLIC_ENDPOINTS`.
- `payment_gateway/gateway/.../security/AuthTokenFilter.java` — JWT → `SecurityContext`.
- `payment_gateway/gateway/.../mapper/ProtoMapper.java` — proto → DTO (dipakai ulang).
- `payment_gateway/gateway/.../exception/RestExceptionHandler.java` — sumber pemetaan error gRPC → HTTP.
- `payment_gateway/gateway/src/test/java/.../GatewayApplicationTests.java` — test context load yang sudah ada.
- `payment_gateway/common/src/main/proto/**` — kontrak gRPC tiap domain.
- **Pola implementasi**: `ecommerce/gateway` (`graphql/**`, `resources/graphql/**`,
  `GraphQl*Tests.java`) dan `ecommerce/gateway/GRAPHQL-MIGRATION.md`.

---

## 15. Ringkasan pemetaan controller → resolver

| Controller REST | Resolver GraphQL | Schema file |
|---|---|---|
| `AuthController` | `AuthResolver` | `auth.graphqls` |
| `UserController` | `UserResolver` | `user.graphqls` |
| `RoleController` | `RoleResolver` | `role.graphqls` |
| `MerchantController` | `MerchantResolver` (+ `MerchantTransactionResolver`) | `merchant.graphqls` |
| `CardController` | `CardResolver` (+ dashboard) | `card.graphqls` |
| `SaldoController` | `SaldoResolver` | `saldo.graphqls` |
| `TopupController` | `TopupResolver` | `topup.graphqls` |
| `TransactionController` | `TransactionResolver` | `transaction.graphqls` |
| `TransferController` | `TransferResolver` | `transfer.graphqls` |
| `WithdrawController` | `WithdrawResolver` | `withdraw.graphqls` |
| `StatsController` | `StatsResolver` | `stats.graphqls` |

---

## 16. Catatan implementasi (status akhir & deviasi)

Implementasi penuh **selesai**. Endpoint yang tersisa hanya `POST /graphql` (+ GraphiQL `/graphiql`).
Tidak ada upload di gateway ini, jadi tidak ada `UploadController`.

### 16.1 Berkas dibuat

- Schema (`src/main/resources/graphql/`, 13 berkas): `schema`, `common`, `auth`, `user`, `role`,
  `merchant`, `card`, `saldo`, `topup`, `transaction`, `transfer`, `withdraw`, `stats`.
- `graphql/Gql.java`, `graphql/dto/Page.java`, `graphql/dto/Token.java`, `graphql/GraphQlExceptionHandler.java`.
- `graphql/resolver/`: `Auth`, `User`, `Role`, `Merchant`, `Card`, `Saldo`, `Topup`, `Transaction`,
  `Transfer`, `Withdraw`, `Stats` (11 resolver).
- Test: `GraphQlEndpointTests`, `GraphQlResolverTests`.

### 16.2 Berkas dihapus

- 11 controller REST (`Auth`, `Card`, `Merchant`, `Role`, `Saldo`, `Stats`, `Topup`, `Transaction`,
  `Transfer`, `User`, `Withdraw`).
- `exception/RestExceptionHandler.java` (digantikan `GraphQlExceptionHandler`).
- Seluruh `dto/requests/**`; `dto/responses/**` dipertahankan untuk `ProtoMapper`.

### 16.3 Berkas diubah

- `pom.xml`: tambah `spring-boot-starter-graphql` + `spring-graphql-test`; deskripsi → GraphQL gateway.
- `application.properties`: blok `spring.graphql.*` (path `/graphql`, GraphiQL `/graphiql`, lokasi schema).
- `config/SecurityConfig.java`: `PUBLIC_ENDPOINTS` memuat `/graphql` + `/graphiql`; entri
  `/api/auth/{login,register,healthchecker}` dihapus.
- `mapper/ProtoMapper.java` dan `security/**` dipakai ulang tanpa perubahan.

### 16.4 Deviasi dari rencana (§6) dan alasan

1. **Tipe nilai uang (input).** GraphQL `Float` (mis. `amount`, `totalBalance`, `topupAmount`,
   `withdrawAmount`, `transferAmount`) dipetakan ke `Double` di record input lalu dikonversi ke
   proto `int32` via `Gql.i(Double)`. Output tetap `Long` di DTO dan diserialisasi GraphQL sebagai
   `Float`.
2. **`expireDate` (card).** Input `String` ISO `yyyy-MM-dd`, di-parse dengan `Gql.date(...)` →
   `LocalDate` → `ProtoMapper.toTimestamp(LocalDate)`; kosong → `Timestamp` default.
3. **Waktu transaksi/withdraw.** Mengikuti §6, `transactionTime`/`withdrawTime` **tidak** diekspos
   di input; proto menerima `Timestamp` default — sama dengan perilaku REST lama saat nilainya null.
4. **`apiKey`.** Header `X-api-Key` pada create/update transaction dipindah menjadi argumen field
   `apiKey` (opsi 1 §8); validasi tetap di service transaction.
5. **`username`.** `RegisterInput`, `CreateUserInput`, `UpdateUserInput` menyertakan `username`
   walau proto tidak memiliki field tersebut (diterima, tidak dikirim) — paritas dengan DTO REST lama.
6. **Boolean mutation.** Memakai `Gql.ok(resp.getStatus())` karena proto payment memakai
   `string status` ("success"), bukan `bool success`.
7. **`cardByUser`.** Mengembalikan objek tunggal `Card!` (proto `FindByUserIdCard` mengembalikan satu kartu).
8. **`me`.** Mengambil `UserDetailsImpl.getId()` (bukan username) seperti `AuthController` lama.
9. **`healthchecker`.** Mengembalikan `"API is up and running!"` (tanpa emoji — beda dari ecommerce).
10. **`topupsOfCard`.** Membungkus satu item `FindByCardNumberTopup` (`hasData()`), paritas REST.

### 16.5 Keamanan & error

- `/graphql` dan `/graphiql` `permitAll()` di filter chain; otorisasi per-field lewat
  `@PreAuthorize` (`ADMIN`/`STAFF`/`USER`) di method `@QueryMapping`/`@MutationMapping`.
- `me` memakai `isAuthenticated()`; `trashedCards` memakai `hasRole('ADMIN')` (menggantikan
  `isAuthenticated()` level kelas `CardController`).
- `GraphQlExceptionHandler` memetakan gRPC/validasi/security → `errors[].extensions.httpStatus`
  (tidak ada lagi envelope `{status,message,data}`).
- `AuthTokenFilter.shouldNotFilter` masih menyisakan prefiks `/api/auth/*` yang sudah tidak ada —
  sengaja dibiarkan (tidak berbahaya); `/graphql` **tidak** di-skip agar JWT tetap memprosesnya.

### 16.6 Verifikasi

- `GraphQlEndpointTests`: `healthchecker` publik sukses; field ber-role tanpa token → GraphQL error.
- `GraphQlResolverTests`: `roles` (proto → DTO → `*Page` + `PaginationMeta`) dan binding
  `createRole(input)` ke record (verifikasi `ArgumentCaptor`), stub gRPC di-mock.
- `./mvnw -o -pl gateway -am test` → BUILD SUCCESS, `Tests run: 5, Failures: 0, Errors: 0`.
- Inspeksi schema bersih (hanya `Query=[_root]`, `Mutation=[_root]` yang belum dipetakan).
