package com.business.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.business.entities.User;
import com.business.services.UserServices;

import jakarta.validation.Valid;

@Controller
public class RegistrationController {

    @Autowired
    private UserServices userServices;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "Register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                               BindingResult result,
                               @RequestParam("passwordConfirm") String passwordConfirm,
                               Model model) {
        if (!user.getUpassword().equals(passwordConfirm)) {
            model.addAttribute("passwordMismatch", "Passwörter stimmen nicht überein");
            return "Register";
        }
        if (result.hasErrors()) {
            return "Register";
        }
        if (userServices.getUserByEmail(user.getUemail()) != null) {
            model.addAttribute("emailExists", "Diese E-Mail ist bereits registriert");
            return "Register";
        }
        userServices.addUser(user);
        return "redirect:/login?registered=true";
    }
}
