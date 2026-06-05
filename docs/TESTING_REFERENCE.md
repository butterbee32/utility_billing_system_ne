# Complete API Testing Reference

Use this guide to test **every API** in Swagger: **http://localhost:8080/swagger-ui.html**

---

## Quick setup

| Setting | Value |
|---------|-------|
| Swagger | http://localhost:8080/swagger-ui.html |
| JWT expiry | **48 hours** (after app restart) |
| Password rule | Min 8 chars, uppercase, lowercase, digit, special char |
| Phone format | `+250788XXXXXX` |
| National ID | Exactly **16 digits** |

### Authorize in Swagger

1. **Login** first (clear Authorize if login returns 401).
2. Copy the `token` from the response.
3. Click **Authorize** → paste **token only** (no `Bearer` — Swagger adds it).
4. Switch roles → login again → re-paste new token.

### Pagination (`page`, `size`, `sort`)

| Param | Value |
|-------|-------|
| `page` | `0` (first page) |
| `size` | `20` |
| `sort` | `createdAt,desc` or **leave blank** |

Do **not** use JSON array format for sort (causes 500).

### OTP (self-register / forgot-password)

- Check the customer's email inbox (or spam).
- OTP expires in **5 minutes**.
- SQL fallback (pgAdmin): `SELECT otp_code FROM otp_tokens WHERE email='...' AND used=false ORDER BY generated_at DESC LIMIT 1;`

---

## Accounts already in your database

> Passwords are **hashed** in the DB — only the values below are known.  
> Staff/admin-created accounts receive a **temp password by email**.

| Role | Email | Password | Phone | Notes |
|------|-------|----------|-------|-------|
| **ADMIN** | `admin@wasac.rw` | `Admin@12345` | `+250788000000` | Seeded on first startup |
| **OPERATOR** | `operator@wasac.rw` | Check creation email | `+250788123456` | Created during testing |
| **OPERATOR** | `marie.operator@wasac.rw` | Check creation email | `+250788200002` | Created during testing |
| **FINANCE** | `paul.finance@wasac.rw` | Check creation email | `+250788200001` | Created during testing |
| **CUSTOMER** | `jessyneige34@gmail.com` | Password used at register | `+250788123766` | Self-registered |

**If staff password unknown:** login as admin → create new staff, or use forgot-password if the account has a customer email.

---

## Fresh test data (use for new runs — avoids 409 Conflict)

Use **Set D / E / F** when existing emails are already taken.

| Field | Set D | Set E | Set F |
|-------|-------|-------|-------|
| Customer email | `diana.testd@customer.rw` | `eric.teste@customer.rw` | `fiona.testf@customer.rw` |
| Phone | `+250788400001` | `+250788400002` | `+250788400003` |
| National ID | `1199887766554601` | `1199887766554602` | `1199887766554603` |
| Password | `Test@12345` | `Test@12345` | `Test@12345` |
| Meter number | `WM-TEST-D-001` | `WM-TEST-E-001` | `WM-TEST-F-001` |
| Operator staff | `op.testd@wasac.rw` | `op.teste@wasac.rw` | — |
| Finance staff | `fin.testd@wasac.rw` | — | `fin.testf@wasac.rw` |
| Staff phone | `+250788410001` | `+250788410002` | `+250788410003` |

---

## Recommended full flow

```
OPTION A — Customer self-registers:
  03 → 01 Self-register → 01 → 03 Verify OTP → customer login

OPTION B — Admin registers customer:
  01 Login (admin) → 03 → 02 Create customer → temp password emailed

THEN (both options):
  06 → 01 Create tariff
  04 → 01 Create meter (customerId)
  Login Operator → 05 → 01 Capture reading
  Login Finance → 07 → Generate → Approve bill
  08 → 01 Record payment
  09 → Check notifications
  Customer login → view bills & payments
```

---

## ID tracker (fill in as you test)

| Item | Your ID / value |
|------|-----------------|
| customerId | |
| meterId | |
| meterReadingId | |
| tariffId | |
| billId | |
| billReference | |
| paymentId | |
| notificationId | |

---

# 01 - Authentication

