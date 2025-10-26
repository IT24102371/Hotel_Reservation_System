package com.hotel.eventreservation.controller;

import com.hotel.eventreservation.service.CustomUserDetailsService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
            
            String role = userPrincipal.getAuthorities().iterator().next().getAuthority();
            model.addAttribute("user", userPrincipal.getUser());
            model.addAttribute("userRole", role);
            
            // Redirect based on role
            switch (role) {
                case "ROLE_GUEST":
                    return "redirect:/guest/dashboard";
                case "ROLE_GENERAL_MANAGER":
                    return "redirect:/manager/dashboard";
                case "ROLE_EVENT_COORDINATOR":
                    return "redirect:/coordinator/dashboard";
                case "ROLE_CATERING_TEAM_LEADER":
                    return "redirect:/catering/dashboard";
                case "ROLE_MARKETING_EXECUTIVE":
                    return "redirect:/marketing/dashboard";
                case "ROLE_RECEPTIONIST":
                    return "redirect:/reception/dashboard";
                default:
                    return "redirect:/login";
            }
        }
        return "redirect:/login";
    }
    
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    @GetMapping("/register")
    public String register() {
        return "register";
    }
}
