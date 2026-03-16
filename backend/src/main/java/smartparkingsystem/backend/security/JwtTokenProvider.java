package smartparkingsystem.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import smartparkingsystem.backend.config.JwtProperties;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
@AllArgsConstructor
@Slf4j
public class JwtTokenProvider {
    private final JwtProperties jwtProperties;

    /**
     * Generate JWT token from authentication
     */
    public String generateToken(Authentication authentication) {
        CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();
        return createToken(userPrincipal.getUsername(), userPrincipal.getId().toString(), userPrincipal.getRole().name(), false, false);
    }

    /**
     * Generate JWT token from username and userId
     */
    public String generateToken(String username, String userId, String role) {
        return createToken(username, userId, role, false, false);
    }

    /**
     * Generate refresh token with Remember Me option
     * Remember Me extends the refresh token expiration
     */
    public String generateRefreshToken(String username, UUID userId, Object role, boolean rememberMe) {
        String roleStr = (role instanceof String) ? (String) role : role.toString();
        return createToken(username, userId.toString(), roleStr, true, rememberMe);
    }

    /**
     * Create JWT token
     */
    private String createToken(String username, String userId, String role, boolean isRefreshToken, boolean rememberMe) {
        Date now = new Date();
        long expirationMs;

        if (isRefreshToken) {
            // Use rememberMe expiration if enabled, otherwise use default refresh expiration
            expirationMs = rememberMe ? jwtProperties.getRememberMeExpiration() : jwtProperties.getRefreshExpiration();
        } else {
            expirationMs = jwtProperties.getExpiration();
        }

        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("role", role)
                .claim("isRefreshToken", isRefreshToken)
                .claim("rememberMe", rememberMe)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Get username from JWT token
     */
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Get userId from JWT token
     */
    public String getUserIdFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("userId", String.class);
    }

    /**
     * Get role from JWT token
     */
    public String getRoleFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
    }

    /**
     * Get rememberMe flag from JWT token
     */
    public boolean getRememberMeFromToken(String token) {
        try {
            Boolean rememberMe = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("rememberMe", Boolean.class);
            return rememberMe != null && rememberMe;
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Check if token is refresh token
     */
    public boolean isRefreshToken(String token) {
        try {
            Boolean isRefreshToken = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("isRefreshToken", Boolean.class);
            return isRefreshToken != null && isRefreshToken;
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Get signing key from secret
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

