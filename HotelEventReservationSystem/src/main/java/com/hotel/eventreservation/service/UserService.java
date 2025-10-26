package com.hotel.eventreservation.service;

import com.hotel.eventreservation.model.Role;
import com.hotel.eventreservation.model.User;
import com.hotel.eventreservation.repository.RoleRepository;
import com.hotel.eventreservation.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;
    
    /**
     * Create a new user
     */
    public User createUser(String username, String email, String password, String firstName, String lastName, String phone) {
        // Validate input parameters
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be null or empty");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be null or empty");
        }
        
        try {
            if (userRepository.existsByUsername(username)) {
                throw new RuntimeException("Username already exists");
            }
            
            if (userRepository.existsByEmail(email)) {
                throw new RuntimeException("Email already exists");
            }
            
            User user = new User(username, email, passwordEncoder.encode(password), firstName, lastName);
            user.setPhone(phone);
            
            // Assign default role as GUEST
            Role guestRole = roleRepository.findByRoleName("GUEST")
                    .orElseThrow(() -> new RuntimeException("Guest role not found"));
            user.getRoles().add(guestRole);
            
            user = userRepository.save(user);
            logger.info("User created successfully: {}", username);
            return user;
        } catch (Exception e) {
            logger.error("Error creating user: {}", username, e);
            throw e;
        }
    }
    
    /**
     * Update user information
     */
    public User updateUser(Long userId, String firstName, String lastName, String phone) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        User user = userOpt.get();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(phone);
        
        user = userRepository.save(user);
        logger.info("User updated successfully: {}", user.getUsername());
        return user;
    }
    
    /**
     * Change user password
     */
    public User changePassword(Long userId, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        
        user = userRepository.save(user);
        logger.info("Password changed successfully for user: {}", user.getUsername());
        return user;
    }
    
    /**
     * Assign role to user
     */
    public User assignRole(Long userId, String roleName) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        Optional<Role> roleOpt = roleRepository.findByRoleName(roleName);
        if (roleOpt.isEmpty()) {
            throw new RuntimeException("Role not found");
        }
        
        User user = userOpt.get();
        Role role = roleOpt.get();
        
        if (!user.getRoles().contains(role)) {
            user.getRoles().add(role);
            user = userRepository.save(user);
            logger.info("Role {} assigned to user: {}", roleName, user.getUsername());
        }
        
        return user;
    }
    
    /**
     * Remove role from user
     */
    public User removeRole(Long userId, String roleName) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        User user = userOpt.get();
        user.getRoles().removeIf(role -> role.getRoleName().equals(roleName));
        
        user = userRepository.save(user);
        logger.info("Role {} removed from user: {}", roleName, user.getUsername());
        return user;
    }
    
    /**
     * Find user by username
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * Find user by email
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * Find user by ID
     */
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }
    
    /**
     * Get all active users
     */
    public List<User> getAllActiveUsers() {
        return userRepository.findByIsActiveTrue();
    }
    
    /**
     * Get users by role
     */
    public List<User> getUsersByRole(String roleName) {
        return userRepository.findActiveUsersByRoleName(roleName);
    }
    
    /**
     * Search users by name
     */
    public List<User> searchUsersByName(String name) {
        return userRepository.findByNameContaining(name);
    }
    
    /**
     * Deactivate user
     */
    public User deactivateUser(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        User user = userOpt.get();
        user.setIsActive(false);
        
        user = userRepository.save(user);
        logger.info("User deactivated: {}", user.getUsername());
        return user;
    }
    
    /**
     * Activate user
     */
    public User activateUser(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        User user = userOpt.get();
        user.setIsActive(true);
        
        user = userRepository.save(user);
        logger.info("User activated: {}", user.getUsername());
        return user;
    }
    
    /**
     * Check if user has specific role
     */
    public boolean hasRole(User user, String roleName) {
        return user.hasRole(roleName);
    }
    
    /**
     * Get user roles
     */
    public Set<Role> getUserRoles(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        return userOpt.get().getRoles();
    }
}
