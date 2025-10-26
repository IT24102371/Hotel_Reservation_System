package com.hotel.eventreservation.controller;

import com.hotel.eventreservation.model.User;
import com.hotel.eventreservation.service.ReportService;
import com.hotel.eventreservation.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.hotel.eventreservation.service.NotificationService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/marketing")
public class MarketingController {
    
    @Autowired
    private ReportService reportService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        
        // Get basic statistics - use a wider date range to capture more data
        LocalDate startDate = LocalDate.now().minusDays(90); // Look back 3 months
        LocalDate endDate = LocalDate.now().plusDays(90);    // Look forward 3 months
        
        List<Map<String, Object>> bookingAnalytics = reportService.generateBookingAnalytics(startDate, endDate, null, null);
        List<Map<String, Object>> revenueData = reportService.generateRevenueReport(startDate, endDate);
        List<Map<String, Object>> eventTypeTrends = reportService.generateEventTypeTrendsReport(startDate, endDate);
        
        // Calculate additional metrics
        double totalRevenue = calculateTotalRevenue(revenueData);
        double cancellationRate = calculateCancellationRate(bookingAnalytics);
        double venueOccupancy = calculateVenueOccupancy(bookingAnalytics);
        
        // Debug logging
        System.out.println("Dashboard Data Debug:");
        System.out.println("Date Range: " + startDate + " to " + endDate);
        System.out.println("Total Bookings: " + bookingAnalytics.size());
        System.out.println("Revenue Data Size: " + revenueData.size());
        System.out.println("Event Type Trends Size: " + eventTypeTrends.size());
        System.out.println("Total Revenue: " + totalRevenue);
        System.out.println("Cancellation Rate: " + cancellationRate);
        System.out.println("Venue Occupancy: " + venueOccupancy);
        
        // Log sample data for debugging
        if (!bookingAnalytics.isEmpty()) {
            System.out.println("Sample Booking Data: " + bookingAnalytics.get(0));
        }
        if (!revenueData.isEmpty()) {
            System.out.println("Sample Revenue Data: " + revenueData.get(0));
        }
        
        model.addAttribute("user", user);
        model.addAttribute("totalBookings", bookingAnalytics.size());
        model.addAttribute("revenueData", revenueData);
        model.addAttribute("eventTypeTrends", eventTypeTrends);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("cancellationRate", cancellationRate);
        model.addAttribute("venueOccupancy", venueOccupancy);
        model.addAttribute("availableFormats", reportService.getAvailableExportFormats());
        
