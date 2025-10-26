package com.hotel.eventreservation.config;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception ex, HttpServletRequest request, Model model) {
        System.err.println("Unhandled exception occurred: " + ex.getMessage());
        ex.printStackTrace();
        
        model.addAttribute("errorCode", "500");
        model.addAttribute("errorMessage", "Internal Server Error");
        model.addAttribute("errorDescription", "Something went wrong on our end. Please try again later.");
        model.addAttribute("requestUrl", request.getRequestURL());
        
        return "error/500";
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request, Model model) {
        System.err.println("Bad Request: " + ex.getMessage());
        
        model.addAttribute("errorCode", "400");
        model.addAttribute("errorMessage", "Bad Request");
        model.addAttribute("errorDescription", ex.getMessage());
        model.addAttribute("requestUrl", request.getRequestURL());
        
        return "error/500"; // Use 500 template for now
    }
    
    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleSecurityException(SecurityException ex, HttpServletRequest request, Model model) {
        System.err.println("Access Denied: " + ex.getMessage());
        
        model.addAttribute("errorCode", "403");
        model.addAttribute("errorMessage", "Access Denied");
        model.addAttribute("errorDescription", "You don't have permission to access this resource.");
        model.addAttribute("requestUrl", request.getRequestURL());
        
        return "error/403";
    }
}
