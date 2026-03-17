package com.business.services;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.business.entities.Orders;
import com.business.entities.User;
import com.business.repositories.OrderRepository;

@Component
public class OrderServices {

	@Autowired
	private OrderRepository orderRepository;

	public List<Orders> getOrders() {
		return this.orderRepository.findAll();
	}

	public void saveOrder(Orders order) {
		this.orderRepository.save(order);
	}

	public void updateOrder(UUID id, Orders order) {
		order.setId(id);
		this.orderRepository.save(order);
	}

	public void deleteOrder(UUID id) {
		this.orderRepository.deleteById(id);
	}

	public List<Orders> getOrdersForUser(User user) {
		return this.orderRepository.findOrdersByUser(user);
	}
}