| # | Method | Endpoint | Auth | Login as |
|---|--------|----------|------|----------|
| 01 | POST | `/api/auth/login` | No | Anyone |
| 02 | POST | `/api/auth/register` | No | Public |
| 03 | POST | `/api/auth/verify-otp` | No | Public |
| 04 | POST | `/api/auth/verify-email` | No | Public |
| 05 | PATCH | `/api/auth/change-password` | Yes | Any logged-in user |
| 06 | POST | `/api/auth/forgot-password` | No | Public |
| 07 | POST | `/api/auth/reset-password` | No | Public |
| 08 | POST | `/api/auth/logout` | Yes | Any logged-in user |

### 01 - Login

**Existing — Admin**
```json
{ "email": "admin@wasac.rw", "password": "Admin@12345" }
```

**Existing — Customer**
```json
{ "email": "jessyneige34@gmail.com", "password": "<password-used-at-register>" }
```

**Fresh — Set D customer (after self-register + OTP)**
```json
{ "email": "diana.testd@customer.rw", "password": "Test@12345" }
```

### 02 - Register (optional — self-register is preferred)

**Fresh Set D**
```json
{
  "fullNames": "Diana Testd",
  "email": "diana.testd@customer.rw",
  "phoneNumber": "+250788400001",
  "password": "Test@12345",
  "roles": ["ROLE_CUSTOMER"]
}
```

### 03 - Verify OTP

```json
{ "email": "diana.testd@customer.rw", "otpCode": "123456" }
```
Replace `123456` with code from email.

### 04 - Verify email

```json
{ "email": "diana.testd@customer.rw", "verificationToken": "<from-email>" }
```

### 05 - Change password

```json
{ "currentPassword": "Test@12345", "newPassword": "NewTest@99" }
```

### 06 - Forgot password

```json
{ "email": "diana.testd@customer.rw" }
```

### 07 - Reset password

```json
{
  "email": "diana.testd@customer.rw",
  "otpCode": "123456",
  "newPassword": "Reset@12345"
}
```

### 08 - Logout

No body. Clears token — login again after.

---

# 02 - User Management (ADMIN only)

| # | Method | Endpoint |
|---|--------|----------|
| 01 | POST | `/api/users/staff` |
| 02 | POST | `/api/users` |
| 03 | GET | `/api/users` |
| 04 | GET | `/api/users/{id}` |
| 05 | PUT | `/api/users/{id}` |
| 06 | PATCH | `/api/users/{id}/roles` |
| 07 | PATCH | `/api/users/{id}/status` |
| 08 | DELETE | `/api/users/{id}` |

**Login:** `admin@wasac.rw` / `Admin@12345`

### 01 - Create staff (Operator)

**Fresh Set D**
```json
{
  "fullNames": "Operator Testd",
  "email": "op.testd@wasac.rw",
  "phoneNumber": "+250788410001",
  "role": "ROLE_OPERATOR"
}
```
Password → check email.

### 01 - Create staff (Finance)

**Fresh Set F**
```json
{
  "fullNames": "Finance Testf",
  "email": "fin.testf@wasac.rw",
  "phoneNumber": "+250788410003",
  "role": "ROLE_FINANCE"
}
```

### 02 - Create user

**Fresh**
```json
{
  "fullNames": "User Testd",
  "email": "user.testd@wasac.rw",
  "phoneNumber": "+250788420001",
  "password": "Test@12345",
  "status": "ACTIVE",
  "roles": ["ROLE_FINANCE"]
}
```

### 03 - List users

- Existing: `page=0`, `size=20`
- Search: `search=operator`

### 04 - Get user

- Existing admin: `/api/users/1`
- Staff: use id from create response

### 05 - Update user

```json
{
  "fullNames": "Operator Updated",
  "email": "op.testd@wasac.rw",
  "phoneNumber": "+250788410001",
  "status": "ACTIVE",
  "roles": ["ROLE_OPERATOR"]
}
```

### 06 - Update roles

```json
{ "roles": ["ROLE_OPERATOR"] }
```

### 07 - Update status

```json
{ "status": "ACTIVE" }
```

### 08 - Delete user

Use a throwaway test user id — **not** admin id `1`.

---

# 03 - Customer Management

