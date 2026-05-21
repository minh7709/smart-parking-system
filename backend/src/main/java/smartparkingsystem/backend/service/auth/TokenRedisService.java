package smartparkingsystem.backend.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenRedisService {

    private static final String REFRESH_TOKEN_PREFIX = "RT:";
    private static final String ACCESS_TOKEN_BLACKLIST_PREFIX = "BLACKLIST:";
    private static final String DELETED_USERS_PREFIX = "DELETED_USERS:";
    private static final long DELETED_USER_TTL_DAYS = 30; // Keep deleted user ID for 30 days

    private final RedisTemplate<String, Object> redisTemplate;

    public void storeRefreshToken(String refreshToken, long ttlMillis) {
        if (refreshToken == null || refreshToken.isBlank() || ttlMillis <= 0) {
            return;
        }
        redisTemplate.opsForValue().set(buildRefreshTokenKey(refreshToken), "1", ttlMillis, TimeUnit.MILLISECONDS);
    }

    public boolean isRefreshTokenActive(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(buildRefreshTokenKey(refreshToken)));
    }

    public void revokeRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        redisTemplate.delete(buildRefreshTokenKey(refreshToken));
    }

    public void blacklistAccessToken(String accessToken, long ttlMillis) {
        if (accessToken == null || accessToken.isBlank() || ttlMillis <= 0) {
            return;
        }
        redisTemplate.opsForValue().set(buildAccessBlacklistKey(accessToken), "1", ttlMillis, TimeUnit.MILLISECONDS);
    }

    public boolean isAccessTokenBlacklisted(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(buildAccessBlacklistKey(accessToken)));
    }

    public void markUserAsDeleted(UUID userId) {
        if (userId == null) {
            return;
        }
        long ttlMillis = TimeUnit.DAYS.toMillis(DELETED_USER_TTL_DAYS);
        redisTemplate.opsForValue().set(buildDeletedUserKey(userId), "1", ttlMillis, TimeUnit.MILLISECONDS);
    }

    public boolean isUserDeleted(UUID userId) {
        if (userId == null) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(buildDeletedUserKey(userId)));
    }

    public void removeDeletedUser(UUID userId) {
        if (userId == null) {
            return;
        }
        redisTemplate.delete(buildDeletedUserKey(userId));
    }

    private String buildRefreshTokenKey(String refreshToken) {
        return REFRESH_TOKEN_PREFIX + refreshToken;
    }

    private String buildAccessBlacklistKey(String accessToken) {
        return ACCESS_TOKEN_BLACKLIST_PREFIX + accessToken;
    }

    private String buildDeletedUserKey(UUID userId) {
        return DELETED_USERS_PREFIX + userId.toString();
    }
}

