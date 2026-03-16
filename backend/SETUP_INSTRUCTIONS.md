# Smart Parking System - JWT Authentication Setup Guide

## ✅ Hoàn Thành Features

### 1. **JWT Token Management**
- ✅ Generate Access Token (24 hours)
- ✅ Generate Refresh Token (7 days)
- ✅ Token validation with signature verification
- ✅ Extract user info from tokens
- ✅ Automatic token expiration

### 2. **Authentication**
- ✅ Username/Password authentication
- ✅ BCrypt password encoding
- ✅ User details loading from database
- ✅ Authentication manager configuration
- ✅ Login endpoint with credentials validation

### 3. **Authorization**
- ✅ Role-based access control (RBAC)
- ✅ ADMIN-only access to `/api/v1/admin/**`
- ✅ GUARD restriction from web login (desktop app only)
- ✅ Method-level security with `@PreAuthorize`
- ✅ Customizable authorization rules

### 4. **Security Filter Chain**
- ✅ JWT Authentication Filter
- ✅ Stateless session management
- ✅ CSRF protection disabled (for API)
- ✅ Custom authentication entry point
- ✅ Proper error handling

### 5. **API Endpoints**
- ✅ POST `/api/v1/auth/login` - User login
- ✅ POST `/api/v1/auth/refresh` - Refresh access token
- ✅ POST `/api/v1/auth/logout` - User logout
- ✅ GET `/api/v1/auth/me` - Current user info
- ✅ GET `/api/v1/admin/**` - Admin-only endpoints
- ✅ POST `/api/v1/admin/**` - Admin-only operations

## 📂 Files Created

### Security Components
```
src/main/java/smartparkingsystem/backend/
├── security/
│   ├── JwtTokenProvider.java              # Token generation & validation
│   ├── CustomUserDetails.java             # Spring Security UserDetails
│   ├── CustomUserDetailsService.java      # Load user from database
│   ├── JwtAuthenticationFilter.java       # Extract & validate JWT
│   └── JwtAuthenticationEntryPoint.java   # Handle auth errors
├── config/
│   ├── SecurityConfig.java                # Spring Security configuration
│   └── JwtProperties.java                 # JWT properties from YAML
├── controller/v1/
│   ├── auth/AuthController.java           # Login/refresh/logout
│   └── admin/AdminController.java         # Admin endpoints (demo)
├── service/auth/
│   └── AuthService.java                   # Auth business logic
├── dto/
│   ├── request/
│   │   ├── LoginRequest.java
│   │   └── RefreshTokenRequest.java
│   └── response/
│       └── LoginResponse.java
└── repository/
    └── UserRepository.java                # User database access
```

### Configuration & Documentation
```
├── src/main/resources/
│   └── application.yml                    # Updated with JWT config
├── JWT_AUTHENTICATION_GUIDE.md            # Complete usage guide
└── SETUP_INSTRUCTIONS.md                  # This file
```

### Tests
```
src/test/java/smartparkingsystem/backend/
└── controller/
    └── JwtAuthenticationIntegrationTest.java
```

## 🔧 Configuration Details

### 1. **application.yml** - JWT Settings
```yaml
jwt:
  secret: your-secret-key-change-this-in-production-must-be-at-least-256-bits
  expiration: 86400000        # 24 hours
  refresh-expiration: 604800000 # 7 days
```

**⚠️ IMPORTANT for Production:**
- Change the `secret` to a long, random string (≥256 bits)
- Example: `openssl rand -base64 32` to generate a secure key
- Store in environment variable: `export JWT_SECRET="..."`
- Update YAML: `secret: ${JWT_SECRET}`

### 2. **SecurityConfig.java** - Security Configuration
- Password encoder: BCrypt
- Session management: Stateless (JWT)
- CORS: Can be configured per domain
- Authorization rules:
  ```
  /api/v1/auth/**      → Public (no auth required)
  /api/v1/admin/**     → ADMIN only
  /api/v1/sync/**      → Desktop app sync (public)
  Others               → Authenticated users
  ```

### 3. **Database Requirements**
Ensure the `users` table exists with these columns:
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    fullname VARCHAR(100) NOT NULL,
    phone VARCHAR(11) NOT NULL,
    role VARCHAR(20) NOT NULL, -- ENUM: ADMIN, GUARD
    status VARCHAR(20) NOT NULL, -- ENUM: ACTIVE, INACTIVE
    created_at TIMESTAMP NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT false
);

-- Sample data: Create an ADMIN user
-- Password: Use BCryptPasswordEncoder to hash "password123"
-- You can do this in Java code first, then insert
INSERT INTO users (id, username, password, fullname, phone, role, status, created_at, is_deleted)
VALUES (
    '550e8400-e29b-41d4-a716-446655440000',
    'admin',
    '$2a$10$...', -- BCrypt hashed "password123"
    'Admin User',
    '0123456789',
    'ADMIN',
    'ACTIVE',
    NOW(),
    false
);
```

## 🚀 How to Get Started

### Step 1: Update Dependencies
```xml
<!-- Already included in pom.xml -->
<!-- JJWT libraries for JWT operations -->
<!-- Jackson for JSON serialization -->
<!-- Spring Security starter -->
```

### Step 2: Configure JWT Secret
```bash
# Generate a secure secret
openssl rand -base64 32

