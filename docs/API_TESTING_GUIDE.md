# API Testing Guide — Utility Billing System

Test every API in order using Swagger UI: **http://localhost:8080/swagger-ui.html**

Sections are numbered **01 → 11** in Swagger (same order as this guide).

---

## Before You Start

| Item | Value |
|------|-------|
| Swagger URL | http://localhost:8080/swagger-ui.html |
| Default admin | `admin@wasac.rw` / `Admin@12345` |
| Password rule | Min 8 chars, uppercase, lowercase, digit, special char (e.g. `Test@12345`) |
| Phone format | `+250788XXXXXX` (12 digits after `+`) |
| National ID | 16 digits (e.g. `1199887766554401`) |

### How to authorize in Swagger

1. Run **01 - Authentication → 01 - Login** with admin credentials.
2. Copy the `token` from the response.
3. Click the green **Authorize** button (top right).
4. Paste: `Bearer <paste-token-here>` (include the word `Bearer` and a space).
5. Click **Authorize**, then **Close**.

When switching roles (Operator, Finance, Customer), login again and re-paste the new token.

### OTP and email

Registration, self-register, and forgot-password send a **6-digit OTP** to the email you used. Check that inbox (or spam). OTP expires in **5 minutes**.

### Three test data sets (A, B, C)

Use a different set each time you test an endpoint to avoid **409 Conflict** (duplicate email/phone/national ID).

**Do NOT use these existing accounts:**
- Emails: `admin@wasac.rw`, `operator@wasac.rw`, `paul.finance@wasac.rw`, `marie.operator@wasac.rw`, `jessyneige34@gmail.com`
- Phones: `+250788000000`, `+250788123456`, `+250788200001`, `+250788200002`, `+250788123766`

| Field | Set A | Set B | Set C |
|-------|-------|-------|-------|
| Phone | `+250788300001` | `+250788300002` | `+250788300003` |
| Customer email | `alice.testa@customer.rw` | `bob.testb@customer.rw` | `carol.testc@customer.rw` |
| Staff email (Operator) | `op.testa@wasac.rw` | `op.testb@wasac.rw` | `op.testc@wasac.rw` |
| Staff email (Finance) | `fin.testa@wasac.rw` | `fin.testb@wasac.rw` | `fin.testc@wasac.rw` |
| National ID | `1199887766554401` | `1199887766554402` | `1199887766554403` |
| Meter number | `WM-TEST-A-001` | `WM-TEST-B-001` | `WM-TEST-C-001` |
| Password | `Test@12345` | `Test@12345` | `Test@12345` |

**Save IDs from responses** — you will need them later:

| Response field | Used in |
|----------------|---------|
| `id` (customer) | Meters, notifications |
| `id` (meter) | Meter readings |
| `id` (meter reading) | Bill generate |
| `id` (bill) | Approve, reject, delete |
| `billReference` | Record payment |
| `id` (user) | File upload profile |

---

## Recommended Full Flow (end-to-end)

```
Customer self-registers (public) → Verify OTP
Admin login → Create tariff → Assign meter to customer
    → Login as Operator → Capture reading
    → Login as Finance → Generate bill → Approve bill → Record payment
    → Check notifications → Review audit logs
```

---

## 01 - Authentication

### 01 - Login (get JWT token)
`POST /api/auth/login` — **Public**

**Example A**
```json
{ "email": "admin@wasac.rw", "password": "Admin@12345" }
```

**Example B** (after creating operator staff — see 02)
```json
{ "email": "op.testa@wasac.rw", "password": "<temp-password-from-email>" }
```

**Example C** (after creating finance staff)
```json
{ "email": "fin.testa@wasac.rw", "password": "<temp-password-from-email>" }
```

---

### 02 - Register customer user
`POST /api/auth/register` — **Public**

**Example A**
```json
{
  "fullNames": "Alice Testa",
  "email": "alice.testa@customer.rw",
  "phoneNumber": "+250788300001",
  "password": "Test@12345",
  "roles": ["ROLE_CUSTOMER"]
}
```

**Example B**
```json
{
  "fullNames": "Bob Testb",
  "email": "bob.testb@customer.rw",
  "phoneNumber": "+250788300002",
  "password": "Test@12345",
  "roles": ["ROLE_CUSTOMER"]
}
```

**Example C**
```json
{
  "fullNames": "Carol Testc",
  "email": "carol.testc@customer.rw",
  "phoneNumber": "+250788300003",
  "password": "Test@12345",
  "roles": ["ROLE_CUSTOMER"]
}
```

