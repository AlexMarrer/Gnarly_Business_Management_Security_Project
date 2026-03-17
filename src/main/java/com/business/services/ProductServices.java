package com.business.services;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.business.entities.Product;
import com.business.repositories.ProductRepository;

@Component
public class ProductServices {

	@Autowired
	private ProductRepository productRepository;

	public void addProduct(Product p) {
		this.productRepository.save(p);
	}

	public List<Product> getAllProducts() {
		return (List<Product>) this.productRepository.findAll();
	}

	public Product getProduct(UUID id) {
		return this.productRepository.findById(id).orElseThrow();
	}

	public void updateproduct(Product p, UUID id) {
		Product existing = this.productRepository.findById(id).orElseThrow();
		existing.setPname(p.getPname());
		existing.setPprice(p.getPprice());
		existing.setPdescription(p.getPdescription());
		this.productRepository.save(existing);
	}

	public void deleteProduct(UUID id) {
		this.productRepository.deleteById(id);
	}

	public Product getProductByName(String name) {
		return this.productRepository.findByPname(name);
	}
}
