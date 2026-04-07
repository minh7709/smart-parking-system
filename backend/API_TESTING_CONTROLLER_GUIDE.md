# API Testing Guide - All V1 Controllers

Tai lieu nay huong dan test API cho tung ham trong cac controller duoi `src/main/java/smartparkingsystem/backend/controller/v1`.

## 1) Chuan bi

- Base URL mac dinh: `http://localhost:8080`
- Header chung:
  - `Content-Type: application/json` (voi JSON API)
  - `Authorization: Bearer <ACCESS_TOKEN>` (voi endpoint can dang nhap)
- Dinh dang response thanh cong (theo `ApiResponse<T>`):

```json
{
  "success": true,
  "message": "...",
  "data": {},
  "timestamp": "2026-04-06T..."
}
```

- Dinh dang response loi (GlobalException):

```json
{
  "success": false,
  "message": "...",
  "errorCode": "...",
  "path": "/api/v1/...",
  "timestamp": "2026-04-06T...",
  "fieldErrors": []
}
```

## 2) Tao bien moi truong de test nhanh (PowerShell)

```powershell
$BASE = "http://localhost:8080"
$ADMIN_USER = "admin"
$ADMIN_PASS = "12345678Aa"
```

## 3) AuthController - `/api/v1/auth`

File: `src/main/java/smartparkingsystem/backend/controller/v1/auth/AuthController.java`

### 3.1 POST `/api/v1/auth/login`
Muc dich: Dang nhap, nhan `accessToken` va `refreshToken`.

**Request body**
```json
{
  "username": "admin",
  "password": "12345678Aa",
  "rememberMe": true
}
```

**Test**
```powershell
$loginBody = @{ username = $ADMIN_USER; password = $ADMIN_PASS; rememberMe = $true } | ConvertTo-Json
$loginRes = Invoke-RestMethod -Method Post -Uri "$BASE/api/v1/auth/login" -ContentType "application/json" -Body $loginBody
$loginRes | ConvertTo-Json -Depth 10
$ACCESS_TOKEN = $loginRes.data.accessToken
$REFRESH_TOKEN = $loginRes.data.refreshToken
```

### 3.2 POST `/api/v1/auth/refresh`
Muc dich: Lay access token moi bang refresh token.

```powershell
$refreshBody = @{ refreshToken = $REFRESH_TOKEN } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri "$BASE/api/v1/auth/refresh" -ContentType "application/json" -Body $refreshBody | ConvertTo-Json -Depth 10
```

### 3.3 POST `/api/v1/auth/logout`
Muc dich: Dang xuat (blacklist access token + xoa refresh token trong Redis).

```powershell
$logoutBody = @{ refreshToken = $REFRESH_TOKEN } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri "$BASE/api/v1/auth/logout" -Headers @{ Authorization = "Bearer $ACCESS_TOKEN" } -ContentType "application/json" -Body $logoutBody | ConvertTo-Json -Depth 10
```

### 3.4 GET `/api/v1/auth/me`
Muc dich: Lay thong tin user dang dang nhap.

```powershell
Invoke-RestMethod -Method Get -Uri "$BASE/api/v1/auth/me" -Headers @{ Authorization = "Bearer $ACCESS_TOKEN" } | ConvertTo-Json -Depth 10
```

### 3.5 POST `/api/v1/auth/forgot-password`
Muc dich: Gui OTP qua so dien thoai.

```powershell
$forgotBody = @{ phone = "0123456789" } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri "$BASE/api/v1/auth/forgot-password" -ContentType "application/json" -Body $forgotBody | ConvertTo-Json -Depth 10
```

### 3.6 POST `/api/v1/auth/verify-otp`
Muc dich: Xac thuc OTP, nhan reset token.

```powershell
$verifyBody = @{ phone = "0123456789"; otp = "123456" } | ConvertTo-Json
$verifyRes = Invoke-RestMethod -Method Post -Uri "$BASE/api/v1/auth/verify-otp" -ContentType "application/json" -Body $verifyBody
$verifyRes | ConvertTo-Json -Depth 10
$RESET_TOKEN = $verifyRes.data
```

### 3.7 POST `/api/v1/auth/reset-password`
Muc dich: Dat lai mat khau bang reset token.

```powershell
$resetBody = @{ newPassword = "Abcd1234"; token = $RESET_TOKEN } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri "$BASE/api/v1/auth/reset-password" -ContentType "application/json" -Body $resetBody | ConvertTo-Json -Depth 10
```

