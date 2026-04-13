package com.business.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
public class Orders {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@NotBlank(message = "Produktname ist erforderlich")
	@Size(min = 2, max = 100)
	@Pattern(regexp = "^[^<>\"'&]*$", message = "Sonderzeichen < > \" ' & sind nicht erlaubt")
	private String oName;

	@NotNull(message = "Preis ist erforderlich")
	@DecimalMin(value = "0.01", message = "Preis muss mindestens 0.01 sein")
	@Digits(integer = 9, fraction = 2)
	private BigDecimal oPrice;

	@NotNull(message = "Menge ist erforderlich")
	@Min(value = 1, message = "Mindestmenge: 1")
	@Max(value = 999, message = "Maximalmenge: 999")
	private Integer oQuantity;

	private LocalDate orderDate;

	@Digits(integer = 12, fraction = 2)
	private BigDecimal totalAmount;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	@PrePersist
	protected void onCreate() {
		this.orderDate = LocalDate.now();
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getoName() {
		return oName;
	}

	public void setoName(String oName) {
		this.oName = oName;
	}

	public BigDecimal getoPrice() {
		return oPrice;
	}

	public void setoPrice(BigDecimal oPrice) {
		this.oPrice = oPrice;
	}

	public Integer getoQuantity() {
		return oQuantity;
	}

	public void setoQuantity(Integer oQuantity) {
		this.oQuantity = oQuantity;
	}

	public LocalDate getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(LocalDate orderDate) {
		this.orderDate = orderDate;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public String toString() {
		return "Orders [id=" + id + ", oName=" + oName + ", oPrice=" + oPrice
				+ ", oQuantity=" + oQuantity + ", orderDate=" + orderDate
				+ ", totalAmount=" + totalAmount + "]";
	}
}
