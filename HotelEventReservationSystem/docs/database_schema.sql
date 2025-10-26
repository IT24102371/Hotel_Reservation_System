-- Hotel Event Reservation System Database Schema
-- For XAMPP MySQL Server

CREATE DATABASE IF NOT EXISTS hotel_event_db;
USE hotel_event_db;

-- 1. User Management and Role-Based Access Control (RBAC)

-- Roles table
CREATE TABLE roles (
    role_id INT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Users table
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- User-Role junction table
CREATE TABLE user_roles (
    user_role_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    role_id INT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_role (user_id, role_id)
);

-- 2. Venue Management (Rooms & Halls)

-- Venues table (for both Rooms and Halls)
CREATE TABLE venues (
    venue_id INT PRIMARY KEY AUTO_INCREMENT,
    venue_name VARCHAR(100) NOT NULL,
    venue_type ENUM('ROOM', 'HALL') NOT NULL,
    capacity INT NOT NULL,
    description TEXT,
    hourly_rate DECIMAL(10,2) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Venue Availability table
CREATE TABLE venue_availability (
    availability_id INT PRIMARY KEY AUTO_INCREMENT,
    venue_id INT NOT NULL,
    date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status ENUM('AVAILABLE', 'BOOKED', 'MAINTENANCE', 'BLOCKED') DEFAULT 'AVAILABLE',
    booking_id INT NULL,
    notes TEXT NULL,
    maintenance_reason VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (venue_id) REFERENCES venues(venue_id) ON DELETE CASCADE,
    UNIQUE KEY unique_venue_time (venue_id, date, start_time, end_time)
);

-- 3. Event Booking and Customization

-- Bookings table
CREATE TABLE bookings (
    booking_id INT PRIMARY KEY AUTO_INCREMENT,
    guest_id INT NOT NULL,
    venue_id INT NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    guest_count INT NOT NULL,
    total_cost DECIMAL(10,2) NOT NULL,
    booking_status ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED') DEFAULT 'PENDING',
    reference_code VARCHAR(50) NOT NULL UNIQUE,
    qr_code_path TEXT,
    special_requests TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (guest_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (venue_id) REFERENCES venues(venue_id) ON DELETE CASCADE
);

-- Decor Preferences table
CREATE TABLE decor_preferences (
    decor_id INT PRIMARY KEY AUTO_INCREMENT,
    booking_id INT NOT NULL,
    theme VARCHAR(100),
    color_scheme VARCHAR(100),
    flower_arrangements TEXT,
    lighting_preferences TEXT,
    additional_decor_requests TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE
);

-- Catering Preferences table
CREATE TABLE catering_preferences (
    catering_id INT PRIMARY KEY AUTO_INCREMENT,
    booking_id INT NOT NULL,
    meal_type ENUM('BREAKFAST', 'LUNCH', 'DINNER', 'SNACKS', 'COCKTAILS') NOT NULL,
    cuisine_type VARCHAR(100),
    dietary_restrictions TEXT,
    special_dishes TEXT,
    beverage_preferences TEXT,
    serving_style ENUM('BUFFET', 'PLATED', 'FAMILY_STYLE', 'COCKTAIL') DEFAULT 'BUFFET',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE
);

-- 4. Notifications and Alerts

-- Notifications table
CREATE TABLE notifications (
    notification_id INT PRIMARY KEY AUTO_INCREMENT,
    recipient_user_id INT NOT NULL,
    sender_type ENUM('SYSTEM', 'STAFF') DEFAULT 'SYSTEM',
    sender_user_id INT NULL,
    message_content TEXT NOT NULL,
    alert_type ENUM('GUEST_ARRIVAL', 'BOOKING_CHANGE', 'COORDINATION_ALERT', 'PAYMENT_REMINDER', 'EVENT_REMINDER', 'BOOKING_CONFIRMATION', 'BOOKING_CANCELLATION', 'SETUP_COMPLETE', 'CATERING_CONFIRMED') NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL,
    FOREIGN KEY (recipient_user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (sender_user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

-- 5. Insert Initial Data

-- Insert default roles
INSERT INTO roles (role_name, description) VALUES
('GUEST', 'External customers who book events'),
('GENERAL_MANAGER', 'Oversees all bookings and schedules, manages internal coordination'),
('EVENT_COORDINATOR', 'Handles décor, guest requests, and event planning'),
('CATERING_TEAM_LEADER', 'Manages food services for events'),
('MARKETING_EXECUTIVE', 'Analyzes bookings, trends, and manages promotions'),
('RECEPTIONIST', 'Verifies bookings using QR/Ref code and handles guest arrival');

-- Insert sample venues
INSERT INTO venues (venue_name, venue_type, capacity, description, hourly_rate) VALUES
('Grand Ballroom', 'HALL', 200, 'Elegant ballroom perfect for weddings and large events', 150.00),
('Conference Room A', 'ROOM', 50, 'Professional conference room with AV equipment', 75.00),
('Garden Pavilion', 'HALL', 100, 'Outdoor covered pavilion with garden views', 120.00),
('Executive Boardroom', 'ROOM', 20, 'Intimate boardroom for executive meetings', 100.00),
('Rooftop Terrace', 'HALL', 80, 'Scenic rooftop venue with city views', 180.00),
('Meeting Room B', 'ROOM', 30, 'Standard meeting room with whiteboard', 60.00);

-- Insert all users with their actual data
INSERT INTO users (user_id, username, email, password, first_name, last_name, phone, is_active, created_at, updated_at) VALUES
(1, 'admin', 'admin@hotel.com', '$2a$10$LBQcFN2ketp/H7shylEopeE83FscbKEzU3TeFU6vJpgg/ZXZIax7K', 'System', 'Administrator', '1234567890', 1, '2025-10-21 15:32:28', '2025-10-21 10:36:08'),
(2, 'sithika', 'sithii.walisundara@gmail.com', '$2a$10$LBQcFN2ketp/H7shylEopeE83FscbKEzU3TeFU6vJpgg/ZXZIax7K', 'Sithika', 'Wathsula', '0723180882', 1, '2025-10-21 10:04:20', '2025-10-21 10:04:20'),
(3, 'catering', 'catering@hotel.com', '$2a$10$i4sD2A1Nu1mnrAIn10yHIOJ3SScQUhRQVQzQlfE0qtrqXoL4p.JqO', 'Catering', 'Service', '0123456789', 1, '2025-10-22 01:27:44', '2025-10-22 01:27:44'),
(4, 'coordinator', 'coordinator@hotel.com', '$2a$10$SVlrpR/ezJh1Noxz6Sc7ROrMs3hNR7L2tCAj/OUG5gaYkewoSn5ka', 'Event', 'Coordinator', '1111111111', 1, '2025-10-22 01:29:36', '2025-10-22 01:29:36'),
(5, 'marketing', 'marketing@hotel.com', '$2a$10$uDrxvL2LG7mx76rLDxEMmuya/.el50ogjcoNABzL3ODjyT7VQw6QG', 'Marketing', 'Executive', '2222222222', 1, '2025-10-22 01:30:54', '2025-10-22 01:30:54'),
(6, 'reception', 'reception@hotel.com', '$2a$10$0i1dQ8fKeeYHHmBukQ0oJeGyq3FK1yO01.GUIBtQKRVjHwONyW5qS', 'Recieption', 'Staff', '3333333333', 1, '2025-10-22 01:32:14', '2025-10-22 01:32:14');

-- Assign roles to users
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 2), -- admin -> GENERAL_MANAGER role (role_id=2)
(2, 1), -- sithika -> GUEST role (role_id=1)
(3, 4), -- catering -> CATERING_TEAM_LEADER role (role_id=4)
(4, 3), -- coordinator -> EVENT_COORDINATOR role (role_id=3)
(5, 5), -- marketing -> MARKETING_EXECUTIVE role (role_id=5)
(6, 6); -- reception -> RECEPTIONIST role (role_id=6)

-- Create indexes for better performance
CREATE INDEX idx_bookings_guest_id ON bookings(guest_id);
CREATE INDEX idx_bookings_venue_id ON bookings(venue_id);
CREATE INDEX idx_bookings_event_date ON bookings(event_date);
CREATE INDEX idx_bookings_status ON bookings(booking_status);
CREATE INDEX idx_venue_availability_date ON venue_availability(date);
CREATE INDEX idx_notifications_recipient ON notifications(recipient_user_id);
CREATE INDEX idx_notifications_unread ON notifications(recipient_user_id, is_read);