---

### 03 - Verify OTP
`POST /api/auth/verify-otp` — **Public** (use OTP from email)

**Example A**
```json
{ "email": "alice.testa@customer.rw", "otpCode": "123456" }
```

**Example B**
```json
{ "email": "bob.testb@customer.rw", "otpCode": "123456" }
```

**Example C**
```json
{ "email": "carol.testc@customer.rw", "otpCode": "123456" }
```

> Replace `123456` with the actual OTP from the registration email.

---

### 04 - Verify email token
`POST /api/auth/verify-email` — **Public**

**Example A**
```json
{ "email": "alice.testa@customer.rw", "verificationToken": "<token-from-email>" }
```

**Example B**
```json
{ "email": "bob.testb@customer.rw", "verificationToken": "<token-from-email>" }
```

**Example C**
```json
{ "email": "carol.testc@customer.rw", "verificationToken": "<token-from-email>" }
```

---

### 05 - Change password
`PATCH /api/auth/change-password` — **Authenticated**

**Example A** (staff first login)
```json
{ "currentPassword": "<temp-password>", "newPassword": "Test@12345" }
```

**Example B**
```json
{ "currentPassword": "Test@12345", "newPassword": "NewTest@99" }
```

**Example C**
```json
{ "currentPassword": "NewTest@99", "newPassword": "Test@12345" }
```

---

### 06 - Forgot password
`POST /api/auth/forgot-password` — **Public**

**Example A**
```json
{ "email": "alice.testa@customer.rw" }
```

**Example B**
```json
{ "email": "bob.testb@customer.rw" }
```

**Example C**
```json
{ "email": "carol.testc@customer.rw" }
```

---

### 07 - Reset password
`POST /api/auth/reset-password` — **Public**

**Example A**
```json
{
  "email": "alice.testa@customer.rw",
  "otpCode": "123456",
  "newPassword": "Reset@12345"
}
```

**Example B**
```json
{
  "email": "bob.testb@customer.rw",
  "otpCode": "123456",
  "newPassword": "Reset@12345"
}
```

**Example C**
```json
{
  "email": "carol.testc@customer.rw",
  "otpCode": "123456",
  "newPassword": "Reset@12345"
}
```

---

### 08 - Logout
`POST /api/auth/logout` — **Authenticated**

No request body. Send with a valid Bearer token. Returns:
```json
{ "message": "Logged out successfully" }
```

Test 3 times by logging in as admin, operator, and finance — logout after each.

---

## 02 - User Management (ROLE_ADMIN)

Login as **admin** before testing this section.

### 01 - Create staff (Operator/Finance)
`POST /api/users/staff`

**Example A — Operator**
```json
{
  "fullNames": "Operator Testa",
  "email": "op.testa@wasac.rw",
  "phoneNumber": "+250788310001",
  "role": "ROLE_OPERATOR"
}
```

**Example B — Finance**
```json
{
  "fullNames": "Finance Testb",
  "email": "fin.testb@wasac.rw",
  "phoneNumber": "+250788310002",
  "role": "ROLE_FINANCE"
}
```

**Example C — Operator**
```json
{
  "fullNames": "Operator Testc",
  "email": "op.testc@wasac.rw",
  "phoneNumber": "+250788310003",
  "role": "ROLE_OPERATOR"
}
```

> Temp password is emailed. Use it to login, then change password (Auth 05).

---

### 02 - Create user
`POST /api/users`

**Example A**
```json
{
  "fullNames": "User Testa",
  "email": "user.testa@wasac.rw",
  "phoneNumber": "+250788320001",
  "password": "Test@12345",
  "status": "ACTIVE",
  "roles": ["ROLE_FINANCE"]
}
```

**Example B**
```json
{
  "fullNames": "User Testb",
  "email": "user.testb@wasac.rw",
  "phoneNumber": "+250788320002",
  "password": "Test@12345",
  "status": "ACTIVE",
  "roles": ["ROLE_OPERATOR"]
}
```

**Example C**
```json
{
  "fullNames": "User Testc",
  "email": "user.testc@wasac.rw",
  "phoneNumber": "+250788320003",
  "password": "Test@12345",
  "status": "ACTIVE",
  "roles": ["ROLE_CUSTOMER"]
}
```

---

### 03 - List / search users
`GET /api/users`

