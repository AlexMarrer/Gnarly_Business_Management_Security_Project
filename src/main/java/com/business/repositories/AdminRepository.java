package com.business.repositories;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.business.entities.Admin;

public interface AdminRepository extends CrudRepository<Admin, UUID> {
	public Admin findByAdminEmail(String email);
}
