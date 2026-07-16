package com.henri_fraise.hff_data_studio.security.token;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class TokenBlacklistService {

	private final Map<String, Long> blacklistedTokens = new ConcurrentHashMap<>();

	private static final long TOKEN_BLACKLIST_DURATION = 24 * 60 * 60 * 1000;

	public void blacklist(String token) {
		if (token != null && !token.isEmpty()) {
			blacklistedTokens.put(token, System.currentTimeMillis() + TOKEN_BLACKLIST_DURATION);
			log.debug("Token blacklisted: {}", token.substring(0, Math.min(token.length(), 10)) + "...");
		}
	}

	public boolean isBlacklisted(String token) {
		if (token == null || token.isEmpty()) return false;

		Long expiry = blacklistedTokens.get(token);
		if (expiry == null) return false;

		if (expiry < System.currentTimeMillis()) {
			blacklistedTokens.remove(token);
			return false;
		}

		return true;
	}

	public void removeFromBlacklist(String token) {
		if (token != null && !token.isEmpty()) {
			blacklistedTokens.remove(token);
		}
	}

	@Scheduled(cron = "0 0 * * * *")
	public void cleanupBlacklist() {
		long now = System.currentTimeMillis();
		long before = blacklistedTokens.size();

		blacklistedTokens.entrySet().removeIf(entry -> entry.getValue() < now);

		long after = blacklistedTokens.size();
		if (before != after) {
			log.info("Cleaned up {} expired tokens from blacklist", before - after);
		}
	}

	public int getBlacklistSize() {
		return blacklistedTokens.size();
	}
}