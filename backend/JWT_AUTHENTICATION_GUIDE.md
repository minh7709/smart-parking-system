# JWT Authentication & Authorization Guide

## Tổng quan

Hệ thống Smart Parking đã được cấu hình với JWT (JSON Web Token) authentication. Chỉ **ADMIN** có thể đăng nhập qua web portal. **GUARD** sẽ đăng nhập qua desktop app.

## Kiến trúc

### 1. **Authentication Flow**
```
Client (Web) → Login Request → AuthController → AuthService → AuthenticationManager
                                                                ↓
                                                    CustomUserDetailsService
                                                    (Load User from DB)
                                                                ↓
                                                    Password Validation
                                                    (BCrypt)
                                                                ↓
                                                    JWT Token Generation
```

### 2. **Authorization Flow**
```
Client Request (with JWT Token)
        ↓
JwtAuthenticationFilter (Extract Token from Header)
        ↓
JwtTokenProvider (Validate & Parse Token)
        ↓
SecurityContext (Set Authentication)
        ↓
Authorization Rules (Check Role: ADMIN, GUARD)
        ↓
Protected Endpoint Access
```

## API Endpoints

### Login
**POST** `/api/v1/auth/login`

**Request Body:**
```json
{
  "username": "admin",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
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

**Error Cases:**
- `401 Unauthorized`: Invalid username/password
- `401 Unauthorized`: User is not ADMIN (GUARD cannot login via web)
- `400 Bad Request`: Missing/invalid request parameters

### Refresh Token
**POST** `/api/v1/auth/refresh`

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": { ... }
}
```

### Logout
**POST** `/api/v1/auth/logout`

**Headers:**
```
Authorization: Bearer <accessToken>
```

**Response (200 OK):**
```json
{
  "message": "Logged out successfully"
}
```

### Get Current User
**GET** `/api/v1/auth/me`

**Headers:**
```
Authorization: Bearer <accessToken>
```

**Response (200 OK):**
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

## Configuration

### JWT Settings (application.yml)
```yaml
jwt:
  secret: your-secret-key-change-this-in-production-must-be-at-least-256-bits
  expiration: 86400000        # 24 hours in milliseconds
  refresh-expiration: 604800000 # 7 days in milliseconds
```

**Production Security:**
- Thay đổi `secret` thành một key dài (≥256 bits)
- Lưu trữ secret trong environment variables hoặc secret management system
- Sử dụng HTTPS để transmit tokens

## Client Implementation

### JavaScript/Frontend Example

```javascript
// Login
async function login(username, password) {
  const response = await fetch('/api/v1/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ username, password })
  });
  
  const data = await response.json();
  
  if (response.ok) {
    // Store tokens in localStorage
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('refreshToken', data.refreshToken);
    localStorage.setItem('expiresIn', data.expiresIn);
    localStorage.setItem('user', JSON.stringify(data.user));
    return data;
  } else {
    throw new Error(data.message);
  }
}

// Make authenticated request
async function makeAuthenticatedRequest(endpoint, options = {}) {
  let token = localStorage.getItem('accessToken');
  
  const headers = {
    ...options.headers,
    'Authorization': `Bearer ${token}`
  };
  
  let response = await fetch(endpoint, {
    ...options,
    headers
  });
  
  // If token expired, try to refresh
  if (response.status === 401) {
    const refreshToken = localStorage.getItem('refreshToken');
    const refreshResponse = await fetch('/api/v1/auth/refresh', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ refreshToken })
    });
    
    if (refreshResponse.ok) {
      const newData = await refreshResponse.json();
      localStorage.setItem('accessToken', newData.accessToken);
      
      // Retry original request with new token
      headers['Authorization'] = `Bearer ${newData.accessToken}`;
      response = await fetch(endpoint, {
        ...options,
        headers
      });
    } else {
      // Redirect to login
      window.location.href = '/login';
    }
  }
  
  return response;
}

// Usage
const result = await makeAuthenticatedRequest('/api/v1/admin/users');
const data = await result.json();
```

## Security Features

### 1. **Password Encoding**
- Sử dụng BCrypt để mã hóa password
- Không bao giờ lưu trữ plain-text password

### 2. **JWT Token Security**
- Sử dụng HS512 (HMAC with SHA-512) signing algorithm
- Access token: 24 giờ expiration
- Refresh token: 7 ngày expiration
- Stateless authentication (không cần session)

### 3. **Authorization**
- Role-based access control (RBAC)
- ADMIN: Có quyền truy cập `/api/v1/admin/**`
- GUARD: Chỉ có quyền truy cập desktop app sync endpoints

### 4. **Token Validation**
- Verify signature (chắc chắn token không bị giả mạo)
- Check expiration time
- Extract user info từ claims
- Validate role từ claims

## Folder Structure

```
src/main/java/smartparkingsystem/backend/
├── security/
│   ├── JwtTokenProvider.java          # Token generation & validation
│   ├── CustomUserDetails.java         # Spring Security UserDetails
│   ├── CustomUserDetailsService.java  # Load user from database
│   ├── JwtAuthenticationFilter.java   # Extract & validate JWT from requests
│   └── JwtAuthenticationEntryPoint.java # Handle auth errors
├── config/
│   ├── SecurityConfig.java            # Spring Security configuration
│   └── JwtProperties.java             # JWT settings from application.yml
├── controller/v1/auth/
│   └── AuthController.java            # Login, refresh, logout endpoints
├── service/auth/
│   └── AuthService.java               # Business logic for authentication
├── dto/
│   ├── request/
│   │   ├── LoginRequest.java
│   │   └── RefreshTokenRequest.java
│   └── response/
│       └── LoginResponse.java
└── repository/
    └── UserRepository.java            # Database access for users
```

## Testing

### Test Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password123"
  }'
```

### Test Protected Endpoint
```bash
curl -H "Authorization: Bearer <accessToken>" \
  http://localhost:8080/api/v1/auth/me
```

### Test Refresh Token
```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "<refreshToken>"
  }'
```

## Troubleshooting

### Token Expired
- Response: `401 Unauthorized`
- Solution: Sử dụng refresh token để lấy access token mới

### Invalid Token
- Response: `401 Unauthorized`
- Causes:
  - Token bị giả mạo
  - Token signature không hợp lệ
  - Token format sai

### Access Denied (Insufficient Permissions)
- Response: `403 Forbidden`
- Solution: GUARD accounts cần đăng nhập qua desktop app, không qua web

### User Not Found
- Response: `401 Unauthorized`
- Solution: Kiểm tra username có tồn tại trong database không

## Future Enhancements

1. **Token Blacklist**: Implement blacklist để revoke tokens immediately
2. **2FA**: Thêm two-factor authentication
3. **API Keys**: Support API keys cho desktop app
4. **Audit Logging**: Log tất cả authentication events
5. **Rate Limiting**: Limit login attempts để prevent brute force attacks
6. **CORS Configuration**: Configure CORS cho frontend domain

