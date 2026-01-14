
# 🛡️ **AUTHO-FORGE-SERVICE — FULL SCOPE**

Think of this as the "roadmap" for building the authentication server.

We divide it into **MVP**, **Core Production**, and **Advanced Enhancements**.

---

# ⭐ PHASE 1 — **MVP Core Authentication (Must Have)**

This will make the system **functional end-to-end**.

### 1️⃣ **User Entity + Repository**

* id (UUID)
* username (unique)
* passwordHash (BCrypt)
* roles (comma-separated or list)
* timestamps

### 2️⃣ **Register API**

`POST /api/v1/auth/register`

* Accept username + password
* Validate inputs
* Hash password
* Save user

### 3️⃣ **Login API**

`POST /api/v1/auth/login`

* Validate username/password
* Generate Access Token (JWT RS256)
* Generate Refresh Token (JWT RS256)

### 4️⃣ **Token Generator (JWT Signing Service)**

* Generate RS256 access token
* Generate refresh token
* Include:

    * sub
    * roles
    * exp
    * iss
    * kid

### 5️⃣ **Key Loading**

* From classpath for dev (`private_key.pem`)
* Will later integrate with Vault/KMS

### 6️⃣ **JWK Publisher Endpoint**

`GET /.well-known/jwks.json`

* Publish public RSA key in JWK format
* Required by microservices

### 7️⃣ **Refresh Token API**

`POST /api/v1/auth/refresh`

* Validate refresh token signature & expiry
* Issue new access token
* (Optional) issue a rotated refresh token

### 8️⃣ **Exception Handling**

* Unified JSON error format
* GlobalExceptionHandler

---

# ⭐ PHASE 2 — **Security Layer**

This includes:

### 1️⃣ SecurityConfig

* Allow `/api/v1/auth/*`
* Allow `/.well-known/*`
* Secure everything else
* Enable BCryptPasswordEncoder

### 2️⃣ Password Encoder

* BCrypt with strength 10–12

### 3️⃣ CORS Config

(Open depending on environment)

---

# ⭐ PHASE 3 — **Database Layer**

* H2 for dev
* Later Postgres/MySQL for prod

Migrations:

* Flyway or Liquibase (optional for MVP)

Tables needed:

* users
* refresh_tokens (optional for MVP, mandatory later)

---

# ⭐ PHASE 4 — **Production-Ready Enhancements (Next)**

These will make the service robust for real-world use.

### 1️⃣ Refresh Token Storage

Store refresh tokens to:

* allow revocation
* track login devices
* track token reuse

### 2️⃣ Rate Limiting

* login attempts limit
* register attempts limit

### 3️⃣ Actuator + Metrics

Add:

* login_success_count
* login_failure_count
* refresh_success_count
* refresh_failure_count

### 4️⃣ Validation & Sanitization

@Valid
@Size
@Pattern
etc.

---

# ⭐ PHASE 5 — **Advanced (Optional but Powerful)**

### 1️⃣ Key Rotation

* generate new RSA keypair
* store old + new
* publish both in JWK set
* auto-rotate monthly

### 2️⃣ Device-bound Refresh Tokens

Track:

* IP
* device_id
* issued_at

### 3️⃣ Admin APIs

* DELETE user
* GET user
* RESET password

### 4️⃣ OpenID Discovery Document

Optional:

`/.well-known/openid-configuration`

---

# 🧩 Putting It All Together — The Development Blueprint

Below is the **exact order we will implement things**:

---

# 📌 **Step-by-step Implementation Order (What we code next)**

### ✔ Step 1 — User Entity

### ✔ Step 2 — Repository

### ✔ Step 3 — Register Request DTO

### ✔ Step 4 — Login Request DTO

### ✔ Step 5 — TokenResponse DTO

### Step 6 — AuthService (register + login)

### Step 7 — Password Hashing

### Step 8 — Key Loader (RSA private key)

### Step 9 — JWT signing (access + refresh)

### Step 10 — JWK publisher endpoint

### Step 11 — Refresh token API

### Step 12 — Exception handling

### Step 13 — Security Config

After Step 13 — **AUTH SERVER MVP IS COMPLETE.**

Then we go to production-grade features:

* refresh token persistence
* rate limiting
* key rotation
* monitoring

---

creating private.pem file for keys in resource/keys
openssl genrsa -out private.pem 2048
