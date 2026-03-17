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

import com.business.entities.User;
import com.business.services.UserServices;

import jakarta.validation.Valid;

@Controller
public class UserController {

	@Autowired
	private UserServices services;

	@PostMapping("/addingUser")
	public String addUser(@Valid @ModelAttribute("user") User user, BindingResult result) {
		if (result.hasErrors()) {
			return "Add_User";
		}
		this.services.addUser(user);
		return "redirect:/admin/services";
	}

	@PostMapping("/updatingUser/{id}")
	public String updateUser(@ModelAttribute User user, @PathVariable("id") UUID id) {
		this.services.updateUser(user, id);
		return "redirect:/admin/services";
	}

	@GetMapping("/deleteUser/{id}")
	public String deleteUser(@PathVariable("id") UUID id) {
		this.services.deleteUser(id);
		return "redirect:/admin/services";
	}
}
