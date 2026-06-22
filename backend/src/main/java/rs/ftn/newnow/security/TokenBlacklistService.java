package rs.ftn.newnow.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private final JwtUtil jwtUtil;
    private final ConcurrentHashMap<String, Long> revokedTokens = new ConcurrentHashMap<>();

    public void revoke(String token) {
        try {
            Date expiration = jwtUtil.extractExpiration(token);
            revokedTokens.put(token, expiration.getTime());
        } catch (Exception e) {
            revokedTokens.put(token, System.currentTimeMillis() + 86_400_000L);
        }
    }

    public boolean isRevoked(String token) {
        return revokedTokens.containsKey(token);
    }

    @Scheduled(fixedDelay = 600_000L)
    public void purgeExpired() {
        long now = System.currentTimeMillis();
        int before = revokedTokens.size();
        revokedTokens.entrySet().removeIf(entry -> entry.getValue() < now);
        int removed = before - revokedTokens.size();
        if (removed > 0) {
            log.debug("Purged {} expired tokens from blacklist", removed);
        }
    }
}
