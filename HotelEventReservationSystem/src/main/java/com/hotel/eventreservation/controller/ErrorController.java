package com.hotel.eventreservation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                model.addAttribute("errorCode", "404");
                model.addAttribute("errorMessage", "Page Not Found");
                model.addAttribute("errorDescription", "The page you are looking for does not exist.");
                return "error/404";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                model.addAttribute("errorCode", "500");
                model.addAttribute("errorMessage", "Internal Server Error");
                model.addAttribute("errorDescription", "Something went wrong on our end. Please try again later.");
                return "error/500";
            } else if (statusCode == HttpStatus.METHOD_NOT_ALLOWED.value()) {
                model.addAttribute("errorCode", "405");
                model.addAttribute("errorMessage", "Method Not Allowed");
                model.addAttribute("errorDescription", "This action must be performed via the correct HTTP method.");
                return "error/500";
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                model.addAttribute("errorCode", "403");
                model.addAttribute("errorMessage", "Access Denied");
                model.addAttribute("errorDescription", "You don't have permission to access this resource.");
                return "error/403";
            }
        }
        
        model.addAttribute("errorCode", "500");
        model.addAttribute("errorMessage", "Internal Server Error");
        model.addAttribute("errorDescription", "Something went wrong on our end. Please try again later.");
        return "error/500";
    }
}