### 3.8 Test loi quan trong cho Auth
- Sai mat khau login -> mong doi `401/403` + message invalid credentials.
- Thieu `refreshToken` -> `400` + `fieldErrors`.
- Goi `/me` khong token -> `401`.

---

## 4) AdminController - `/api/v1/admin` (ROLE_ADMIN)

File: `src/main/java/smartparkingsystem/backend/controller/v1/admin/AdminController.java`

### 4.1 GET `/api/v1/admin/users`
```powershell
Invoke-RestMethod -Method Get -Uri "$BASE/api/v1/admin/users" -Headers @{ Authorization = "Bearer $ACCESS_TOKEN" } | ConvertTo-Json -Depth 10
```

### 4.2 GET `/api/v1/admin/users/{id}`
```powershell
$users = Invoke-RestMethod -Method Get -Uri "$BASE/api/v1/admin/users" -Headers @{ Authorization = "Bearer $ACCESS_TOKEN" }
$firstId = $users[0].id
Invoke-RestMethod -Method Get -Uri "$BASE/api/v1/admin/users/$firstId" -Headers @{ Authorization = "Bearer $ACCESS_TOKEN" } | ConvertTo-Json -Depth 10
```

### 4.3 GET `/api/v1/admin/dashboard`
```powershell
Invoke-RestMethod -Method Get -Uri "$BASE/api/v1/admin/dashboard" -Headers @{ Authorization = "Bearer $ACCESS_TOKEN" } | ConvertTo-Json -Depth 10
```

### 4.4 GET `/api/v1/admin/verify-access`
```powershell
Invoke-RestMethod -Method Get -Uri "$BASE/api/v1/admin/verify-access" -Headers @{ Authorization = "Bearer $ACCESS_TOKEN" } | ConvertTo-Json -Depth 10
```

### 4.5 Test loi quan trong cho Admin
- Dung token GUARD goi endpoint admin -> `403 FORBIDDEN`.
- Khong token -> `401`.

---

## 5) PricingRuleController - `/api/v1/admin/pricing-rules` (ROLE_ADMIN)

File: `src/main/java/smartparkingsystem/backend/controller/v1/admin/PricingRuleController.java`

### 5.1 POST `/api/v1/admin/pricing-rules`
```powershell
$ruleBody = @{
  ruleName = "Flat Rate Test"
  vehicleType = "CAR"
  pricingStrategy = "FLAT_RATE"
  basePrice = 30000
  penaltyFee = 200
  isActive = $true
} | ConvertTo-Json

$newRule = Invoke-RestMethod -Method Post -Uri "$BASE/api/v1/admin/pricing-rules" -Headers @{ Authorization = "Bearer $ACCESS_TOKEN" } -ContentType "application/json" -Body $ruleBody
$newRule | ConvertTo-Json -Depth 10
$RULE_ID = $newRule.data.id
```

### 5.2 PUT `/api/v1/admin/pricing-rules/{id}`
```powershell
$updateBody = @{
  ruleName = "Flat Rate Test Updated"
  vehicleType = "CAR"
  pricingStrategy = "FLAT_RATE"
  basePrice = 35000
  penaltyFee = 300
  isActive = $true
} | ConvertTo-Json

Invoke-RestMethod -Method Put -Uri "$BASE/api/v1/admin/pricing-rules/$RULE_ID" -Headers @{ Authorization = "Bearer $ACCESS_TOKEN" } -ContentType "application/json" -Body $updateBody | ConvertTo-Json -Depth 10
```

### 5.3 GET `/api/v1/admin/pricing-rules/{id}`
```powershell
Invoke-RestMethod -Method Get -Uri "$BASE/api/v1/admin/pricing-rules/$RULE_ID" -Headers @{ Authorization = "Bearer $ACCESS_TOKEN" } | ConvertTo-Json -Depth 10
```

### 5.4 GET `/api/v1/admin/pricing-rules?page=0&size=10&sort=active,desc&vehicleType=CAR`
```powershell
Invoke-RestMethod -Method Get -Uri "$BASE/api/v1/admin/pricing-rules?page=0&size=10&sort=active,desc&vehicleType=CAR" -Headers @{ Authorization = "Bearer $ACCESS_TOKEN" } | ConvertTo-Json -Depth 10
```

