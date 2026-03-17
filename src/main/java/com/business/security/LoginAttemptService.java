package com.business.security;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {

	private static final int MAX_ATTEMPTS = 5;
	private final Map<String, Integer> attemptsCache = new ConcurrentHashMap<>();

	public void loginFailed(String email) {
		attemptsCache.merge(email, 1, Integer::sum);
	}

	public boolean isBlocked(String email) {
		return attemptsCache.getOrDefault(email, 0) >= MAX_ATTEMPTS;
	}

	public void loginSucceeded(String email) {
		attemptsCache.remove(email);
	}
}
