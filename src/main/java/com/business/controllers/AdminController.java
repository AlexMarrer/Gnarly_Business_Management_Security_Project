package com.business.controllers;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.business.basiclogics.Logic;
import com.business.entities.Admin;
import com.business.entities.Orders;
import com.business.entities.Product;
import com.business.entities.User;
import com.business.services.AdminServices;
import com.business.services.OrderServices;
import com.business.services.ProductServices;
import com.business.services.UserServices;

import jakarta.validation.Valid;

@Controller
public class AdminController {

	@Autowired
	private UserServices services;
	@Autowired
	private AdminServices adminServices;
	@Autowired
	private ProductServices productServices;
	@Autowired
	private OrderServices orderServices;

	// --- User-facing endpoints (ROLE_USER) ---

	@PreAuthorize("hasRole('USER')")
	@PostMapping("/product/search")
	public String searchHandler(@RequestParam("productName") String name,
			@AuthenticationPrincipal UserDetails principal, Model model) {
		User currentUser = services.getUserByEmail(principal.getUsername());
		model.addAttribute("name", currentUser.getUname());

		Product product = productServices.getProductByName(name);
		if (product == null) {
			model.addAttribute("message", "SORRY...!  Product Unavailable");
		}
		model.addAttribute("product", product);

		List<Orders> orders = orderServices.getOrdersForUser(currentUser);
		model.addAttribute("orders", orders);
		return "BuyProduct";
	}

	@PreAuthorize("hasRole('USER')")
	@PostMapping("/product/order")
	public String orderHandler(@ModelAttribute() Orders order,
			@AuthenticationPrincipal UserDetails principal, Model model) {
		User currentUser = services.getUserByEmail(principal.getUsername());
		BigDecimal totalAmount = Logic.countTotal(order.getoPrice(), order.getoQuantity());
		order.setTotalAmount(totalAmount);
		order.setUser(currentUser);
		this.orderServices.saveOrder(order);
		model.addAttribute("amount", totalAmount);
		return "Order_success";
	}

	@PreAuthorize("hasRole('USER')")
	@GetMapping("/product/back")
	public String back(@AuthenticationPrincipal UserDetails principal, Model model) {
		User currentUser = services.getUserByEmail(principal.getUsername());
		model.addAttribute("name", currentUser.getUname());
		List<Orders> orders = this.orderServices.getOrdersForUser(currentUser);
		model.addAttribute("orders", orders);
		return "BuyProduct";
	}

	// --- Admin-facing endpoints (ROLE_ADMIN) ---

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/admin/services")
	public String returnBack(Model model) {
		List<User> users = this.services.getAllUser();
		List<Admin> admins = this.adminServices.getAll();
		List<Product> products = this.productServices.getAllProducts();
		List<Orders> orders = this.orderServices.getOrders();
		model.addAttribute("users", users);
		model.addAttribute("admins", admins);
		model.addAttribute("products", products);
		model.addAttribute("orders", orders);
		return "Admin_Page";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/addAdmin")
	public String addAdminPage(Model model) {
		model.addAttribute("admin", new Admin());
		return "Add_Admin";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("addingAdmin")
	public String addAdmin(@Valid @ModelAttribute("admin") Admin admin, BindingResult result) {
		if (result.hasErrors()) {
			return "Add_Admin";
		}
		this.adminServices.addAdmin(admin);
		return "redirect:/admin/services";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/updateAdmin/{adminId}")
	public String update(@PathVariable("adminId") UUID id, Model model) {
		Admin admin = this.adminServices.getAdmin(id);
		model.addAttribute("admin", admin);
		return "Update_Admin";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/updatingAdmin/{id}")
	public String updateAdmin(@ModelAttribute Admin admin, @PathVariable("id") UUID id) {
		this.adminServices.update(admin, id);
		return "redirect:/admin/services";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/deleteAdmin/{id}")
	public String deleteAdmin(@PathVariable("id") UUID id) {
		this.adminServices.delete(id);
		return "redirect:/admin/services";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/addProduct")
	public String addProduct(Model model) {
		model.addAttribute("product", new Product());
		return "Add_Product";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/updateProduct/{productId}")
	public String updateProduct(@PathVariable("productId") UUID id, Model model) {
		Product product = this.productServices.getProduct(id);
		model.addAttribute("product", product);
		return "Update_Product";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/addUser")
	public String addUser(Model model) {
		model.addAttribute("user", new User());
		return "Add_User";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/updateUser/{userId}")
	public String updateUserPage(@PathVariable("userId") UUID id, Model model) {
		User user = this.services.getUser(id);
		model.addAttribute("user", user);
		return "Update_User";
	}
}
