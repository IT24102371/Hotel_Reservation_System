package com.hotel.eventreservation.controller;

import com.hotel.eventreservation.model.Booking;
import com.hotel.eventreservation.model.User;
import com.hotel.eventreservation.service.BookingService;
import com.hotel.eventreservation.service.NotificationService;
import com.hotel.eventreservation.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;
import java.util.List;

@Controller
@RequestMapping("/reception")
public class ReceptionController {
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        model.addAttribute("user", user);
        return "reception/dashboard";
    }
    
    @GetMapping("/verify-booking")
    public String verifyBookingForm(@RequestParam(required = false) String ref, Model model) {
        if (ref != null && !ref.trim().isEmpty()) {
            try {
                Optional<Booking> bookingOpt = bookingService.findByReferenceCodeWithDetails(ref);
                if (bookingOpt.isPresent()) {
                    Booking booking = bookingOpt.get();
                    model.addAttribute("booking", booking);
                    model.addAttribute("found", true);
                } else {
                    model.addAttribute("error", "Booking not found with reference code: " + ref);
                    model.addAttribute("found", false);
                }
            } catch (Exception e) {
                model.addAttribute("error", e.getMessage());
                model.addAttribute("found", false);
            }
        } else {
            model.addAttribute("found", false);
        }
        return "reception/verify-booking";
    }
    
    @PostMapping("/verify-booking")
    public String verifyBooking(@RequestParam String referenceCode, Model model, RedirectAttributes redirectAttributes) {
        try {
            Optional<Booking> bookingOpt = bookingService.findByReferenceCodeWithDetails(referenceCode);
            if (bookingOpt.isPresent()) {
                Booking booking = bookingOpt.get();
                model.addAttribute("booking", booking);
                model.addAttribute("found", true);
                return "reception/verify-booking";
            } else {
                redirectAttributes.addFlashAttribute("error", "Booking not found with reference code: " + referenceCode);
                return "redirect:/reception/verify-booking";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reception/verify-booking";
        }
    }
    
    @PostMapping("/check-in/{bookingId}")
    public String checkInGuest(@PathVariable Long bookingId, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            Optional<Booking> bookingOpt = bookingService.findByReferenceCode(
                bookingService.getBookingsByStatus(Booking.BookingStatus.CONFIRMED).stream()
                    .filter(b -> b.getBookingId().equals(bookingId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Booking not found"))
                    .getReferenceCode()
            );
            
            if (bookingOpt.isPresent()) {
                Booking booking = bookingOpt.get();
                User user = getCurrentUser(authentication);
                
                // Send notification to event coordinator about guest arrival
                notificationService.sendNotification(
                    user, 
                    "Guest has arrived for booking: " + booking.getReferenceCode(),
                    com.hotel.eventreservation.model.Notification.AlertType.GUEST_ARRIVAL
                );
                
                redirectAttributes.addFlashAttribute("success", "Guest checked in successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/reception/verify-booking";
    }
    
    @GetMapping("/qr-scanner")
    public String qrScanner() {
        return "reception/qr-scanner";
    }
    
    @GetMapping("/todays-arrivals")
    public String todaysArrivals(Model model) {
        // Get today's confirmed bookings
        var todaysBookings = bookingService.getBookingsByStatus(Booking.BookingStatus.CONFIRMED)
                .stream()
                .filter(booking -> booking.getEventDate().equals(java.time.LocalDate.now()))
                .toList();
        
        model.addAttribute("bookings", todaysBookings);
        return "reception/todays-arrivals";
    }
    
    @GetMapping("/notifications")
    public String notifications(@RequestParam(required = false, defaultValue = "ALL") String status,
                                Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        List<com.hotel.eventreservation.model.Notification> notifications;
        switch (status.toUpperCase()) {
            case "UNREAD":
                notifications = notificationService.getUnreadNotificationsForUser(user.getUserId());
                break;
            case "READ":
                notifications = notificationService.getReadNotificationsForUser(user.getUserId());
                break;
            default:
                notifications = notificationService.getNotificationsForUser(user.getUserId());
        }
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", notificationService.getUnreadNotificationCount(user.getUserId()));
        model.addAttribute("status", status.toUpperCase());
        model.addAttribute("showCompose", true);
        model.addAttribute("roles", java.util.Arrays.asList("GENERAL_MANAGER","EVENT_COORDINATOR","CATERING_TEAM_LEADER","MARKETING_EXECUTIVE","RECEPTIONIST","GUEST"));
        model.addAttribute("alertTypes", java.util.Arrays.asList(
            com.hotel.eventreservation.model.Notification.AlertType.BOOKING_CONFIRMATION,
            com.hotel.eventreservation.model.Notification.AlertType.BOOKING_CANCELLATION,
            com.hotel.eventreservation.model.Notification.AlertType.EVENT_REMINDER,
            com.hotel.eventreservation.model.Notification.AlertType.COORDINATION_ALERT,
            com.hotel.eventreservation.model.Notification.AlertType.CATERING_CONFIRMED,
            com.hotel.eventreservation.model.Notification.AlertType.GUEST_ARRIVAL,
            com.hotel.eventreservation.model.Notification.AlertType.PAYMENT_REMINDER,
            com.hotel.eventreservation.model.Notification.AlertType.BOOKING_CHANGE
        ));
        return "reception/notifications";
    }

    @PostMapping("/notifications/send")
    public String sendNotification(@RequestParam String targetType,
                                   @RequestParam(required = false) Long userId,
                                   @RequestParam(required = false) String roleName,
                                   @RequestParam String message,
                                   @RequestParam com.hotel.eventreservation.model.Notification.AlertType alertType,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        try {
            int count = 0;
            User sender = getCurrentUser(authentication);
            if ("USER".equalsIgnoreCase(targetType)) {
                if (userId == null) throw new RuntimeException("User is required");
                User recipient = userService.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
                notificationService.sendNotification(recipient, sender, message, alertType);
                count = 1;
            } else if ("ROLE".equalsIgnoreCase(targetType)) {
                if (roleName == null || roleName.isBlank()) throw new RuntimeException("Role is required");
                for (User u : userService.getUsersByRole(roleName)) {
                    notificationService.sendNotification(u, sender, message, alertType);
                    count++;
                }
            } else if ("ALL_GUESTS".equalsIgnoreCase(targetType)) {
                for (User u : userService.getUsersByRole("GUEST")) {
                    notificationService.sendNotification(u, sender, message, alertType);
                    count++;
                }
            } else if ("ALL_USERS".equalsIgnoreCase(targetType)) {
                for (User u : userService.getAllActiveUsers()) {
                    notificationService.sendNotification(u, sender, message, alertType);
                    count++;
                }
            } else {
                throw new RuntimeException("Unknown target type");
            }
            redirectAttributes.addFlashAttribute("success", "Notification sent to " + count + " recipient(s).");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/reception/notifications";
    }
    
    @PostMapping("/notifications/{notificationId}/mark-read")
    public String markNotificationAsRead(@PathVariable Long notificationId, RedirectAttributes redirectAttributes) {
        try {
            notificationService.markAsRead(notificationId);
            redirectAttributes.addFlashAttribute("success", "Notification marked as read!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/reception/notifications";
    }
    
    @PostMapping("/notifications/{notificationId}/delete")
    public String deleteNotification(@PathVariable Long notificationId, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser(authentication);
            if (notificationService.deleteNotificationForUser(notificationId, user.getUserId())) {
                redirectAttributes.addFlashAttribute("success", "Notification deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Notification not found or access denied!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/reception/notifications";
    }
    
    @PostMapping("/notifications/mark-all-read")
    public String markAllNotificationsAsRead(Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser(authentication);
            notificationService.markAllAsReadForUser(user.getUserId());
            redirectAttributes.addFlashAttribute("success", "All notifications marked as read!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/reception/notifications";
    }
    
    @PostMapping("/notifications/delete-all")
    public String deleteAllNotifications(Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser(authentication);
            notificationService.deleteAllNotificationsForUser(user.getUserId());
            redirectAttributes.addFlashAttribute("success", "All notifications deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/reception/notifications";
    }
    
    @PostMapping("/notifications/bulk-delete")
    public String bulkDeleteNotifications(@RequestParam("notificationIds") List<Long> notificationIds, 
                                       Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser(authentication);
            int deletedCount = notificationService.deleteMultipleNotificationsForUser(notificationIds, user.getUserId());
            redirectAttributes.addFlashAttribute("success", deletedCount + " notifications deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/reception/notifications";
    }
    
    private User getCurrentUser(Authentication authentication) {
        return ((com.hotel.eventreservation.service.CustomUserDetailsService.CustomUserPrincipal) 
                authentication.getPrincipal()).getUser();
    }
}