| Example | URL |
|---------|-----|
| A | `/api/users?page=0&size=10` |
| B | `/api/users?search=operator` |
| C | `/api/users?search=finance&page=0&size=5` |

---

### 04 - Get user by ID
`GET /api/users/{id}`

| Example | Path |
|---------|------|
| A | `/api/users/1` (admin) |
| B | `/api/users/2` (use ID from staff create response) |
| C | `/api/users/3` (use ID from user create response) |

---

### 05 - Update user
`PUT /api/users/{id}`

**Example A** — update user ID 2
```json
{
  "fullNames": "Operator Testa Updated",
  "email": "op.testa@wasac.rw",
  "phoneNumber": "+250788310001",
  "status": "ACTIVE",
  "roles": ["ROLE_OPERATOR"]
}
```

**Example B**
```json
{
  "fullNames": "Finance Testb Updated",
  "email": "fin.testb@wasac.rw",
  "phoneNumber": "+250788310002",
  "status": "ACTIVE",
  "roles": ["ROLE_FINANCE"]
}
```

**Example C**
```json
{
  "fullNames": "User Testc Updated",
  "email": "user.testc@wasac.rw",
  "phoneNumber": "+250788320003",
  "status": "ACTIVE",
  "roles": ["ROLE_CUSTOMER", "ROLE_OPERATOR"]
}
```

---

### 06 - Update user roles
`PATCH /api/users/{id}/roles`

**Example A**
```json
{ "roles": ["ROLE_OPERATOR"] }
```

**Example B**
```json
{ "roles": ["ROLE_FINANCE"] }
```

**Example C**
```json
{ "roles": ["ROLE_OPERATOR", "ROLE_FINANCE"] }
```

---

### 07 - Update user status
`PATCH /api/users/{id}/status`

**Example A**
```json
{ "status": "INACTIVE" }
```

**Example B**
```json
{ "status": "ACTIVE" }
```

**Example C**
```json
{ "status": "INACTIVE" }
```

---

### 08 - Delete user
`DELETE /api/users/{id}`

| Example | Path | Note |
|---------|------|------|
| A | `/api/users/99` | Use a test user ID you created |
| B | `/api/users/98` | Do not delete admin (ID 1) |
| C | `/api/users/97` | Delete only unused test accounts |

---

## 03 - Customer Management

> **Rule:** Only customers can create their own account via **self-register**. Admin and Operator cannot create customer accounts.

### 01 - Self register (public)
`POST /api/customers/self-register` — **No auth required**

**Example A**
```json
{
  "fullNames": "Alice Testa",
  "nationalId": "1199887766554401",
  "email": "alice.testa@customer.rw",
  "phoneNumber": "+250788300001",
  "address": "Kigali, Gasabo, Remera",
  "password": "Test@12345"
}
```

**Example B**
```json
{
  "fullNames": "Bob Testb",
  "nationalId": "1199887766554402",
  "email": "bob.testb@customer.rw",
  "phoneNumber": "+250788300002",
  "address": "Kigali, Kicukiro, Niboye",
  "password": "Test@12345"
}
```

**Example C**
```json
{
  "fullNames": "Carol Testc",
  "nationalId": "1199887766554403",
  "email": "carol.testc@customer.rw",
  "phoneNumber": "+250788300003",
  "address": "Kigali, Nyarugenge, Muhima",
  "password": "Test@12345"
}
```

> Then verify OTP in **01 - Authentication → 03**.

---

### 02 - List / search customers
`GET /api/customers`

| Example | URL |
|---------|-----|
| A | `/api/customers?page=0&size=10` |
| B | `/api/customers?search=alice` |
| C | `/api/customers?search=testc` |

---

### 03 - Get customer by ID
`GET /api/customers/{id}`

| Example | Path |
|---------|------|
| A | `/api/customers/1` |
| B | `/api/customers/2` |
| C | `/api/customers/3` |

---

### 04 - Update customer
`PUT /api/customers/{id}`

**Example A** — customer ID 1
```json
{
  "fullNames": "Alice Testa Updated",
  "nationalId": "1199887766554401",
  "email": "alice.testa@customer.rw",
  "phoneNumber": "+250788300001",
  "address": "Kigali, Gasabo, Remera — Updated",
  "status": "ACTIVE"
}
```