        return "marketing/dashboard";
    }
    
    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("availableFormats", reportService.getAvailableExportFormats());
        return "marketing/reports";
    }
    
    @PostMapping("/reports/booking-analytics")
    public String generateBookingAnalyticsReport(@RequestParam(required = false) String startDate,
                                               @RequestParam(required = false) String endDate,
                                               @RequestParam(required = false) String eventType,
                                               @RequestParam(required = false) String status,
                                               Model model) {
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now().plusDays(30);
        
        com.hotel.eventreservation.model.Booking.BookingStatus bookingStatus = null;
        if (status != null && !status.isEmpty()) {
            bookingStatus = com.hotel.eventreservation.model.Booking.BookingStatus.valueOf(status);
        }
        
        List<Map<String, Object>> reportData = reportService.generateBookingAnalytics(start, end, eventType, bookingStatus);
        model.addAttribute("reportData", reportData);
        model.addAttribute("reportType", "Booking Analytics");
        model.addAttribute("availableFormats", reportService.getAvailableExportFormats());
        
        return "marketing/report-results";
    }
    
    @PostMapping("/reports/venue-utilization")
    public String generateVenueUtilizationReport(@RequestParam(required = false) String startDate,
                                               @RequestParam(required = false) String endDate,
                                               Model model) {
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now().plusDays(30);
        
        List<Map<String, Object>> reportData = reportService.generateVenueUtilizationReport(start, end);
        model.addAttribute("reportData", reportData);
        model.addAttribute("reportType", "Venue Utilization");
        model.addAttribute("availableFormats", reportService.getAvailableExportFormats());
        
        return "marketing/report-results";
    }
    
    @PostMapping("/reports/revenue")
    public String generateRevenueReport(@RequestParam(required = false) String startDate,
                                      @RequestParam(required = false) String endDate,
                                      Model model) {
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now().plusDays(30);
        
        List<Map<String, Object>> reportData = reportService.generateRevenueReport(start, end);
        model.addAttribute("reportData", reportData);
        model.addAttribute("reportType", "Revenue Report");
        model.addAttribute("availableFormats", reportService.getAvailableExportFormats());
        
        return "marketing/report-results";
    }
    
    @PostMapping("/reports/event-trends")
    public String generateEventTrendsReport(@RequestParam(required = false) String startDate,
                                          @RequestParam(required = false) String endDate,
                                          Model model) {
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now().plusDays(30);
        
        List<Map<String, Object>> reportData = reportService.generateEventTypeTrendsReport(start, end);
        model.addAttribute("reportData", reportData);
        model.addAttribute("reportType", "Event Type Trends");
        model.addAttribute("availableFormats", reportService.getAvailableExportFormats());
        
        return "marketing/report-results";
    }
    
    @GetMapping("/export/{format}")
    public ResponseEntity<ByteArrayResource> exportReport(@PathVariable String format,
                                                        @RequestParam String reportType,
                                                        @RequestParam(required = false) String startDate,
                                                        @RequestParam(required = false) String endDate,
                                                        @RequestParam(required = false) String eventType,
                                                        @RequestParam(required = false) String status) {
        try {
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(30);
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now().plusDays(30);
            
            com.hotel.eventreservation.model.Booking.BookingStatus bookingStatus = null;
            if (status != null && !status.isEmpty()) {
                bookingStatus = com.hotel.eventreservation.model.Booking.BookingStatus.valueOf(status);
            }
            
            List<Map<String, Object>> reportData;
            String filename;
            
            switch (reportType.toLowerCase()) {
                case "booking analytics":
                    reportData = reportService.generateBookingAnalytics(start, end, eventType, bookingStatus);
                    filename = "booking_analytics";
                    break;
                case "venue utilization":
                    reportData = reportService.generateVenueUtilizationReport(start, end);
                    filename = "venue_utilization";
                    break;
                case "revenue report":
                    reportData = reportService.generateRevenueReport(start, end);
                    filename = "revenue_report";
                    break;
                case "event type trends":
                    reportData = reportService.generateEventTypeTrendsReport(start, end);
                    filename = "event_trends";
                    break;
                default:
                    throw new RuntimeException("Unknown report type: " + reportType);
            }
            
            byte[] exportData = reportService.exportReport(format, reportData, filename);
            String fileExtension = reportService.getAvailableExportFormats().get(format);
            
            ByteArrayResource resource = new ByteArrayResource(exportData);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename + fileExtension)
                    .contentType(MediaType.parseMediaType(getMimeType(format)))
                    .contentLength(exportData.length)
                    .body(resource);
                    
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    private String getMimeType(String format) {
        switch (format.toUpperCase()) {
            case "PDF":
                return "application/pdf";
            case "CSV":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "JSON":
                return "application/json";
            default:
                return "application/octet-stream";
        }
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
        return "marketing/notifications";
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
        return "redirect:/marketing/notifications";
    }
    
    @PostMapping("/notifications/{notificationId}/mark-read")
    public String markNotificationAsRead(@PathVariable Long notificationId, RedirectAttributes redirectAttributes) {
        try {
            notificationService.markAsRead(notificationId);
            redirectAttributes.addFlashAttribute("success", "Notification marked as read!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/marketing/notifications";
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
        return "redirect:/marketing/notifications";
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
        return "redirect:/marketing/notifications";
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
        return "redirect:/marketing/notifications";
    }
    
    @GetMapping("/promotions")
    public String promotions(Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        model.addAttribute("user", user);
        // For now, we'll show a basic promotions page
        // In a real implementation, you would fetch promotions from a service
        return "marketing/promotions";
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
        return "redirect:/marketing/notifications";
    }
    
    private User getCurrentUser(Authentication authentication) {
        return ((com.hotel.eventreservation.service.CustomUserDetailsService.CustomUserPrincipal) 
                authentication.getPrincipal()).getUser();
    }
    
    /**
     * Calculate total revenue from revenue data
     */
    private double calculateTotalRevenue(List<Map<String, Object>> revenueData) {
        return revenueData.stream()
                .mapToDouble(item -> {
                    Object revenue = item.get("Revenue");
                    if (revenue instanceof Number) {
                        return ((Number) revenue).doubleValue();
                    } else if (revenue instanceof String) {
                        try {
                            return Double.parseDouble((String) revenue);
                        } catch (NumberFormatException e) {
                            return 0.0;
                        }
                    }
                    return 0.0;
                })
                .sum();
    }
    
    /**
     * Calculate cancellation rate from booking analytics
     */
    private double calculateCancellationRate(List<Map<String, Object>> bookingAnalytics) {
        if (bookingAnalytics.isEmpty()) {
            return 0.0;
        }
        
        long cancelledCount = bookingAnalytics.stream()
                .mapToLong(item -> {
                    Object status = item.get("Status");
                    return "CANCELLED".equals(status.toString()) ? 1 : 0;
                })
                .sum();
        
        return (double) cancelledCount / bookingAnalytics.size() * 100;
    }
    
    /**
     * Calculate average venue occupancy from booking analytics
     */
    private double calculateVenueOccupancy(List<Map<String, Object>> bookingAnalytics) {
        if (bookingAnalytics.isEmpty()) {
            return 0.0;
        }
        
        double totalOccupancy = bookingAnalytics.stream()
                .mapToDouble(item -> {
                    Object guestCount = item.get("Guest Count");
                    
                    // This is a simplified calculation
                    // In a real implementation, you'd need venue capacity data
                    if (guestCount instanceof Number) {
                        return ((Number) guestCount).doubleValue();
                    }
                    return 0.0;
                })
                .average()
                .orElse(0.0);
        
        // Assuming average venue capacity of 100 for calculation
        // In a real implementation, you'd get actual venue capacity
        return Math.min(totalOccupancy / 100.0 * 100, 100.0);
    }
}
