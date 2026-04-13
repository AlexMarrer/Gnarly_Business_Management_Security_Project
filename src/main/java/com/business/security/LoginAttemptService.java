package com.business.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {

	private static final int MAX_ATTEMPTS = 5;
	private static final long LOCKOUT_MINUTES = 15;

	private record AttemptRecord(int count, Instant windowStart) {}

	private final ConcurrentHashMap<String, AttemptRecord> attemptsCache = new ConcurrentHashMap<>();

	public void loginFailed(String email) {
		attemptsCache.compute(email, (key, existing) -> {
			if (existing == null || isExpired(existing)) {
				return new AttemptRecord(1, Instant.now());
			}
			return new AttemptRecord(existing.count() + 1, existing.windowStart());
		});
	}

	public boolean isBlocked(String email) {
		AttemptRecord record = attemptsCache.get(email);
		if (record == null || isExpired(record)) {
			attemptsCache.remove(email);
			return false;
		}
		return record.count() >= MAX_ATTEMPTS;
	}

	public void loginSucceeded(String email) {
		attemptsCache.remove(email);
	}

	private boolean isExpired(AttemptRecord record) {
		return record.windowStart().plus(LOCKOUT_MINUTES, ChronoUnit.MINUTES).isBefore(Instant.now());
	}
}