**Example B**
```json
{
  "fullNames": "Bob Testb Updated",
  "nationalId": "1199887766554402",
  "email": "bob.testb@customer.rw",
  "phoneNumber": "+250788300002",
  "address": "Kigali, Kicukiro — Updated",
  "status": "ACTIVE"
}
```

**Example C**
```json
{
  "fullNames": "Carol Testc Updated",
  "nationalId": "1199887766554403",
  "email": "carol.testc@customer.rw",
  "phoneNumber": "+250788300003",
  "address": "Kigali, Nyarugenge — Updated",
  "status": "ACTIVE"
}
```

---

### 05 - Delete customer
`DELETE /api/customers/{id}`

| Example | Path |
|---------|------|
| A | `/api/customers/10` |
| B | `/api/customers/11` |
| C | `/api/customers/12` |

Use IDs of test customers you no longer need.

---

## 04 - Meter Management

Requires **customerId** from section 03.

### 01 - Create meter
`POST /api/meters` — **ADMIN or OPERATOR**

**Example A**
```json
{
  "meterNumber": "WM-TEST-A-001",
  "meterType": "WATER",
  "installationDate": "2025-01-15",
  "status": "ACTIVE",
  "customerId": 1
}
```

**Example B**
```json
{
  "meterNumber": "EM-TEST-B-001",
  "meterType": "ELECTRICITY",
  "installationDate": "2025-02-01",
  "status": "ACTIVE",
  "customerId": 2
}
```

**Example C**
```json
{
  "meterNumber": "WM-TEST-C-001",
  "meterType": "WATER",
  "installationDate": "2025-03-10",
  "status": "ACTIVE",
  "customerId": 3
}
```

---

### 02 - List / search meters
`GET /api/meters`

| Example | URL |
|---------|-----|
| A | `/api/meters?page=0&size=10` |
| B | `/api/meters?search=WM-TEST` |
| C | `/api/meters?search=ELECTRICITY` |

---

### 03 - Get meter by ID
`GET /api/meters/{id}`

| Example | Path |
|---------|------|
| A | `/api/meters/1` |
| B | `/api/meters/2` |
| C | `/api/meters/3` |

---

### 04 - Update meter
`PUT /api/meters/{id}`

**Example A**
```json
{
  "meterNumber": "WM-TEST-A-001",
  "meterType": "WATER",
  "installationDate": "2025-01-20",
  "status": "ACTIVE",
  "customerId": 1
}
```

**Example B**
```json
{
  "meterNumber": "EM-TEST-B-001-UPD",
  "meterType": "ELECTRICITY",
  "installationDate": "2025-02-05",
  "status": "ACTIVE",
  "customerId": 2
}
```

**Example C**
```json
{
  "meterNumber": "WM-TEST-C-001",
  "meterType": "WATER",
  "installationDate": "2025-03-15",
  "status": "INACTIVE",
  "customerId": 3
}
```

---

### 05 - Delete meter
`DELETE /api/meters/{id}`

| Example | Path |
|---------|------|
| A | `/api/meters/10` |
| B | `/api/meters/11` |
| C | `/api/meters/12` |

---

## 05 - Meter Readings

Login as **Operator** (`op.testa@wasac.rw`) before capturing readings.

### 01 - Capture meter reading
`POST /api/meter-readings`

**Example A** — water meter, 50 m³ used
```json
{
  "meterId": 1,
  "previousReading": 100.00,
  "currentReading": 150.00,
  "readingDate": "2025-06-01"
}
```

**Example B** — electricity, 200 kWh used
```json
{
  "meterId": 2,
  "previousReading": 500.00,
  "currentReading": 700.00,
  "readingDate": "2025-06-01"
}
```

**Example C**
```json
{
  "meterId": 3,
  "previousReading": 0.00,
  "currentReading": 45.50,
  "readingDate": "2025-06-02"
}
```

> Save `id` from response → used in **07 - Bills → 01 - Generate bill**.

---

### 02 - List all readings
`GET /api/meter-readings`

No parameters. Call 3 times after creating readings A, B, C.

---

### 03 - Get reading by ID
`GET /api/meter-readings/{id}`

| Example | Path |
|---------|------|
| A | `/api/meter-readings/1` |
| B | `/api/meter-readings/2` |
| C | `/api/meter-readings/3` |

---

### 04 - Update reading
`PUT /api/meter-readings/{id}` — only if no bill linked yet

**Example A**
```json
{
  "meterId": 1,
  "previousReading": 100.00,
  "currentReading": 155.00,
  "readingDate": "2025-06-01"
}
```

