package com.tms.logistica.authservice.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class BruteForceProtectionService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String USER_PREFIX = "login:fail:user:";
    private static final String IP_PREFIX   = "login:fail:ip:";
    private static final int    MAX         = 5;
    private static final long   BLOCK_MIN   = 15;

    public void recordFailedAttempt(String username, String ip) {
        increment(USER_PREFIX + username);
        increment(IP_PREFIX + ip);
    }

    public void resetAttempts(String username, String ip) {
        redisTemplate.delete(USER_PREFIX + username);
        redisTemplate.delete(IP_PREFIX + ip);
    }

    public boolean isBlocked(String username, String ip) {
        return getCount(USER_PREFIX + username) >= MAX
            || getCount(IP_PREFIX + ip) >= MAX;
    }

    private void increment(String key) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, BLOCK_MIN, TimeUnit.MINUTES);
        }
    }

    private long getCount(String key) {
        String val = redisTemplate.opsForValue().get(key);
        return val == null ? 0L : Long.parseLong(val);
    }
}
