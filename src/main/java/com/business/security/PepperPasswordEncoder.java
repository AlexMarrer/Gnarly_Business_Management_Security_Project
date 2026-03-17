package com.business.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PepperPasswordEncoder implements PasswordEncoder {

	@Value("${app.security.pepper}")
	private String pepper;

	private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder(12);

	@Override
	public String encode(CharSequence rawPassword) {
		return bcrypt.encode(rawPassword + pepper);
	}

	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		return bcrypt.matches(rawPassword + pepper, encodedPassword);
	}
}