**Example B**
```json
{
  "meterId": 2,
  "previousReading": 500.00,
  "currentReading": 710.00,
  "readingDate": "2025-06-01"
}
```

**Example C**
```json
{
  "meterId": 3,
  "previousReading": 0.00,
  "currentReading": 48.00,
  "readingDate": "2025-06-02"
}
```

---

### 05 - Delete reading
`DELETE /api/meter-readings/{id}`

| Example | Path |
|---------|------|
| A | `/api/meter-readings/10` |
| B | `/api/meter-readings/11` |
| C | `/api/meter-readings/12` |

---

## 06 - Tariff Configuration (ROLE_ADMIN)

Create tariffs **before** generating bills. Match `meterType` to the meter (WATER or ELECTRICITY).

### 01 - Create tariff
`POST /api/tariffs`

**Example A — Flat water tariff**
```json
{
  "name": "Water Flat Tariff A",
  "meterType": "WATER",
  "tariffType": "FLAT",
  "flatRate": 500.00,
  "fixedServiceCharge": 1000.00,
  "taxRate": 18.00,
  "penaltyRate": 5.00,
  "effectiveFrom": "2025-01-01",
  "effectiveTo": "2025-12-31"
}
```

**Example B — Tier-based electricity**
```json
{
  "name": "Electricity Tier Tariff B",
  "meterType": "ELECTRICITY",
  "tariffType": "TIER_BASED",
  "fixedServiceCharge": 2000.00,
  "taxRate": 18.00,
  "penaltyRate": 5.00,
  "effectiveFrom": "2025-01-01",
  "tiers": [
    { "minConsumption": 0, "maxConsumption": 100, "ratePerUnit": 120.00 },
    { "minConsumption": 100, "maxConsumption": 500, "ratePerUnit": 180.00 },
    { "minConsumption": 500, "maxConsumption": null, "ratePerUnit": 250.00 }
  ]
}
```

**Example C — Tier-based water**
```json
{
  "name": "Water Tier Tariff C",
  "meterType": "WATER",
  "tariffType": "TIER_BASED",
  "fixedServiceCharge": 1500.00,
  "taxRate": 18.00,
  "penaltyRate": 3.00,
  "effectiveFrom": "2025-06-01",
  "tiers": [
    { "minConsumption": 0, "maxConsumption": 10, "ratePerUnit": 400.00 },
    { "minConsumption": 10, "maxConsumption": null, "ratePerUnit": 600.00 }
  ]
}
```

---

### 02 - List all tariffs
`GET /api/tariffs`

No parameters.

---

### 03 - Get tariff by ID
`GET /api/tariffs/{id}`

| Example | Path |
|---------|------|
| A | `/api/tariffs/1` |
| B | `/api/tariffs/2` |
| C | `/api/tariffs/3` |

---

### 04 - Update tariff
`PUT /api/tariffs/{id}`

**Example A**
```json
{
  "name": "Water Flat Tariff A — Updated",
  "meterType": "WATER",
  "tariffType": "FLAT",
  "flatRate": 550.00,
  "fixedServiceCharge": 1000.00,
  "taxRate": 18.00,
  "penaltyRate": 5.00,
  "effectiveFrom": "2025-01-01"
}
```

**Example B** — same structure as create B with adjusted rates
```json
{
  "name": "Electricity Tier Tariff B — Updated",
  "meterType": "ELECTRICITY",
  "tariffType": "TIER_BASED",
  "fixedServiceCharge": 2200.00,
  "taxRate": 18.00,
  "penaltyRate": 5.00,
  "effectiveFrom": "2025-01-01",
  "tiers": [
    { "minConsumption": 0, "maxConsumption": 100, "ratePerUnit": 130.00 },
    { "minConsumption": 100, "maxConsumption": null, "ratePerUnit": 200.00 }
  ]
}
```

**Example C**
```json
{
  "name": "Water Tier Tariff C — Updated",
  "meterType": "WATER",
  "tariffType": "TIER_BASED",
  "fixedServiceCharge": 1600.00,
  "taxRate": 18.00,
  "penaltyRate": 3.00,
  "effectiveFrom": "2025-06-01",
  "tiers": [
    { "minConsumption": 0, "maxConsumption": 10, "ratePerUnit": 420.00 },
    { "minConsumption": 10, "maxConsumption": null, "ratePerUnit": 620.00 }
  ]
}
```

---