| # | Method | Endpoint | Auth |
|---|--------|----------|------|
| 01 | POST | `/api/customers/self-register` | **No** |
| 02 | POST | `/api/customers` | ADMIN, OPERATOR |
| 03 | GET | `/api/customers` | ADMIN, OPERATOR, FINANCE |
| 04 | GET | `/api/customers/{id}` | All roles |
| 05 | PUT | `/api/customers/{id}` | ADMIN, OPERATOR |
| 06 | DELETE | `/api/customers/{id}` | ADMIN |

### 01 - Self register (public)

**Fresh Set D**
```json
{
  "fullNames": "Diana Testd",
  "nationalId": "1199887766554601",
  "email": "diana.testd@customer.rw",
  "phoneNumber": "+250788400001",
  "address": "Kigali, Gasabo",
  "password": "Test@12345"
}
```
Then **01 → 03 Verify OTP**.

### 02 - Create customer (admin/operator)

**Fresh Set E**
```json
{
  "fullNames": "Eric Teste",
  "nationalId": "1199887766554602",
  "email": "eric.teste@customer.rw",
  "phoneNumber": "+250788400002",
  "address": "Kigali, Kicukiro",
  "status": "ACTIVE"
}
```
Temp password emailed — no OTP.

### 03 - List customers

`page=0`, `size=20`, optional `search=diana`

### 04 - Get customer

`/api/customers/{customerId}`

### 05 - Update customer

```json
{
  "fullNames": "Diana Testd Updated",
  "nationalId": "1199887766554601",
  "email": "diana.testd@customer.rw",
  "phoneNumber": "+250788400001",
  "address": "Kigali — Updated",
  "status": "ACTIVE"
}
```

### 06 - Delete customer

Throwaway test customer only.

---

# 04 - Meter Management (ADMIN, OPERATOR)

| # | Method | Endpoint |
|---|--------|----------|
| 01 | POST | `/api/meters` |
| 02 | GET | `/api/meters` |
| 03 | GET | `/api/meters/{id}` |
| 04 | PUT | `/api/meters/{id}` |
| 05 | DELETE | `/api/meters/{id}` |

### 01 - Create meter

**Fresh Set D**
```json
{
  "meterNumber": "WM-TEST-D-001",
  "meterType": "WATER",
  "installationDate": "2025-06-01",
  "status": "ACTIVE",
  "customerId": 1
}
```
Replace `customerId` with yours.

### 02 - List meters

`page=0`, `size=20` or `search=WM-TEST`

### 03–05

Use `meterId` from create response.

---

# 05 - Meter Readings (ADMIN, OPERATOR)

| # | Method | Endpoint |
|---|--------|----------|
| 01 | POST | `/api/meter-readings` |
| 02 | GET | `/api/meter-readings` |
| 03 | GET | `/api/meter-readings/{id}` |
| 04 | PUT | `/api/meter-readings/{id}` |
| 05 | DELETE | `/api/meter-readings/{id}` |

**Login:** operator token

### 01 - Capture reading

```json
{
  "meterId": 1,
  "previousReading": 0.00,
  "currentReading": 55.00,
  "readingDate": "2025-06-05"
}
```

---

# 06 - Tariff Configuration (ADMIN)

| # | Method | Endpoint |
|---|--------|----------|
| 01 | POST | `/api/tariffs` |
| 02 | GET | `/api/tariffs` |
| 03 | GET | `/api/tariffs/{id}` |
| 04 | PUT | `/api/tariffs/{id}` |
| 05 | DELETE | `/api/tariffs/{id}` |

### 01 - Create tariff (flat water)

```json
{
  "name": "Water Flat Tariff D",
  "meterType": "WATER",
  "tariffType": "FLAT",
  "flatRate": 500.00,
  "fixedServiceCharge": 1000.00,
  "taxRate": 18.00,
  "penaltyRate": 5.00,
  "effectiveFrom": "2025-01-01"
}
```

### 01 - Create tariff (tier electricity)

```json
{
  "name": "Electricity Tier E",
  "meterType": "ELECTRICITY",
  "tariffType": "TIER_BASED",
  "fixedServiceCharge": 2000.00,
  "taxRate": 18.00,
  "penaltyRate": 5.00,
  "effectiveFrom": "2025-01-01",
  "tiers": [
    { "minConsumption": 0, "maxConsumption": 100, "ratePerUnit": 120.00 },
    { "minConsumption": 100, "maxConsumption": null, "ratePerUnit": 200.00 }
  ]
}
```