### 5.5 POST `/api/v1/admin/pricing-rules/{id}/activate`
```powershell
Invoke-RestMethod -Method Post -Uri "$BASE/api/v1/admin/pricing-rules/$RULE_ID/activate" -Headers @{ Authorization = "Bearer $ACCESS_TOKEN" } | ConvertTo-Json -Depth 10
```

### 5.6 POST `/api/v1/admin/pricing-rules/{id}/deactivate`
```powershell
Invoke-RestMethod -Method Post -Uri "$BASE/api/v1/admin/pricing-rules/$RULE_ID/deactivate" -Headers @{ Authorization = "Bearer $ACCESS_TOKEN" } | ConvertTo-Json -Depth 10
```

### 5.7 DELETE `/api/v1/admin/pricing-rules/{id}`
```powershell
Invoke-RestMethod -Method Delete -Uri "$BASE/api/v1/admin/pricing-rules/$RULE_ID" -Headers @{ Authorization = "Bearer $ACCESS_TOKEN" } | ConvertTo-Json -Depth 10
```

### 5.8 Test loi quan trong cho Pricing Rule
- Gui `vehicleType = "CARR"` -> `400 BAD_REQUEST` (enum mapping fail).
- Trung `ruleName` -> `409/400` tuy logic service.
- Thieu field bat buoc -> `400` + `fieldErrors`.

---

## 6) LaneController - `/api/v1/guard/active-lanes` (ROLE_GUARD)

File: `src/main/java/smartparkingsystem/backend/controller/v1/guard/LaneController.java`

### 6.1 GET `/api/v1/guard/active-lanes/`
Luu y endpoint nay co dau `/` o cuoi.

```powershell
# Can token GUARD
$GUARD_TOKEN = "<put-guard-access-token-here>"
Invoke-RestMethod -Method Get -Uri "$BASE/api/v1/guard/active-lanes/" -Headers @{ Authorization = "Bearer $GUARD_TOKEN" } | ConvertTo-Json -Depth 10
```

### 6.2 Test loi quan trong cho Lane
- Dung token ADMIN goi endpoint nay -> theo `@PreAuthorize("hasRole('GUARD')")` se `403`.
- Khong token -> `401`.

---

## 7) ParkingSessionController - `/api/v1/guard/parking-session` (ADMIN or GUARD)

File: `src/main/java/smartparkingsystem/backend/controller/v1/guard/ParkingSessionController.java`

> Khuyen nghi test bang Postman/Insomnia cho endpoint multipart de de gui JSON + image.

### 7.1 POST `/check-in` (multipart)
- Form-data:
  - `request` (text, content-type `application/json`)
  - `image` (file)

Gia tri `request`:
```json
{
  "entryLaneId": "11111111-1111-1111-1111-111111111111",
  "vehicleType": "CAR"
}
```

cURL mau:
```bash
curl -X POST "http://localhost:8080/api/v1/guard/parking-session/check-in" \
  -H "Authorization: Bearer <TOKEN>" \
  -F 'request={"entryLaneId":"11111111-1111-1111-1111-111111111111","vehicleType":"CAR"};type=application/json' \
  -F "image=@C:/temp/car-in.jpg"
```

### 7.2 POST `/check-out` (multipart)
`request`:
```json
{
  "exitLaneId": "22222222-2222-2222-2222-222222222222",
  "parkingSessionId": "33333333-3333-3333-3333-333333333333"
}
```

```bash
curl -X POST "http://localhost:8080/api/v1/guard/parking-session/check-out" \
  -H "Authorization: Bearer <TOKEN>" \
  -F 'request={"exitLaneId":"22222222-2222-2222-2222-222222222222","parkingSessionId":"33333333-3333-3333-3333-333333333333"};type=application/json' \
  -F "image=@C:/temp/car-out.jpg"
```

### 7.3 POST `/confirm-check-in`
```powershell
$confirmInBody = @{
  entryLaneId = "11111111-1111-1111-1111-111111111111"
  finalPlate = "59A12345"
  parkingSessionId = "33333333-3333-3333-3333-333333333333"
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri "$BASE/api/v1/guard/parking-session/confirm-check-in" -Headers @{ Authorization = "Bearer $ACCESS_TOKEN" } -ContentType "application/json" -Body $confirmInBody | ConvertTo-Json -Depth 10
```

### 7.4 POST `/confirm-check-out`
```powershell
$confirmOutBody = @{
  finalPlate = "59A12345"
  paymentMethod = "CASH"
  parkingSessionId = "33333333-3333-3333-3333-333333333333"
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri "$BASE/api/v1/guard/parking-session/confirm-check-out" -Headers @{ Authorization = "Bearer $ACCESS_TOKEN" } -ContentType "application/json" -Body $confirmOutBody | ConvertTo-Json -Depth 10
```

