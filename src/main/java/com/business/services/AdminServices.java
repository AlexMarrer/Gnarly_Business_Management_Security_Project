package com.business.services;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.business.entities.Admin;
import com.business.repositories.AdminRepository;
import com.business.security.PepperPasswordEncoder;

@Component
public class AdminServices {

	@Autowired
	private AdminRepository adminRepository;

	@Autowired
	private PepperPasswordEncoder passwordEncoder;

	public List<Admin> getAll() {
		return (List<Admin>) this.adminRepository.findAll();
	}

	public Admin getAdmin(UUID id) {
		return this.adminRepository.findById(id).orElseThrow();
	}

	public void update(Admin admin, UUID id) {
		Admin existing = this.adminRepository.findById(id).orElseThrow();
		existing.setAdminName(admin.getAdminName());
		existing.setAdminEmail(admin.getAdminEmail());
		existing.setAdminNumber(admin.getAdminNumber());
		if (admin.getAdminPassword() != null && !admin.getAdminPassword().isEmpty()) {
			existing.setAdminPassword(passwordEncoder.encode(admin.getAdminPassword()));
		}
		this.adminRepository.save(existing);
	}

	public void delete(UUID id) {
		this.adminRepository.deleteById(id);
	}

	public void addAdmin(Admin admin) {
		admin.setAdminPassword(passwordEncoder.encode(admin.getAdminPassword()));
		this.adminRepository.save(admin);
	}
}