---

# 07 - Bill Management (ADMIN, FINANCE)

| # | Method | Endpoint |
|---|--------|----------|
| 01 | POST | `/api/bills/generate` |
| 02 | PATCH | `/api/bills/{id}/approve` |
| 03 | PATCH | `/api/bills/{id}/reject` |
| 04 | GET | `/api/bills` |
| 05 | GET | `/api/bills/{id}` |
| 06 | GET | `/api/bills/reference/{reference}` |
| 07 | DELETE | `/api/bills/{id}` |

**Login:** finance or admin

### 01 - Generate bill

```json
{ "meterReadingId": 1 }
```

### 02 - Approve

`PATCH /api/bills/{billId}/approve`

### 04 - List bills (customer view)

`GET /api/bills?customerId=1`

### 06 - By reference

`GET /api/bills/reference/BILL-...`

---

# 08 - Payment Management (ADMIN, FINANCE)

| # | Method | Endpoint |
|---|--------|----------|
| 01 | POST | `/api/payments` |
| 02 | GET | `/api/payments` |
| 03 | GET | `/api/payments/{id}` |
| 04 | GET | `/api/payments/bill/{billId}` |
| 05 | DELETE | `/api/payments/{id}` |

### 01 - Record payment

Bill must be **APPROVED**.

```json
{
  "billReference": "BILL-20250605-0001",
  "amountPaid": 15000.00,
  "paymentMethod": "MOBILE_MONEY",
  "paymentDate": "2025-06-05"
}
```

`paymentMethod`: `CASH`, `BANK_TRANSFER`, `MOBILE_MONEY`, `CARD`

### 04 - Customer payment history

`GET /api/payments/bill/{billId}` (customer token works)

---

# 09 - Notifications

| # | Method | Endpoint |
|---|--------|----------|
| 01 | GET | `/api/notifications/customer/{customerId}` |
| 02 | GET | `/api/notifications/customer/{customerId}/unread` |
| 03 | GET | `/api/notifications` |
| 04 | GET | `/api/notifications/{id}` |
| 05 | PATCH | `/api/notifications/{id}/read` |
| 06 | DELETE | `/api/notifications/{id}` |

---

# 10 - File Management

| # | Method | Endpoint |
|---|--------|----------|
| 01 | POST | `/api/files/profile/{userId}` |
| 02 | POST | `/api/files/customer/{customerId}` |
| 03 | GET | `/api/files/{id}` |
| 04 | GET | `/api/files/entity/{entityType}/{entityId}` |
| 05 | GET | `/api/files/{id}/download` |
| 06 | DELETE | `/api/files/{id}` |

Uploads use **multipart/form-data** — pick any `.jpg` or `.pdf` under 10 MB.

---

# 11 - Audit Logs (ADMIN)

| # | Method | Endpoint |
|---|--------|----------|
| 01 | GET | `/api/audit-logs` |
| 02 | GET | `/api/audit-logs/{id}` |
| 03 | DELETE | `/api/audit-logs/{id}` |

Filter: `?user=admin@wasac.rw`

---

## Role cheat sheet

| Task | Login as |
|------|----------|
| Users, tariffs, audit, admin customer create | `admin@wasac.rw` / `Admin@12345` |
| Meters, readings, customer create | Operator (or admin) |
| Bills, payments, list all payments | Finance (or admin) |
| Self-register, OTP | No login |
| View own bills/payments | Customer |

---

## Common errors

| Code | Fix |
|------|-----|
| **401** | Clear Authorize → login again → paste fresh token |
| **409** | Email/phone/national ID taken — use Set D/E/F |
| **400** | Check field names (`fullNames`), phone `+250...`, 16-digit national ID |
| **403** | Wrong role — switch login |
| **500 on list** | Remove bad `sort` JSON — use `createdAt,desc` or leave blank |

---

## Refresh this document

After creating new users, run as admin:

`GET /api/users?page=0&size=50`

`GET /api/customers?page=0&size=50`

Copy ids into the **ID tracker** section above.
