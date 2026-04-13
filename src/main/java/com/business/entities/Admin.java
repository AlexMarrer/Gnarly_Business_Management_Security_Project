package com.business.entities;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import com.business.validation.OnCreate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
public class Admin {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@NotBlank(message = "Admin-Name ist erforderlich")
	@Size(min = 2, max = 50, message = "Admin-Name: 2–50 Zeichen")
	@Pattern(regexp = "^[^<>\"'&]*$", message = "Keine HTML-Sonderzeichen erlaubt")
	@Column(length = 50, nullable = false)
	private String adminName;

	@NotBlank(message = "E-Mail ist erforderlich")
	@Email(message = "Ungültiges E-Mail-Format")
	@Column(length = 255, nullable = false, unique = true)
	private String adminEmail;

	@NotBlank(message = "Passwort ist erforderlich", groups = OnCreate.class)
	@Pattern(
		regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
		message = "Passwort: min. 8 Zeichen, Gross+Klein+Zahl+Sonderzeichen",
		groups = OnCreate.class
	)
	@Column(length = 255, nullable = false)
	private String adminPassword;

	@NotBlank(message = "Telefonnummer ist erforderlich")
	@Pattern(regexp = "^\\d{10,15}$", message = "Telefonnummer: 10–15 Ziffern")
	@Column(length = 20, nullable = false)
	private String adminNumber;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getAdminName() {
		return adminName;
	}

	public void setAdminName(String adminName) {
		this.adminName = adminName;
	}

	public String getAdminEmail() {
		return adminEmail;
	}

	public void setAdminEmail(String adminEmail) {
		this.adminEmail = adminEmail;
	}

	public String getAdminPassword() {
		return adminPassword;
	}

	public void setAdminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
	}

	public String getAdminNumber() {
		return adminNumber;
	}

	public void setAdminNumber(String adminNumber) {
		this.adminNumber = adminNumber;
	}

	@Override
	public String toString() {
		return "Admin [id=" + id + ", adminName=" + adminName + ", adminEmail=" + adminEmail + "]";
	}
}
