package com.business.repositories;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.business.entities.Product;

public interface ProductRepository extends CrudRepository<Product, UUID> {
	public Product findByPname(String name);
}