### 05 - Deactivate tariff
`DELETE /api/tariffs/{id}`

| Example | Path |
|---------|------|
| A | `/api/tariffs/10` |
| B | `/api/tariffs/11` |
| C | `/api/tariffs/12` |

---

## 07 - Bill Management

Login as **Finance** before this section.

### 01 - Generate bill
`POST /api/bills/generate`

**Example A**
```json
{ "meterReadingId": 1 }
```

**Example B**
```json
{ "meterReadingId": 2 }
```

**Example C**
```json
{ "meterReadingId": 3 }
```

> Save `id` and `billReference` from the response.

---

### 02 - Approve bill
`PATCH /api/bills/{id}/approve`

| Example | Path |
|---------|------|
| A | `/api/bills/1/approve` |
| B | `/api/bills/2/approve` |
| C | `/api/bills/3/approve` |

---

### 03 - Reject bill
`PATCH /api/bills/{id}/reject`

| Example | Path |
|---------|------|
| A | `/api/bills/4/reject` |
| B | `/api/bills/5/reject` |
| C | `/api/bills/6/reject` |

Use extra bills you generated only for rejection testing.

---

### 04 - List / search bills
`GET /api/bills`

| Example | URL |
|---------|-----|
| A | `/api/bills?page=0&size=10` |
| B | `/api/bills?customerId=1` |
| C | `/api/bills?search=BILL` |

---

### 05 - Get bill by ID
`GET /api/bills/{id}`

| Example | Path |
|---------|------|
| A | `/api/bills/1` |
| B | `/api/bills/2` |
| C | `/api/bills/3` |

---

### 06 - Get bill by reference
`GET /api/bills/reference/{reference}`

| Example | Path |
|---------|------|
| A | `/api/bills/reference/BILL-20250601-0001` |
| B | `/api/bills/reference/BILL-20250601-0002` |
| C | `/api/bills/reference/BILL-20250601-0003` |

Replace with actual `billReference` from generate response.

---

### 07 - Delete bill
`DELETE /api/bills/{id}` — not allowed if PAID

| Example | Path |
|---------|------|
| A | `/api/bills/7` |
| B | `/api/bills/8` |
| C | `/api/bills/9` |

Use rejected or unpaid test bills only.

---

## 08 - Payment Management (ROLE_FINANCE)

Bill must be **APPROVED** before payment.

### 01 - Record payment
`POST /api/payments`

**Example A**
```json
{
  "billReference": "BILL-20250601-0001",
  "amountPaid": 15000.00,
  "paymentMethod": "MOBILE_MONEY",
  "paymentDate": "2025-06-05"
}
```

**Example B**
```json
{
  "billReference": "BILL-20250601-0002",
  "amountPaid": 45000.00,
  "paymentMethod": "BANK_TRANSFER",
  "paymentDate": "2025-06-05"
}
```

**Example C**
```json
{
  "billReference": "BILL-20250601-0003",
  "amountPaid": 8500.00,
  "paymentMethod": "CASH",
  "paymentDate": "2025-06-05"
}
```

> Use exact `billReference` and `totalAmount` from the approved bill response.

---

### 02 - List / search payments
`GET /api/payments`

| Example | URL |
|---------|-----|
| A | `/api/payments?page=0&size=10` |
| B | `/api/payments?search=MOBILE` |
| C | `/api/payments?search=BILL` |

---

### 03 - Get payment by ID
`GET /api/payments/{id}`

| Example | Path |
|---------|------|
| A | `/api/payments/1` |
| B | `/api/payments/2` |
| C | `/api/payments/3` |

---

### 04 - Get payments for bill
`GET /api/payments/bill/{billId}`

| Example | Path |
|---------|------|
| A | `/api/payments/bill/1` |
| B | `/api/payments/bill/2` |
| C | `/api/payments/bill/3` |

---

### 05 - Delete payment
`DELETE /api/payments/{id}`

| Example | Path |
|---------|------|
| A | `/api/payments/10` |
| B | `/api/payments/11` |
| C | `/api/payments/12` |

---

## 09 - Notifications

### 01 - Get customer notifications
`GET /api/notifications/customer/{customerId}`

| Example | Path |
|---------|------|
| A | `/api/notifications/customer/1` |
| B | `/api/notifications/customer/2` |
| C | `/api/notifications/customer/3` |

---

### 02 - Get unread notifications
`GET /api/notifications/customer/{customerId}/unread`

