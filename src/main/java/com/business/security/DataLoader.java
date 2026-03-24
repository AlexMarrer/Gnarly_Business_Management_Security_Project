package com.business.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.business.entities.Admin;
import com.business.repositories.AdminRepository;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PepperPasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String defaultAdminEmail;

    @Value("${app.admin.password}")
    private String defaultAdminPassword;

    @Value("${app.admin.number}")
    private String defaultAdminNumber;

    @Override
    public void run(String... args) {
        if (adminRepository.count() == 0) {
            Admin admin = new Admin();
            admin.setAdminName("Admin");
            admin.setAdminEmail(defaultAdminEmail);
            admin.setAdminPassword(passwordEncoder.encode(defaultAdminPassword));
            admin.setAdminNumber(defaultAdminNumber);
            adminRepository.save(admin);
            System.out.println("=== Initialer Admin angelegt: " + defaultAdminEmail + " / " + defaultAdminPassword + " ===");
        }
    }
}
