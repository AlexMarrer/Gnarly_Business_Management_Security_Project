package com.business.entities;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@NotBlank(message = "Produktname ist erforderlich")
	@Size(min = 2, max = 100, message = "Produktname: 2–100 Zeichen")
	@Column(length = 100, nullable = false)
	private String pname;

	@NotNull(message = "Preis ist erforderlich")
	@DecimalMin(value = "0.01", message = "Preis muss mindestens 0.01 sein")
	@Digits(integer = 9, fraction = 2)
	private BigDecimal pprice;

	@NotBlank(message = "Beschreibung ist erforderlich")
	@Size(max = 500, message = "Beschreibung: max. 500 Zeichen")
	@Column(length = 500)
	private String pdescription;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getPname() {
		return pname;
	}

	public void setPname(String pname) {
		this.pname = pname;
	}

	public BigDecimal getPprice() {
		return pprice;
	}

	public void setPprice(BigDecimal pprice) {
		this.pprice = pprice;
	}

	public String getPdescription() {
		return pdescription;
	}

	public void setPdescription(String pdescription) {
		this.pdescription = pdescription;
	}

	@Override
	public String toString() {
		return "Product [id=" + id + ", pname=" + pname + ", pprice=" + pprice + "]";
	}
}
