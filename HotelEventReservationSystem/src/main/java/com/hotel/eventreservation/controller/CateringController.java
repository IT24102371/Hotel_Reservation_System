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

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/catering")
public class CateringController {
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        
        // Get bookings with catering requirements
        List<Booking> cateringBookings = bookingService.getBookingsWithCatering();
        List<Booking> upcomingBookings = cateringBookings.stream()
                .filter(b -> b.getEventDate().isAfter(LocalDate.now()) || b.getEventDate().isEqual(LocalDate.now()))
                .limit(5)
                .toList();
        
        // Get notification data
        Long unreadCount = notificationService.getUnreadNotificationCount(user.getUserId());
        
        model.addAttribute("user", user);
        model.addAttribute("cateringBookings", cateringBookings);
        model.addAttribute("upcomingBookings", upcomingBookings);
        model.addAttribute("totalCatering", cateringBookings.size());
        model.addAttribute("unreadCount", unreadCount);
        
        return "catering/dashboard";
    }
    
    @GetMapping("/tasks")
    public String taskList(Authentication authentication, Model model) {
        List<Booking> cateringBookings = bookingService.getBookingsWithCatering();
        
        model.addAttribute("bookings", cateringBookings);
        return "catering/tasks";
    }
    
    @GetMapping("/confirmation/{bookingId}")
    public String cateringConfirmation(@PathVariable Long bookingId, Authentication authentication, Model model) {
        getCurrentUser(authentication);
        Booking booking = bookingService.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        // Check if booking has catering preferences
        if (booking.getCateringPreferences() == null) {
            throw new RuntimeException("No catering preferences found for this booking");
        }
        
        model.addAttribute("booking", booking);
        return "catering/confirmation";
    }
    
    @PostMapping("/confirmation/{bookingId}/confirm")
    public String confirmCatering(@PathVariable Long bookingId,
                                 @RequestParam String cateringNotes,
                                 @RequestParam(required = false) String cateringStatus,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            Booking booking = bookingService.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));
            
            // Update booking with catering notes and status
            booking.setCateringNotes(cateringNotes);
            if (cateringStatus != null) {
                booking.setCateringStatus(cateringStatus);
            }
            
            bookingService.updateBooking(booking);
            
            // Send notification to manager about catering confirmation
            User user = getCurrentUser(authentication);
            notificationService.sendNotification(
                user,
                "Catering confirmed for booking: " + booking.getReferenceCode(),
                com.hotel.eventreservation.model.Notification.AlertType.CATERING_CONFIRMED
            );
            
            redirectAttributes.addFlashAttribute("success", "Catering confirmed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/catering/tasks";
    }
    
    @GetMapping("/inventory/{bookingId}")
    public String inventoryTracker(@PathVariable Long bookingId, Model model) {
        Booking booking = bookingService.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        // Generate supply checklist based on guest count and menu
        List<String> supplyChecklist = generateSupplyChecklist(booking);
        
        model.addAttribute("booking", booking);
        model.addAttribute("supplyChecklist", supplyChecklist);
        return "catering/inventory";
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
        return "catering/notifications";
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
        return "redirect:/catering/notifications";
    }
    
    @PostMapping("/notifications/{notificationId}/mark-read")
    public String markNotificationAsRead(@PathVariable Long notificationId, RedirectAttributes redirectAttributes) {
        try {
            notificationService.markAsRead(notificationId);
            redirectAttributes.addFlashAttribute("success", "Notification marked as read!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/catering/notifications";
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
        return "redirect:/catering/notifications";
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
        return "redirect:/catering/notifications";
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
        return "redirect:/catering/notifications";
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
        return "redirect:/catering/notifications";
    }
    
    private List<String> generateSupplyChecklist(Booking booking) {
        // Generate supply checklist based on guest count and catering preferences
        List<String> checklist = new java.util.ArrayList<>();
        
        if (booking.getCateringPreferences() != null) {
            int guestCount = booking.getGuestCount();
            
            // Basic supplies
            checklist.add("Plates: " + (guestCount + 10) + " pieces");
            checklist.add("Cups: " + (guestCount + 10) + " pieces");
            checklist.add("Cutlery sets: " + (guestCount + 10) + " sets");
            checklist.add("Napkins: " + (guestCount * 2) + " pieces");
            
            // Based on meal type
            if (booking.getCateringPreferences().getMealType() != null) {
                switch (booking.getCateringPreferences().getMealType()) {
                    case BREAKFAST:
                        checklist.add("Coffee: " + (guestCount / 4) + " pots");
                        checklist.add("Tea: " + (guestCount / 6) + " pots");
                        break;
                    case LUNCH:
                        checklist.add("Water: " + (guestCount * 2) + " bottles");
                        checklist.add("Soft drinks: " + (guestCount / 2) + " bottles");
                        break;
                    case DINNER:
                        checklist.add("Wine: " + (guestCount / 2) + " bottles");
                        checklist.add("Water: " + (guestCount * 2) + " bottles");
                        break;
                    case SNACKS:
                        checklist.add("Light refreshments: " + (guestCount / 2) + " portions");
                        break;
                    case COCKTAILS:
                        checklist.add("Cocktail ingredients: " + (guestCount / 3) + " sets");
                        checklist.add("Mixers: " + (guestCount / 4) + " bottles");
                        break;
                }
            }
            
            // Special dietary requirements
            if (booking.getCateringPreferences().getDietaryRestrictions() != null) {
                checklist.add("Special dietary items: " + booking.getCateringPreferences().getDietaryRestrictions());
            }
        }
        
        return checklist;
    }
    
    private User getCurrentUser(Authentication authentication) {
        return ((com.hotel.eventreservation.service.CustomUserDetailsService.CustomUserPrincipal) 
                authentication.getPrincipal()).getUser();
    }
}
