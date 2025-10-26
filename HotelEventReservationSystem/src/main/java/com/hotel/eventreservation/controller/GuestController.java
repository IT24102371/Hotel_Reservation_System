package com.hotel.eventreservation.controller;

import com.hotel.eventreservation.model.*;
import com.hotel.eventreservation.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/guest")
public class GuestController {
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private VenueService venueService;
    
    // @Autowired
    // private UserService userService; // Not currently used in this controller
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private com.hotel.eventreservation.service.AvailabilityService availabilityService;
    
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        List<Booking> bookings = bookingService.getBookingsByGuest(user.getUserId());
        
        // Calculate booking counts
        long totalBookings = bookings != null ? bookings.size() : 0;
        long pendingBookings = bookings != null ? bookings.stream()
                .filter(b -> b != null && b.getBookingStatus() == Booking.BookingStatus.PENDING)
                .count() : 0;
        long confirmedBookings = bookings != null ? bookings.stream()
                .filter(b -> b != null && b.getBookingStatus() == Booking.BookingStatus.CONFIRMED)
                .count() : 0;
        long cancelledBookings = bookings != null ? bookings.stream()
                .filter(b -> b != null && b.getBookingStatus() == Booking.BookingStatus.CANCELLED)
                .count() : 0;
        
        model.addAttribute("user", user);
        model.addAttribute("bookings", bookings);
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("pendingBookings", pendingBookings);
        model.addAttribute("confirmedBookings", confirmedBookings);
        model.addAttribute("cancelledBookings", cancelledBookings);
        return "guest/dashboard";
    }
    
    @GetMapping("/book-event")
    public String bookEvent(Model model) {
        List<Venue> venues = venueService.getAllActiveVenues();
        model.addAttribute("venues", venues);
        model.addAttribute("booking", new Booking());
        model.addAttribute("decorPrefs", new DecorPreferences());
        model.addAttribute("cateringPrefs", new CateringPreferences());
        return "guest/book-event";
    }
    
    @PostMapping("/book-event")
    public String submitBooking(@RequestParam Long venueId,
                               @RequestParam String eventType,
                               @RequestParam String eventDate,
                               @RequestParam String startTime,
                               @RequestParam String endTime,
                               @RequestParam Integer guestCount,
                               @RequestParam(required = false) String specialRequests,
                               @RequestParam(required = false) String theme,
                               @RequestParam(required = false) String colorScheme,
                               @RequestParam(required = false) String flowerArrangements,
                               @RequestParam(required = false) String lightingPreferences,
                               @RequestParam(required = false) String additionalDecorRequests,
                               @RequestParam(required = false) String mealType,
                               @RequestParam(required = false) String cuisineType,
                               @RequestParam(required = false) String dietaryRestrictions,
                               @RequestParam(required = false) String specialDishes,
                               @RequestParam(required = false) String beveragePreferences,
                               @RequestParam(required = false) String servingStyle,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser(authentication);
            Venue venue = venueService.findById(venueId).orElseThrow(() -> new RuntimeException("Venue not found"));
            
            // Create decor preferences
            DecorPreferences decorPrefs = null;
            if (theme != null || colorScheme != null || flowerArrangements != null || 
                lightingPreferences != null || additionalDecorRequests != null) {
                decorPrefs = new DecorPreferences();
                decorPrefs.setTheme(theme);
                decorPrefs.setColorScheme(colorScheme);
                decorPrefs.setFlowerArrangements(flowerArrangements);
                decorPrefs.setLightingPreferences(lightingPreferences);
                decorPrefs.setAdditionalDecorRequests(additionalDecorRequests);
            }
            
            // Create catering preferences
            CateringPreferences cateringPrefs = null;
            if (mealType != null) {
                cateringPrefs = new CateringPreferences();
                cateringPrefs.setMealType(CateringPreferences.MealType.valueOf(mealType));
                cateringPrefs.setCuisineType(cuisineType);
                cateringPrefs.setDietaryRestrictions(dietaryRestrictions);
                cateringPrefs.setSpecialDishes(specialDishes);
                cateringPrefs.setBeveragePreferences(beveragePreferences);
                if (servingStyle != null) {
                    cateringPrefs.setServingStyle(CateringPreferences.ServingStyle.valueOf(servingStyle));
                }
            }
            
            Booking booking = bookingService.createBooking(
                user, venue, eventType, 
                LocalDate.parse(eventDate), 
                LocalTime.parse(startTime), 
                LocalTime.parse(endTime),
                guestCount, specialRequests, decorPrefs, cateringPrefs
            );
            
            redirectAttributes.addFlashAttribute("success", "Booking created successfully! Reference: " + booking.getReferenceCode());
            return "redirect:/guest/booking-confirmation/" + booking.getBookingId();
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/guest/book-event";
        }
    }
    
    @GetMapping("/booking-confirmation/{bookingId}")
    public String bookingConfirmation(@PathVariable Long bookingId, Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        Booking booking = bookingService.getBookingsByGuest(user.getUserId()).stream()
                .filter(b -> b.getBookingId().equals(bookingId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        model.addAttribute("booking", booking);
        return "guest/booking-confirmation";
    }
    
    @GetMapping("/my-bookings")
    public String myBookings(Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        List<Booking> bookings = bookingService.getBookingsByGuest(user.getUserId());
        
        model.addAttribute("user", user);
        model.addAttribute("bookings", bookings);
        return "guest/my-bookings";
    }
    
    @PostMapping("/cancel-booking/{bookingId}")
    public String cancelBooking(@PathVariable Long bookingId, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser(authentication);
            // Verify booking exists and belongs to user
            bookingService.getBookingsByGuest(user.getUserId()).stream()
                    .filter(b -> b.getBookingId().equals(bookingId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Booking not found"));
            
            bookingService.cancelBooking(bookingId);
            redirectAttributes.addFlashAttribute("success", "Booking cancelled successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/guest/my-bookings";
    }

    // Gracefully handle accidental GET on cancel URL by redirecting with a message
    @GetMapping("/cancel-booking/{bookingId}")
    public String cancelBookingGetNotAllowed(@PathVariable Long bookingId, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "Cancellation must be performed via POST from My Bookings page.");
        return "redirect:/guest/my-bookings";
    }
    
    @GetMapping("/edit-booking/{bookingId}")
    public String editBooking(@PathVariable Long bookingId, Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        Booking booking = bookingService.getBookingsByGuest(user.getUserId()).stream()
                .filter(b -> b.getBookingId().equals(bookingId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        // Only allow editing of pending bookings
        if (booking.getBookingStatus() != Booking.BookingStatus.PENDING) {
            throw new RuntimeException("Only pending bookings can be edited");
        }
        
        List<Venue> venues = venueService.getAllActiveVenues();
        model.addAttribute("booking", booking);
        model.addAttribute("venues", venues);
        model.addAttribute("decorPrefs", booking.getDecorPreferences() != null ? booking.getDecorPreferences() : new DecorPreferences());
        model.addAttribute("cateringPrefs", booking.getCateringPreferences() != null ? booking.getCateringPreferences() : new CateringPreferences());
        return "guest/edit-booking";
    }
    
    @PostMapping("/update-booking/{bookingId}")
    public String updateBooking(@PathVariable Long bookingId,
                               @RequestParam String eventType,
                               @RequestParam Long venueId,
                               @RequestParam String eventDate,
                               @RequestParam String startTime,
                               @RequestParam String endTime,
                               @RequestParam Integer guestCount,
                               @RequestParam(required = false) String specialRequests,
                               @RequestParam(required = false) String theme,
                               @RequestParam(required = false) String colorScheme,
                               @RequestParam(required = false) String flowerArrangements,
                               @RequestParam(required = false) String lightingPreferences,
                               @RequestParam(required = false) String additionalDecorRequests,
                               @RequestParam(required = false) String mealType,
                               @RequestParam(required = false) String cuisineType,
                               @RequestParam(required = false) String dietaryRestrictions,
                               @RequestParam(required = false) String specialDishes,
                               @RequestParam(required = false) String beveragePreferences,
                               @RequestParam(required = false) String servingStyle,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser(authentication);
            Booking booking = bookingService.getBookingsByGuest(user.getUserId()).stream()
                    .filter(b -> b.getBookingId().equals(bookingId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Booking not found"));
            
            // Only allow editing of pending bookings
            if (booking.getBookingStatus() != Booking.BookingStatus.PENDING) {
                throw new RuntimeException("Only pending bookings can be edited");
            }
            
            // Update booking details
            booking.setEventType(eventType);
            Venue venue = venueService.findById(venueId).orElseThrow(() -> new RuntimeException("Venue not found"));
            booking.setVenue(venue);
            booking.setEventDate(LocalDate.parse(eventDate));
            booking.setStartTime(LocalTime.parse(startTime));
            booking.setEndTime(LocalTime.parse(endTime));
            booking.setGuestCount(guestCount);
            booking.setSpecialRequests(specialRequests);
            
            // Update decor preferences
            if (theme != null || colorScheme != null || flowerArrangements != null || 
                lightingPreferences != null || additionalDecorRequests != null) {
                DecorPreferences decorPrefs = booking.getDecorPreferences();
                if (decorPrefs == null) {
                    decorPrefs = new DecorPreferences();
                    decorPrefs.setBooking(booking);
                    booking.setDecorPreferences(decorPrefs);
                }
                decorPrefs.setTheme(theme);
                decorPrefs.setColorScheme(colorScheme);
                decorPrefs.setFlowerArrangements(flowerArrangements);
                decorPrefs.setLightingPreferences(lightingPreferences);
                decorPrefs.setAdditionalDecorRequests(additionalDecorRequests);
            }
            
            // Update catering preferences
            if (mealType != null) {
                CateringPreferences cateringPrefs = booking.getCateringPreferences();
                if (cateringPrefs == null) {
                    cateringPrefs = new CateringPreferences();
                    cateringPrefs.setBooking(booking);
                    booking.setCateringPreferences(cateringPrefs);
                }
                cateringPrefs.setMealType(CateringPreferences.MealType.valueOf(mealType));
                cateringPrefs.setCuisineType(cuisineType);
                cateringPrefs.setDietaryRestrictions(dietaryRestrictions);
                cateringPrefs.setSpecialDishes(specialDishes);
                cateringPrefs.setBeveragePreferences(beveragePreferences);
                if (servingStyle != null) {
                    cateringPrefs.setServingStyle(CateringPreferences.ServingStyle.valueOf(servingStyle));
                }
            }
            
            // Recalculate total cost
            BigDecimal totalCost = bookingService.calculateTotalCost(venue, LocalTime.parse(startTime), LocalTime.parse(endTime));
            booking.setTotalCost(totalCost);
            
            bookingService.updateBooking(booking);
            redirectAttributes.addFlashAttribute("success", "Booking updated successfully!");
            return "redirect:/guest/my-bookings";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update booking: " + e.getMessage());
            return "redirect:/guest/edit-booking/" + bookingId;
        }
    }
    
    @GetMapping("/venues")
    public String viewVenues(Model model) {
        List<Venue> venues = venueService.getAllActiveVenues();
        model.addAttribute("venues", venues);
        return "guest/venues";
    }
    
    @GetMapping("/availability")
    public String viewAvailability(@RequestParam(required = false) Long venueId,
                                 @RequestParam(required = false) String month,
                                 Model model) {
        LocalDate targetMonth = month != null ? LocalDate.parse(month + "-01") : LocalDate.now();
        
        Map<String, Object> calendarData = availabilityService.getCalendarData(targetMonth, venueId);
        
        // Extract data from calendarData map and add as individual attributes
        model.addAttribute("calendarData", calendarData);
        model.addAttribute("selectedVenueId", venueId);
        model.addAttribute("targetMonth", targetMonth);
        model.addAttribute("startOfMonth", calendarData.get("startOfMonth"));
        model.addAttribute("endOfMonth", calendarData.get("endOfMonth"));
        model.addAttribute("groupedByDate", calendarData.get("groupedByDate"));
        model.addAttribute("availabilities", calendarData.get("availabilities"));
        
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
        List<Venue> venues = venueService.getAllActiveVenues();
        model.addAttribute("venues", venues);
        
        return "guest/availability";
    }
    
    @GetMapping("/notifications")
    public String notifications(@RequestParam(required = false, defaultValue = "ALL") String status,
                                Authentication authentication, Model model) {
        try {
            User user = getCurrentUser(authentication);
            if (user == null) {
                model.addAttribute("error", "User not found");
                model.addAttribute("notifications", java.util.Collections.emptyList());
                model.addAttribute("unreadCount", 0L);
                model.addAttribute("status", "ALL");
                return "guest/notifications";
            }
            
            System.out.println("Guest notifications - User ID: " + user.getUserId());
            
            List<com.hotel.eventreservation.model.Notification> notifications = new java.util.ArrayList<>();
            Long unreadCount = 0L;
            
            try {
                System.out.println("Guest notifications - Attempting to fetch notifications for user: " + user.getUserId());
                
                switch (status.toUpperCase()) {
                    case "UNREAD":
                        System.out.println("Guest notifications - Fetching UNREAD notifications");
                        notifications = notificationService.getUnreadNotificationsForUser(user.getUserId());
                        break;
                    case "READ":
                        System.out.println("Guest notifications - Fetching READ notifications");
                        notifications = notificationService.getReadNotificationsForUser(user.getUserId());
                        break;
                    default:
                        System.out.println("Guest notifications - Fetching ALL notifications");
                        notifications = notificationService.getNotificationsForUser(user.getUserId());
                }
                
                System.out.println("Guest notifications - Successfully fetched " + notifications.size() + " notifications");
                
                System.out.println("Guest notifications - Attempting to get unread count");
                unreadCount = notificationService.getUnreadNotificationCount(user.getUserId());
                System.out.println("Guest notifications - Unread count: " + unreadCount);
                
            } catch (Exception serviceException) {
                System.err.println("Guest notifications service error: " + serviceException.getMessage());
                serviceException.printStackTrace();
                notifications = new java.util.ArrayList<>();
                unreadCount = 0L;
            }
            
            model.addAttribute("notifications", notifications);
            model.addAttribute("unreadCount", unreadCount);
            model.addAttribute("status", status.toUpperCase());
        } catch (Exception e) {
            System.err.println("Guest notifications error: " + e.getMessage());
            e.printStackTrace();
            // Ensure the view still renders even if an error occurs
            model.addAttribute("notifications", java.util.Collections.emptyList());
            model.addAttribute("unreadCount", 0L);
            model.addAttribute("status", "ALL");
            model.addAttribute("error", "Unable to load notifications: " + e.getMessage());
        }
        return "guest/notifications";
    }
    
    @PostMapping("/notifications/{notificationId}/mark-read")
    public String markNotificationAsRead(@PathVariable Long notificationId, RedirectAttributes redirectAttributes) {
        try {
            notificationService.markAsRead(notificationId);
            redirectAttributes.addFlashAttribute("success", "Notification marked as read!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/guest/notifications";
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
        return "redirect:/guest/notifications";
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
        return "redirect:/guest/notifications";
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
        return "redirect:/guest/notifications";
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
        return "redirect:/guest/notifications";
    }
    
    
    private User getCurrentUser(Authentication authentication) {
        try {
            if (authentication == null || authentication.getPrincipal() == null) {
                return null;
            }
            return ((com.hotel.eventreservation.service.CustomUserDetailsService.CustomUserPrincipal) 
                    authentication.getPrincipal()).getUser();
        } catch (Exception e) {
            System.err.println("Error getting current user: " + e.getMessage());
            return null;
        }
    }
}
