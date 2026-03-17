package com.business.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.business.entities.Admin;
import com.business.entities.User;
import com.business.repositories.AdminRepository;
import com.business.repositories.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private AdminRepository adminRepo;

	@Autowired
	private LoginAttemptService loginAttemptService;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		boolean blocked = loginAttemptService.isBlocked(email);

		// Check User table first
		User user = userRepo.findUserByUemail(email);
		if (user != null) {
			return org.springframework.security.core.userdetails.User.builder()
					.username(user.getUemail())
					.password(user.getUpassword())
					.roles("USER")
					.accountLocked(blocked)
					.build();
		}

		// Then check Admin table
		Admin admin = adminRepo.findByAdminEmail(email);
		if (admin != null) {
			return org.springframework.security.core.userdetails.User.builder()
					.username(admin.getAdminEmail())
					.password(admin.getAdminPassword())
					.roles("ADMIN")
					.accountLocked(blocked)
					.build();
		}

		throw new UsernameNotFoundException("User nicht gefunden: " + email);
	}
}
