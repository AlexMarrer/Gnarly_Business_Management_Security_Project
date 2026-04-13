package com.business.entities;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import com.business.validation.OnCreate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@jakarta.persistence.Table(name = "app_user")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@NotBlank(message = "Benutzername ist erforderlich")
	@Size(min = 2, max = 50, message = "Benutzername: 2–50 Zeichen")
	@Pattern(regexp = "^[^<>\"'&]*$", message = "Keine HTML-Sonderzeichen erlaubt")
	@Column(length = 50, nullable = false)
	private String uname;

	@NotBlank(message = "E-Mail ist erforderlich")
	@Email(message = "Ungültiges E-Mail-Format")
	@Column(length = 255, nullable = false, unique = true)
	private String uemail;

	@NotBlank(message = "Passwort ist erforderlich", groups = OnCreate.class)
	@Pattern(
		regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
		message = "Passwort: min. 8 Zeichen, Gross+Klein+Zahl+Sonderzeichen",
		groups = OnCreate.class
	)
	@Column(length = 255, nullable = false)
	private String upassword;

	@NotBlank(message = "Telefonnummer ist erforderlich")
	@Pattern(regexp = "^\\d{10,15}$", message = "Telefonnummer: 10–15 Ziffern")
	@Column(length = 20, nullable = false)
	private String unumber;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	private List<Orders> orders;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getUname() {
		return uname;
	}

	public void setUname(String uname) {
		this.uname = uname;
	}

	public String getUemail() {
		return uemail;
	}

	public void setUemail(String uemail) {
		this.uemail = uemail;
	}

	public String getUpassword() {
		return upassword;
	}

	public void setUpassword(String upassword) {
		this.upassword = upassword;
	}

	public String getUnumber() {
		return unumber;
	}

	public void setUnumber(String unumber) {
		this.unumber = unumber;
	}

	public List<Orders> getOrders() {
		return orders;
	}

	public void setOrders(List<Orders> orders) {
		this.orders = orders;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", uname=" + uname + ", uemail=" + uemail + "]";
	}
}
