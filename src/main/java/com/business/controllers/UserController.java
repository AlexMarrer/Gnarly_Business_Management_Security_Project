package com.business.controllers;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.security.access.prepost.PreAuthorize;

import com.business.entities.User;
import com.business.services.UserServices;
import com.business.validation.OnCreate;

import jakarta.validation.Valid;
import jakarta.validation.groups.Default;

import org.springframework.validation.annotation.Validated;

@Controller
public class UserController {

	@Autowired
	private UserServices services;

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/addingUser")
	public String addUser(@Validated({Default.class, OnCreate.class}) @ModelAttribute("user") User user, BindingResult result) {
		if (result.hasErrors()) {
			return "Add_User";
		}
		this.services.addUser(user);
		return "redirect:/admin/services";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/updatingUser/{id}")
	public String updateUser(@Valid @ModelAttribute("user") User user, BindingResult result,
			@PathVariable("id") UUID id) {
		if (result.hasErrors()) {
			return "Update_User";
		}
		this.services.updateUser(user, id);
		return "redirect:/admin/services";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/deleteUser/{id}")
	public String deleteUser(@PathVariable("id") UUID id) {
		this.services.deleteUser(id);
		return "redirect:/admin/services";
	}
}
