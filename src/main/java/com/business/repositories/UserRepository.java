package com.business.repositories;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.business.entities.User;

public interface UserRepository extends CrudRepository<User, UUID> {
	public User findUserByUemail(String email);
}
