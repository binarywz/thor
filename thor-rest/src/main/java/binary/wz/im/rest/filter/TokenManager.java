package binary.wz.im.rest.filter;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

/**
 * @author binarywz
 * @date 2022/6/3 15:40
 * @description:
 */
@Component
public class TokenManager {

    private static final String SESSION_KEY = "IM:TOKEN:";
    private ReactiveRedisTemplate<String, String> redisTemplate;

    public TokenManager(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 校验token
     * @param token
     * @return
     */
    public Mono<String> validateToken(String token) {
        return redisTemplate.opsForValue().get(SESSION_KEY + token).map(userId -> {
            redisTemplate.expire(SESSION_KEY + token, Duration.ofMinutes(30));
            return userId;
        }).switchIfEmpty(Mono.empty());
    }

    /**
     * 生成token
     * @param userId
     * @return
     */
    public Mono<String> createNewToken(String userId) {
        String token = UUID.randomUUID().toString();
        return redisTemplate.opsForValue().set(SESSION_KEY + token, userId)
                .flatMap(b -> b ? redisTemplate.expire(SESSION_KEY + token, Duration.ofMinutes(30)) : Mono.just(false))
                .flatMap(b -> b ? Mono.just(token) : Mono.empty());
    }

    /**
     * 删除token
     * @param token
     * @return
     */
    public Mono<Boolean> expire(String token) {
        return redisTemplate.delete(SESSION_KEY + token).map(l -> l > 0);
    }
}
