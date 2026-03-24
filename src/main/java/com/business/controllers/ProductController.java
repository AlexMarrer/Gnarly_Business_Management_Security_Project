package com.business.controllers;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.security.access.prepost.PreAuthorize;

import com.business.entities.Product;
import com.business.services.ProductServices;

import jakarta.validation.Valid;

@Controller
public class ProductController {

	@Autowired
	private ProductServices productServices;

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/addingProduct")
	public String addProduct(@Valid @ModelAttribute("product") Product product, BindingResult result) {
		if (result.hasErrors()) {
			return "Add_Product";
		}
		this.productServices.addProduct(product);
		return "redirect:/admin/services";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/updatingProduct/{productId}")
	public String updateProduct(@Valid @ModelAttribute("product") Product product,
			BindingResult result, @PathVariable("productId") UUID id) {
		if (result.hasErrors()) {
			return "Update_Product";
		}
		this.productServices.updateproduct(product, id);
		return "redirect:/admin/services";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/deleteProduct/{productId}")
	public String delete(@PathVariable("productId") UUID id) {
		this.productServices.deleteProduct(id);
		return "redirect:/admin/services";
	}
}
