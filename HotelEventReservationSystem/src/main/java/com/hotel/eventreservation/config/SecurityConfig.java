package com.hotel.eventreservation.config;

import com.hotel.eventreservation.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/images/**", "/verify-booking/**").permitAll()
                
                // Guest endpoints
                .requestMatchers("/guest/**").hasRole("GUEST")
                
                // Availability endpoints
                .requestMatchers("/availability/calendar").hasRole("GUEST")
                .requestMatchers("/availability/manage", "/availability/create", "/availability/create-bulk", 
                        "/availability/update-status/**", "/availability/block-maintenance", 
                        "/availability/delete/**", "/availability/bulk-delete", "/availability/search",
                        "/availability/details/**")
                        .hasAnyRole("GENERAL_MANAGER", "EVENT_COORDINATOR", "CATERING_TEAM_LEADER", "MARKETING_EXECUTIVE", "RECEPTIONIST")
                .requestMatchers("/availability/details/**", "/availability/summary", "/availability/calendar-data")
                                .hasAnyRole("GUEST", "GENERAL_MANAGER", "EVENT_COORDINATOR", "CATERING_TEAM_LEADER", "MARKETING_EXECUTIVE", "RECEPTIONIST")
                
                // Staff endpoints
                .requestMatchers("/staff/**").hasAnyRole("GENERAL_MANAGER", "EVENT_COORDINATOR", "CATERING_TEAM_LEADER", "MARKETING_EXECUTIVE", "RECEPTIONIST")
                
                // Role-specific endpoints
                .requestMatchers("/manager/**").hasRole("GENERAL_MANAGER")
                .requestMatchers("/coordinator/**").hasRole("EVENT_COORDINATOR")
                .requestMatchers("/catering/**").hasRole("CATERING_TEAM_LEADER")
                .requestMatchers("/marketing/**").hasRole("MARKETING_EXECUTIVE")
                .requestMatchers("/reception/**").hasRole("RECEPTIONIST")
                
                // Data management endpoints - allow all authenticated users for now
                .requestMatchers("/data/**").authenticated()
                
                // API endpoints
                .requestMatchers("/api/guest/**").hasRole("GUEST")
                .requestMatchers("/api/staff/**").hasAnyRole("GENERAL_MANAGER", "EVENT_COORDINATOR", "CATERING_TEAM_LEADER", "MARKETING_EXECUTIVE", "RECEPTIONIST")
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )
            .authenticationProvider(authenticationProvider());
            
        return http.build();
    }
}