| Example | Path |
|---------|------|
| A | `/api/notifications/customer/1/unread` |
| B | `/api/notifications/customer/2/unread` |
| C | `/api/notifications/customer/3/unread` |

---

### 03 - List all notifications
`GET /api/notifications` — **ADMIN or FINANCE**

No parameters.

---

### 04 - Get notification by ID
`GET /api/notifications/{id}`

| Example | Path |
|---------|------|
| A | `/api/notifications/1` |
| B | `/api/notifications/2` |
| C | `/api/notifications/3` |

---

### 05 - Mark notification as read
`PATCH /api/notifications/{id}/read`

| Example | Path |
|---------|------|
| A | `/api/notifications/1/read` |
| B | `/api/notifications/2/read` |
| C | `/api/notifications/3/read` |

---

### 06 - Delete notification
`DELETE /api/notifications/{id}`

| Example | Path |
|---------|------|
| A | `/api/notifications/10` |
| B | `/api/notifications/11` |
| C | `/api/notifications/12` |

---

## 10 - File Management

File uploads use **multipart/form-data** in Swagger (choose file in the `file` field).

### 01 - Upload profile picture
`POST /api/files/profile/{userId}`

| Example | Path | File |
|---------|------|------|
| A | `/api/files/profile/1` | Any `.jpg` or `.png` under 10 MB |
| B | `/api/files/profile/2` | Different image file |
| C | `/api/files/profile/3` | Another image file |

---

### 02 - Upload customer document
`POST /api/files/customer/{customerId}`

| Example | Path | File |
|---------|------|------|
| A | `/api/files/customer/1` | PDF or image document |
| B | `/api/files/customer/2` | PDF or image document |
| C | `/api/files/customer/3` | PDF or image document |

---

### 03 - Get file metadata
`GET /api/files/{id}`

| Example | Path |
|---------|------|
| A | `/api/files/1` |
| B | `/api/files/2` |
| C | `/api/files/3` |

---

### 04 - List files by entity
`GET /api/files/entity/{entityType}/{entityId}`

| Example | Path |
|---------|------|
| A | `/api/files/entity/Customer/1` |
| B | `/api/files/entity/User/2` |
| C | `/api/files/entity/Customer/3` |

---

### 05 - Download file
`GET /api/files/{id}/download`

| Example | Path |
|---------|------|
| A | `/api/files/1/download` |
| B | `/api/files/2/download` |
| C | `/api/files/3/download` |

---

### 06 - Delete file
`DELETE /api/files/{id}`

| Example | Path |
|---------|------|
| A | `/api/files/10` |
| B | `/api/files/11` |
| C | `/api/files/12` |

---

## 11 - Audit Logs (ROLE_ADMIN)

### 01 - List audit logs
`GET /api/audit-logs`

| Example | URL |
|---------|-----|
| A | `/api/audit-logs?page=0&size=20` |
| B | `/api/audit-logs?user=admin@wasac.rw` |
| C | `/api/audit-logs?user=op.testa@wasac.rw&page=0&size=10` |

---

### 02 - Get audit log by ID
`GET /api/audit-logs/{id}`

| Example | Path |
|---------|------|
| A | `/api/audit-logs/1` |
| B | `/api/audit-logs/2` |
| C | `/api/audit-logs/3` |

---

### 03 - Delete audit log
`DELETE /api/audit-logs/{id}`

| Example | Path |
|---------|------|
| A | `/api/audit-logs/50` |
| B | `/api/audit-logs/51` |
| C | `/api/audit-logs/52` |

---

## Common Errors

| Code | Meaning | Fix |
|------|---------|-----|
| **400** | Validation failed | Check field names (`fullNames` not `fullName`), phone format, password rules |
| **401** | Not authenticated | Login and paste Bearer token in Authorize |
| **403** | Wrong role | Switch to the correct role (see endpoint description) |
| **404** | ID not found | Use an ID from a recent create/list response |
| **409** | Duplicate | Use Set B or C emails/phones/national IDs |
| **422** | Business rule | e.g. bill not approved before payment, reading already billed |

---

## Quick Role Cheat Sheet

| Task | Login as |
|------|----------|
| Create staff, tariffs, audit | `admin@wasac.rw` |
| Assign meter, capture reading | `op.testa@wasac.rw` (or admin) |
| Generate/approve bill, payment | `fin.testa@wasac.rw` (or admin) |
| Customer self-service | Customer account after OTP verify |
