package com.hotel.eventreservation.config;

import com.hotel.eventreservation.model.User;
import com.hotel.eventreservation.service.CustomUserDetailsService;
import com.hotel.eventreservation.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

@ControllerAdvice
public class GlobalModelAttributes {

    @Autowired
    private NotificationService notificationService;

    @ModelAttribute
    public void addCommonAttributes(Authentication authentication, Model model) {
        try {
            if (authentication != null && authentication.isAuthenticated()
                    && authentication.getPrincipal() instanceof CustomUserDetailsService.CustomUserPrincipal) {
                CustomUserDetailsService.CustomUserPrincipal principal =
                        (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
                User user = principal.getUser();
                model.addAttribute("user", user);
                try {
                    Long unreadCount = notificationService.getUnreadNotificationCount(user.getUserId());
                    model.addAttribute("unreadCount", unreadCount);
                    
                    // Add recent notifications for dropdown (limit to 5 most recent)
                    java.util.List<com.hotel.eventreservation.model.Notification> recentNotifications = 
                            notificationService.getNotificationsForUser(user.getUserId()).stream()
                                    .limit(5)
                                    .collect(java.util.stream.Collectors.toList());
                    model.addAttribute("recentNotifications", recentNotifications);
                } catch (Exception e) {
                    // Log the error for debugging but don't break the view
                    System.err.println("Error loading notifications for user " + user.getUserId() + ": " + e.getMessage());
                    model.addAttribute("unreadCount", 0L);
                    model.addAttribute("recentNotifications", java.util.Collections.emptyList());
                }
            }
        } catch (Exception ignored) {
            // Do not propagate errors to views
        }
    }
}


