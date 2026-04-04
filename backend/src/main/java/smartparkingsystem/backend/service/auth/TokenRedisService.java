package smartparkingsystem.backend.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenRedisService {

    private static final String REFRESH_TOKEN_PREFIX = "RT:";
    private static final String ACCESS_TOKEN_BLACKLIST_PREFIX = "BLACKLIST:";

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

    private String buildRefreshTokenKey(String refreshToken) {
        return REFRESH_TOKEN_PREFIX + refreshToken;
    }

    private String buildAccessBlacklistKey(String accessToken) {
        return ACCESS_TOKEN_BLACKLIST_PREFIX + accessToken;
    }
}

