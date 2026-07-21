package com.henri_fraise.hff_data_studio.security.token;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

  private final Map<String, Long> blacklistedTokens = new ConcurrentHashMap<>();

  private static final long TOKEN_EXPIRATION_MS = 604800000L;

  public void blacklistToken(String token) {
    if (token == null || token.isEmpty()) {
      return;
    }
    blacklistedTokens.put(token, System.currentTimeMillis());
    log.debug("Token blacklisted: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
  }

  public void blacklistTokenWithExpiration(String token, long expirationMs) {
    if (token == null || token.isEmpty()) {
      return;
    }
    blacklistedTokens.put(token, System.currentTimeMillis() + expirationMs);
    log.debug("Token blacklisted with expiration: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
  }

  public boolean isBlacklisted(String token) {
    if (token == null || token.isEmpty()) {
      return true;
    }
    Long expiration = blacklistedTokens.get(token);
    if (expiration == null) {
      return false;
    }
    if (System.currentTimeMillis() > expiration) {
      blacklistedTokens.remove(token);
      return false;
    }
    return true;
  }

  public void removeFromBlacklist(String token) {
    blacklistedTokens.remove(token);
    log.debug("Token removed from blacklist: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
  }

  public int getBlacklistSize() {
    return blacklistedTokens.size();
  }

  public void clearBlacklist() {
    blacklistedTokens.clear();
    log.info("Blacklist cleared");
  }

  @Scheduled(cron = "0 0 0 * * ?")
  public void cleanExpiredTokens() {
    long now = System.currentTimeMillis();
    int before = blacklistedTokens.size();

    blacklistedTokens.entrySet().removeIf(entry ->
            entry.getValue() < now
    );

    int after = blacklistedTokens.size();
    log.info("Cleaned {} expired tokens from blacklist", before - after);
  }
}