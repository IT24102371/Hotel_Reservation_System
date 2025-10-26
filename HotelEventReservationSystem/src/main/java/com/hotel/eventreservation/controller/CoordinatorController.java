package com.hotel.eventreservation.controller;

import com.hotel.eventreservation.model.Booking;
import com.hotel.eventreservation.model.User;
import com.hotel.eventreservation.service.AvailabilityService;
import com.hotel.eventreservation.service.BookingService;
import com.hotel.eventreservation.service.UserService;
import com.hotel.eventreservation.service.NotificationService;
import com.hotel.eventreservation.service.VenueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/coordinator")
public class CoordinatorController {
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private VenueService venueService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AvailabilityService availabilityService;
    
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        
        // Get bookings assigned to this coordinator
        List<Booking> assignedBookings = bookingService.getBookingsByAssignedStaff(user.getUserId());
        List<Booking> upcomingBookings = assignedBookings.stream()
                .filter(b -> b.getEventDate().isAfter(LocalDate.now()) || b.getEventDate().isEqual(LocalDate.now()))
                .limit(5)
                .toList();
        
        // Get notification data
        Long unreadCount = notificationService.getUnreadNotificationCount(user.getUserId());
        
        model.addAttribute("user", user);
        model.addAttribute("assignedBookings", assignedBookings);
        model.addAttribute("upcomingBookings", upcomingBookings);
        model.addAttribute("totalAssigned", assignedBookings.size());
        model.addAttribute("unreadCount", unreadCount);
        
        return "coordinator/dashboard";
    }
    
    @GetMapping("/tasks")
    public String taskList(Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        List<Booking> assignedBookings = bookingService.getBookingsByAssignedStaff(user.getUserId());
        
        model.addAttribute("bookings", assignedBookings);
        return "coordinator/tasks";
    }
    
    @GetMapping("/setup/{bookingId}")
    public String roomSetup(@PathVariable Long bookingId, Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        Booking booking = bookingService.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        // Verify this booking is assigned to the current coordinator
        if (!booking.getAssignedCoordinatorId().equals(user.getUserId())) {
            throw new RuntimeException("Access denied: Booking not assigned to you");
        }
        
        model.addAttribute("booking", booking);
        return "coordinator/room-setup";
    }
    
    @PostMapping("/setup/{bookingId}/confirm")
    public String confirmSetup(@PathVariable Long bookingId,
                              @RequestParam String coordinatorNotes,
                              @RequestParam(required = false) String setupStatus,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser(authentication);
            Booking booking = bookingService.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));
            
            // Update booking with coordinator notes and setup status
            booking.setCoordinatorNotes(coordinatorNotes);
            if (setupStatus != null) {
                booking.setSetupStatus(setupStatus);
            }
            
            bookingService.updateBooking(booking);
            
            // Send notification to manager about setup completion
            notificationService.sendNotification(
                user,
                "Room setup completed for booking: " + booking.getReferenceCode(),
                com.hotel.eventreservation.model.Notification.AlertType.SETUP_COMPLETE
            );
            
            redirectAttributes.addFlashAttribute("success", "Setup confirmed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/coordinator/tasks";
    }
    
    @GetMapping("/calendar")
    public String availabilityCalendar(@RequestParam(required = false) Long venueId,
                                     @RequestParam(required = false) String month,
                                     Model model) {
        LocalDate targetMonth = month != null ? LocalDate.parse(month + "-01") : LocalDate.now();
        
        Map<String, Object> calendarData = availabilityService.getCalendarData(targetMonth, venueId);
        
        model.addAttribute("calendarData", calendarData);
        model.addAttribute("selectedVenueId", venueId);
        model.addAttribute("targetMonth", targetMonth);
        model.addAttribute("startOfMonth", calendarData.get("startOfMonth"));
        model.addAttribute("endOfMonth", calendarData.get("endOfMonth"));
        model.addAttribute("groupedByDate", calendarData.get("groupedByDate"));
        model.addAttribute("bookingMap", calendarData.get("bookingMap"));
        
        // Create calendar days list for the template
        List<LocalDate> calendarDays = new ArrayList<>();
        LocalDate startOfMonth = (LocalDate) calendarData.get("startOfMonth");
        LocalDate endOfMonth = (LocalDate) calendarData.get("endOfMonth");
        
        // Add days from start to end of month
        LocalDate currentDay = startOfMonth;
        while (!currentDay.isAfter(endOfMonth)) {
            calendarDays.add(currentDay);
            currentDay = currentDay.plusDays(1);
        }
        
        model.addAttribute("calendarDays", calendarDays);
        
        // Get all venues for filter dropdown
        List<com.hotel.eventreservation.model.Venue> venues = venueService.getAllActiveVenues();
        model.addAttribute("venues", venues);
        
        return "coordinator/calendar";
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
        return "coordinator/notifications";
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
        return "redirect:/coordinator/notifications";
    }
    
    @PostMapping("/notifications/{notificationId}/mark-read")
    public String markNotificationAsRead(@PathVariable Long notificationId, RedirectAttributes redirectAttributes) {
        try {
            notificationService.markAsRead(notificationId);
            redirectAttributes.addFlashAttribute("success", "Notification marked as read!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/coordinator/notifications";
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
        return "redirect:/coordinator/notifications";
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
        return "redirect:/coordinator/notifications";
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
        return "redirect:/coordinator/notifications";
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
        return "redirect:/coordinator/notifications";
    }
    
    private User getCurrentUser(Authentication authentication) {
        return ((com.hotel.eventreservation.service.CustomUserDetailsService.CustomUserPrincipal) 
                authentication.getPrincipal()).getUser();
    }
}
