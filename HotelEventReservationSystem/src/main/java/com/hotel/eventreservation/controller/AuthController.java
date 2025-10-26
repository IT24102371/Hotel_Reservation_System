package com.hotel.eventreservation.controller;

import com.hotel.eventreservation.model.User;
import com.hotel.eventreservation.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/register")
    public String register(@RequestParam String username,
                          @RequestParam String email,
                          @RequestParam String password,
                          @RequestParam String firstName,
                          @RequestParam String lastName,
                          @RequestParam(required = false) String phone,
                          RedirectAttributes redirectAttributes) {
        try {
            User user = userService.createUser(username, email, password, firstName, lastName, phone);
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }
    
    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            User user = ((com.hotel.eventreservation.service.CustomUserDetailsService.CustomUserPrincipal) 
                        authentication.getPrincipal()).getUser();
            model.addAttribute("user", user);
            return "profile";
        }
        return "redirect:/login";
    }
    
    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String firstName,
                               @RequestParam String lastName,
                               @RequestParam(required = false) String phone,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            User user = ((com.hotel.eventreservation.service.CustomUserDetailsService.CustomUserPrincipal) 
                        authentication.getPrincipal()).getUser();
            
            userService.updateUser(user.getUserId(), firstName, lastName, phone);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile";
    }
    
    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                @RequestParam String newPassword,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = ((com.hotel.eventreservation.service.CustomUserDetailsService.CustomUserPrincipal) 
                        authentication.getPrincipal()).getUser();
            
            // In a real application, you would verify the current password
            userService.changePassword(user.getUserId(), newPassword);
            redirectAttributes.addFlashAttribute("success", "Password changed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile";
    }
    
    @GetMapping("/logout")
    public String logout() {
        SecurityContextHolder.clearContext();
        return "redirect:/login?logout=true";
    }
}