### 7.5 POST `/report-incident/lost-card`
Controller hien tai dung `@RequestBody` + `@RequestPart("image")`; can gui payload phu hop voi cau hinh parser hien tai.

Body JSON:
```json
{
  "exitLaneId": "22222222-2222-2222-2222-222222222222",
  "description": "Mat the xe"
}
```

Neu endpoint loi parse request, nen doi method nay sang multipart dong nhat voi check-in/check-out.

### 7.6 GET `/{plate}`
```powershell
Invoke-RestMethod -Method Get -Uri "$BASE/api/v1/guard/parking-session/59A12345" -Headers @{ Authorization = "Bearer $ACCESS_TOKEN" } | ConvertTo-Json -Depth 10
```

### 7.7 GET `/api/v1/guard/parking-session?page=0&size=10&status=IN_PARKING`
```powershell
Invoke-RestMethod -Method Get -Uri "$BASE/api/v1/guard/parking-session?page=0&size=10&status=IN_PARKING" -Headers @{ Authorization = "Bearer $ACCESS_TOKEN" } | ConvertTo-Json -Depth 10
```

### 7.8 Test loi quan trong cho Parking Session
- `status` sai enum -> `400`.
- `parkingSessionId` khong ton tai -> `404/400` tuy service.
- Khong gui image cho endpoint multipart -> `400`.

---

## 8) TypeController - `/api/v1/type` (ADMIN or GUARD)

File: `src/main/java/smartparkingsystem/backend/controller/v1/type/TypeController.java`

Tat ca endpoint deu GET va khong can request body:

1. `/api/v1/type/lane-statuses`
2. `/api/v1/type/lane-types`
3. `/api/v1/type/vehicle-types`
4. `/api/v1/type/session-statuses`
5. `/api/v1/type/payment-statuses`
6. `/api/v1/type/payment-methods`
7. `/api/v1/type/pricing-strategies`
8. `/api/v1/type/incident-types`
9. `/api/v1/type/user-roles`
10. `/api/v1/type/user-statuses`
11. `/api/v1/type/subscription-types`
12. `/api/v1/type/subscription-statuses`

**Test nhanh tat ca**
```powershell
$paths = @(
  "/api/v1/type/lane-statuses",
  "/api/v1/type/lane-types",
  "/api/v1/type/vehicle-types",
  "/api/v1/type/session-statuses",
  "/api/v1/type/payment-statuses",
  "/api/v1/type/payment-methods",
  "/api/v1/type/pricing-strategies",
  "/api/v1/type/incident-types",
  "/api/v1/type/user-roles",
  "/api/v1/type/user-statuses",
  "/api/v1/type/subscription-types",
  "/api/v1/type/subscription-statuses"
)

foreach ($p in $paths) {
  Write-Host "Testing $p"
  Invoke-RestMethod -Method Get -Uri "$BASE$p" -Headers @{ Authorization = "Bearer $ACCESS_TOKEN" } | ConvertTo-Json -Depth 10
}
```

---

## 9) Checklist regression nhanh (de test lai sau moi lan fix)

- [ ] Login thanh cong, lay duoc ca access + refresh token.
- [ ] Refresh token thanh cong, access token moi su dung duoc.
- [ ] Logout xong, access token cu khong dung lai duoc.
- [ ] Token ADMIN vao duoc nhom `/api/v1/admin/**`.
- [ ] Token GUARD vao duoc lane + parking session endpoint guard.
- [ ] API enum (`/api/v1/type/**`) tra ve day du list enum.
- [ ] Pricing rule CRUD + activate/deactivate hoat dong dung.
- [ ] Cac test negative (enum sai, field thieu, khong token) tra ve dung format loi.

## 10) Ghi chu quan trong khi test

- Theo code hien tai, `DataInitializer` tao san user admin (`admin` / `12345678Aa`) neu bang user trong DB dang rong.
- `LaneController` yeu cau role `GUARD` tuyet doi; ADMIN se bi `403` neu goi endpoint nay.
- Endpoint multipart nen test bang Postman hoac curl de chu dong ve `request` + `image`.
- Neu can log chi tiet loi JSON mapping enum/request body, kiem tra `GlobalExceptionHandler` de dam bao tra ve dung `ApiResponse`.