# Update application.yml
jwt:
  secret: your-generated-secret-here
  expiration: 86400000
  refresh-expiration: 604800000
```

### Step 3: Set Up Database
1. Create database and users table (see SQL above)
2. Insert at least one ADMIN user
3. Test database connection in Spring Boot

### Step 4: Build and Run
```bash
# Build
./mvnw clean install

# Run
./mvnw spring-boot:run

# Or run JAR
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

### Step 5: Test Authentication
```bash
# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}'

# Copy the accessToken from response

# Test Protected Endpoint
curl -H "Authorization: Bearer <accessToken>" \
  http://localhost:8080/api/v1/auth/me

# Test Admin Endpoint
curl -H "Authorization: Bearer <accessToken>" \
  http://localhost:8080/api/v1/admin/users
```

## 📋 API Request/Response Examples

### Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJJZCI6IjU1MGU4NDAwLWUyOWItNDFkNC1hNzE2LTQ0NjY1NTQ0MDAwMCIsInJvbGUiOiJBRE1JTiIsImlzUmVmcmVzaFRva2VuIjpmYWxzZSwiaWF0IjoxNjcwMTAwMDAwLCJleHAiOjE2NzAxMDAwMDB9.signature",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlzUmVmcmVzaFRva2VuIjp0cnVlfQ.signature",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "admin",
    "fullName": "Admin User",
    "role": "ADMIN",
    "status": "ACTIVE"
  }
}
```

### Get Current User
```bash
curl -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..." \
  http://localhost:8080/api/v1/auth/me
```

**Response:**
```json
{
  "username": "admin",
  "roles": [
    {
      "authority": "ROLE_ADMIN"
    }
  ]
}
```

## 🔐 Security Best Practices

### For Development
```yaml
jwt:
  secret: dev-secret-key-change-before-production
  expiration: 86400000
  refresh-expiration: 604800000
```

### For Production
```yaml
jwt:
  secret: ${JWT_SECRET}  # From environment variable
  expiration: 86400000  # 24 hours
  refresh-expiration: 604800000  # 7 days
```

**Environment Variables Setup:**
```bash
export JWT_SECRET="$(openssl rand -base64 32)"
export SPRING_DATASOURCE_PASSWORD="secure_password"
export SPRING_DATASOURCE_URL="jdbc:postgresql://prod-db:5432/parking_db"
```

### Password Hashing
- Framework: Spring Security (BCrypt)
- Algorithm: BCrypt with strength 10
- Passwords are hashed and never stored in plain text
- Use `PasswordEncoder` bean to hash passwords:
  ```java
  @Autowired
  private PasswordEncoder passwordEncoder;
  
  String hashedPassword = passwordEncoder.encode("plainTextPassword");
  ```

### Token Security
- ✅ Signed with HMAC-SHA512
- ✅ Contains expiration time
- ✅ Stateless (no session needed)
- ✅ Cannot be modified without valid signature
- ✅ Refresh token for token rotation

## 🐛 Troubleshooting

### 1. Login Returns 401 Unauthorized
**Cause:** Wrong password or user not found
**Solution:** 
- Check username exists in database
- Verify password is correct
- Ensure user status is ACTIVE

### 2. Token Expired Error
**Cause:** Access token expires after 24 hours
**Solution:**
- Use refresh token to get new access token
- Implement automatic token refresh in client

### 3. Access Denied (403)
**Cause:** User is not ADMIN
**Solution:**
- GUARD users cannot login via web
- GUARD users should use desktop app
- Check user role in database

### 4. Database Connection Error
**Cause:** PostgreSQL not running or wrong connection string
**Solution:**
- Start PostgreSQL service
- Verify connection URL in application.yml
- Check credentials

## 📚 Additional Resources

### JWT Structure
- **Header:** Algorithm (HS512) and token type (JWT)
- **Payload:** Claims (username, userId, role, expiration)
- **Signature:** HMAC-SHA512 with secret key

### Spring Security Flow
```
Request → Filter Chain → JwtAuthenticationFilter → JwtTokenProvider
→ CustomUserDetailsService → SecurityContext → Authorization
```

## ✨ What's Next?

### Recommended Enhancements
1. **Token Blacklist** - Revoke tokens immediately on logout
2. **2FA** - Two-factor authentication
3. **Rate Limiting** - Prevent brute force attacks
4. **Audit Logging** - Log all authentication events
5. **API Keys** - For service-to-service communication
6. **CORS** - Configure for frontend domain
7. **Refresh Token Rotation** - Issue new refresh token on refresh

### For Desktop App
- Desktop app can use `/api/v1/sync/**` endpoints
- Implement similar JWT auth for desktop (or API keys)
- Sync data bidirectionally with web backend

## 📞 Support

For issues or questions:
1. Check JWT_AUTHENTICATION_GUIDE.md for detailed documentation
2. Review test cases in JwtAuthenticationIntegrationTest.java
3. Check Spring Security documentation
4. Review JJWT documentation

---

**Status:** ✅ **JWT Authentication System is Ready for Development and Testing**

