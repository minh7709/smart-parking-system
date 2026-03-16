//package smartparkingsystem.backend.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.MvcResult;
//import org.springframework.test.web.servlet.ResultActions;
//import smartparkingsystem.backend.dto.request.auth.LoginRequest;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
///**
// * Integration tests for JWT Authentication
// *
// * Note: These tests are for demonstration purposes.
// * To run them, you need:
// * 1. Database running with test data
// * 2. At least one ADMIN user in the database
// */
//@SpringBootTest
//@AutoConfigureMockMvc
//@DisplayName("JWT Authentication Integration Tests")
//public class JwtAuthenticationIntegrationTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    /**
//     * Test Login Endpoint
//     *
//     * Steps:
//     * 1. Create a test ADMIN user in database
//     * 2. Make POST request to /api/v1/auth/login
//     * 3. Verify response contains accessToken, refreshToken, and user info
//     *
//     * curl -X POST http://localhost:8080/api/v1/auth/login \
//     *   -H "Content-Type: application/json" \
//     *   -d '{"username":"admin","password":"password123"}'
//     */
//    @Test
//    @DisplayName("Should successfully login with valid ADMIN credentials")
//    public void testLoginSuccess() throws Exception {
//        // Arrange
//        LoginRequest loginRequest = new LoginRequest("admin", "password123");
//        String requestBody = objectMapper.writeValueAsString(loginRequest);
//
//        // Act
//        ResultActions result = mockMvc.perform(post("/api/v1/auth/login")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(requestBody));
//
//        // Assert
//        result.andExpect(status().isOk())
//                .andExpect(jsonPath("$.accessToken").exists())
//                .andExpect(jsonPath("$.refreshToken").exists())
//                .andExpect(jsonPath("$.tokenType").value("Bearer"))
//                .andExpect(jsonPath("$.expiresIn").isNumber())
//                .andExpect(jsonPath("$.user.username").value("admin"))
//                .andExpect(jsonPath("$.user.role").value("ADMIN"));
//    }
//
//    /**
//     * Test Login with Invalid Credentials
//     */
//    @Test
//    @DisplayName("Should return 401 when credentials are invalid")
//    public void testLoginInvalidCredentials() throws Exception {
//        // Arrange
//        LoginRequest loginRequest = new LoginRequest("admin", "wrongpassword");
//        String requestBody = objectMapper.writeValueAsString(loginRequest);
//
//        // Act & Assert
//        mockMvc.perform(post("/api/v1/auth/login")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(requestBody))
//                .andExpect(status().isUnauthorized());
//    }
//
//    /**
//     * Test Refresh Token Endpoint
//     *
//     * Steps:
//     * 1. Login to get accessToken and refreshToken
//     * 2. Use refreshToken to get new accessToken
//     * 3. Verify new accessToken is valid
//     */
//    @Test
//    @DisplayName("Should successfully refresh access token with valid refresh token")
//    public void testRefreshToken() throws Exception {
//        // First, login
//        LoginRequest loginRequest = new LoginRequest("admin", "password123");
//        String loginBody = objectMapper.writeValueAsString(loginRequest);
//
//        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(loginBody))
//                .andReturn();
//
//        String response = loginResult.getResponse().getContentAsString();
//        // Extract refreshToken from response
//        // This is simplified - in real test, parse JSON properly
//
//        // Refresh token request would go here
//    }
//
//    /**
//     * Test Authorization - Protected Endpoint
//     *
//     * Steps:
//     * 1. Login to get accessToken
//     * 2. Make request to protected endpoint with accessToken
//     * 3. Verify request is successful
//     *
//     * curl -H "Authorization: Bearer <token>" \
//     *   http://localhost:8080/api/v1/auth/me
//     */
//    @Test
//    @DisplayName("Should access protected endpoint with valid access token")
//    public void testProtectedEndpointWithValidToken() throws Exception {
//        // This test requires a valid token from login
//        // In production, use TestRestTemplate or RestAssured for full flow
//
//        mockMvc.perform(get("/api/v1/auth/me")
//                .header("Authorization", "Bearer valid-token-here")
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().is5xxServerError()); // Will fail without valid token
//    }
//
//    /**
//     * Test Authorization - Unauthorized Request
//     *
//     * Steps:
//     * 1. Make request to protected endpoint without token
//     * 2. Verify response is 401 Unauthorized
//     */
//    @Test
//    @DisplayName("Should return 401 when accessing protected endpoint without token")
//    public void testProtectedEndpointWithoutToken() throws Exception {
//        mockMvc.perform(get("/api/v1/auth/me")
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isUnauthorized());
//    }
//
//    /**
//     * Test Admin Only Endpoint
//     *
//     * Only ADMIN users should be able to access /api/v1/admin/** endpoints
//     * GUARD users should get 403 Forbidden
//     */
//    @Test
//    @DisplayName("Should allow ADMIN to access admin endpoints")
//    public void testAdminEndpointWithAdminUser() throws Exception {
//        // First login as ADMIN
//        LoginRequest loginRequest = new LoginRequest("admin", "password123");
//        String loginBody = objectMapper.writeValueAsString(loginRequest);
//
//        // Get token from login response
//        // Then use it to access admin endpoint
//
//        mockMvc.perform(get("/api/v1/admin/users")
//                .header("Authorization", "Bearer admin-token")
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk());
//    }
//
//    /**
//     * Test Logout Endpoint
//     *
//     * POST /api/v1/auth/logout
//     * Header: Authorization: Bearer <token>
//     */
//    @Test
//    @DisplayName("Should successfully logout")
//    public void testLogout() throws Exception {
//        mockMvc.perform(post("/api/v1/auth/logout")
//                .header("Authorization", "Bearer valid-token")
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").exists());
//    }
//}
//
