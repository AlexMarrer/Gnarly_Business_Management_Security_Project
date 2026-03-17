package com.business.services;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.business.entities.User;
import com.business.repositories.UserRepository;
import com.business.security.PepperPasswordEncoder;

@Component
public class UserServices {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PepperPasswordEncoder passwordEncoder;

	public List<User> getAllUser() {
		return (List<User>) this.userRepository.findAll();
	}

	public User getUser(UUID id) {
		return this.userRepository.findById(id).orElseThrow();
	}

	public User getUserByEmail(String email) {
		return this.userRepository.findUserByUemail(email);
	}

	public void updateUser(User user, UUID id) {
		User existing = this.userRepository.findById(id).orElseThrow();
		existing.setUname(user.getUname());
		existing.setUemail(user.getUemail());
		existing.setUnumber(user.getUnumber());
		this.userRepository.save(existing);
	}

	public void deleteUser(UUID id) {
		this.userRepository.deleteById(id);
	}

	public void addUser(User user) {
		user.setUpassword(passwordEncoder.encode(user.getUpassword()));
		this.userRepository.save(user);
	}
}
