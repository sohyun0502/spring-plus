package org.example.expert.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.user.dto.response.UserSearchResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String CACHE_USER_PREFIX = "user:";

    // 캐시 조회
    public UserSearchResponse getUserCache(String nickname) {
        String key = CACHE_USER_PREFIX + nickname;
        return (UserSearchResponse) redisTemplate.opsForValue().get(key);
    }

    // 캐시 저장
    public void saveUserCache(String nickname, UserSearchResponse response) {
        String key = CACHE_USER_PREFIX + nickname;
        redisTemplate.opsForValue().set(key, response, 10, TimeUnit.MINUTES);
    }
}
