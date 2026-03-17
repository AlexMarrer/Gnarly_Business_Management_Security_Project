package com.business.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.business.entities.Orders;
import com.business.entities.User;

public interface OrderRepository extends JpaRepository<Orders, UUID> {
	List<Orders> findOrdersByUser(User user);
}
