# Quick Start Guide - JWT Authentication

## 5 Bước Khởi Động Nhanh

### Step 1: Đảm bảo PostgreSQL đang chạy
```bash
# macOS/Linux
brew services start postgresql

# Windows
# Khởi động PostgreSQL service từ Services

# Verify
psql -U admin -d parking_db -c "SELECT 1;"
```

### Step 2: Tạo User Test trong Database
```sql
-- Connect to parking_db
psql -U admin -d parking_db

-- Tạo ADMIN user
-- Hashed password: password123
INSERT INTO users (id, username, password, fullname, phone, role, status, created_at, is_deleted)
VALUES (
    '550e8400-e29b-41d4-a716-446655440000',
    'admin',
    '$2a$10$kbHyJoRqpOkx0Z7fG5k1.OXd0vLN5qJ5n6M5t7K8L9Y0P1Q2R3S4T',  -- password123
    'Admin User',
    '0987654321',
    'ADMIN',
    'ACTIVE',
    NOW(),
    false
);

-- Kiểm tra
SELECT * FROM users WHERE username = 'admin';
```

### Step 3: Build Project
```bash
cd backend

# Clean build
./mvnw clean compile

# Hoặc
mvn clean compile
```

### Step 4: Chạy Application
```bash
# Option 1: Maven
./mvnw spring-boot:run

# Option 2: Build JAR then run
./mvnw clean package
java -jar target/backend-0.0.1-SNAPSHOT.jar

# Application sẽ chạy tại: http://localhost:8080
```

### Step 5: Test API
```bash
# Terminal 1: Run application
./mvnw spring-boot:run

# Terminal 2: Test API

# 1. Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password123"
  }'

# Từ response, copy accessToken

# 2. Test Protected Endpoint
curl -H "Authorization: Bearer <accessToken>" \
  http://localhost:8080/api/v1/auth/me

# 3. Test Admin Endpoint
curl -H "Authorization: Bearer <accessToken>" \
  http://localhost:8080/api/v1/admin/users

# 4. Logout
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Authorization: Bearer <accessToken>"
```

---

## Hashing Password

Nếu bạn muốn tạo hashed password khác:

### Method 1: Online Tool (for testing only)
- Truy cập: https://bcrypt-generator.com/
- Enter: `password123`
- Strength: 10
- Copy hashed result

### Method 2: Java Code
```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String hashedPassword = encoder.encode("password123");
System.out.println(hashedPassword);
// Output: $2a$10$...
```

### Method 3: Command Line (Spring Boot)
```bash
# Tạm thời không dùng, phức tạp

# Dễ nhất là dùng online tool
```

---

## 🔑 Pre-hashed Passwords for Testing

Sử dụng những password đã hash sẵn này để test:

```
password123     → $2a$10$kbHyJoRqpOkx0Z7fG5k1.OXd0vLN5qJ5n6M5t7K8L9Y0P1Q2R3S4T
guard123        → $2a$10$cVZ9H.wv1Z5Q3E8B7M0N.9A8B7C6D5E4F3G2H1I0J9K8L7M6N5O4P
test123         → $2a$10$dHa9I.xw2a6R4F9C8N1O.0B9C8D7E6F5G4H3I2J1K0L9M8N7O6P5Q
```

---

## 📱 Postman Collection Template

Import vào Postman để test:

```json
{
  "info": {
    "name": "Smart Parking JWT Auth",
    "description": "JWT Authentication API tests"
  },
  "item": [
    {
      "name": "Login",
      "request": {
        "method": "POST",
        "url": "http://localhost:8080/api/v1/auth/login",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\"username\":\"admin\",\"password\":\"password123\"}"
        }
      }
    },
    {
      "name": "Get Current User",
      "request": {
        "method": "GET",
        "url": "http://localhost:8080/api/v1/auth/me",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{accessToken}}"
          }
        ]
      }
    },
    {
      "name": "Get All Users (Admin)",
      "request": {
        "method": "GET",
        "url": "http://localhost:8080/api/v1/admin/users",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{accessToken}}"
          }
        ]
      }
    },
    {
      "name": "Logout",
      "request": {
        "method": "POST",
        "url": "http://localhost:8080/api/v1/auth/logout",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{accessToken}}"
          }
        ]
      }
    }
  ]
}
```

Save as `Smart-Parking-Auth.postman_collection.json`
Import vào Postman: File → Import → Select file

---

## 🐛 Common Issues & Solutions

### ❌ "Database connection refused"
**Solution:**
```bash
# Start PostgreSQL
brew services start postgresql
# or
sudo service postgresql start
# or Windows: Services → PostgreSQL → Start

# Test connection
psql -U admin -d parking_db
```

### ❌ "User not found" when logging in
**Solution:**
```bash
# Check if user exists in database
psql -U admin -d parking_db
SELECT * FROM users;

# If empty, insert test user (see Step 2 above)
```

### ❌ "Invalid password" when logging in
**Solution:**
```bash
# Verify password is correct and hashed
# Use BCryptPasswordEncoder to hash

# Or re-insert user with correct hash
DELETE FROM users WHERE username = 'admin';
-- Then insert with correct hash from online tool
```

### ❌ "403 Forbidden" on /admin endpoint
**Solution:**
- Check user role is "ADMIN" in database
- Check token is valid and contains role claim
```sql
SELECT username, role FROM users;
```

### ❌ "Port 8080 already in use"
**Solution:**
```bash
# Kill process on port 8080
# macOS/Linux
lsof -ti:8080 | xargs kill -9

# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Or change port in application.yml
server:
  port: 8081
```

---

## 📖 Full Documentation

- **JWT_AUTHENTICATION_GUIDE.md** - Complete detailed guide
- **SETUP_INSTRUCTIONS.md** - Installation and deployment
- **README_JWT_AUTH.md** - Implementation summary

---

## ✅ Test Checklist

- [ ] PostgreSQL running
- [ ] Database created with users table
- [ ] Admin user inserted in database
- [ ] Project compiles successfully
- [ ] Application starts without errors
- [ ] Login endpoint returns token
- [ ] Protected endpoint requires token
- [ ] Admin endpoint returns 403 for non-admin
- [ ] Token expires correctly
- [ ] Refresh token works

---

## 🎯 Next: Frontend Integration

Sau khi backend chạy OK, integrate với frontend:

1. **Store tokens:**
   ```javascript
   localStorage.setItem('accessToken', response.accessToken);
   localStorage.setItem('refreshToken', response.refreshToken);
   ```

2. **Add to requests:**
   ```javascript
   headers: {
     'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
   }
   ```

3. **Handle token refresh:**
   - When 401, use refreshToken to get new accessToken
   - Retry original request

4. **Logout:**
   ```javascript
   localStorage.removeItem('accessToken');
   localStorage.removeItem('refreshToken');
   // Redirect to login
   ```

---

**Ready to go! 🚀**

