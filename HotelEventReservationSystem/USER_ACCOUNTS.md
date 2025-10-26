# Hotel Event Reservation System - User Accounts

## User Login Information

All users can login with their username and password: **password123**

### Admin Users (General Manager Role)
| User ID | Username | Email | Name | Phone | Role |
|---------|----------|-------|------|-------|------|
| 1 | admin | admin@hotel.com | System Administrator | 1234567890 | General Manager |

### Guest Users
| 2 | sithika | sithii.walisundara@gmail.com | Sithika Wathsula | 0723180882 | Guest |

### Staff Users
| User ID | Username | Email | Name | Phone | Role |
|---------|----------|-------|------|-------|------|
| 3 | catering | catering@hotel.com | Catering Service | 0123456789 | Catering Team Leader |
| 4 | coordinator | coordinator@hotel.com | Event Coordinator | 1111111111 | Event Coordinator |
| 5 | marketing | marketing@hotel.com | Marketing Executive | 2222222222 | Marketing Executive |
| 6 | reception | reception@hotel.com | Reception Staff | 3333333333 | Receptionist |

## Role Permissions

### General Manager (admin, sithika)
- Access to manager dashboard
- View all bookings and manage staff
- Access to notifications
- Full system administration

### Catering Team Leader (catering)
- Access to catering dashboard
- Manage catering tasks and inventory
- View catering-related notifications
- Confirm catering arrangements

### Event Coordinator (coordinator)
- Access to coordinator dashboard
- Manage event setup and tasks
- View coordinator notifications
- Update room setup status

### Marketing Executive (marketing)
- Access to marketing dashboard
- Generate reports and analytics
- View marketing notifications
- Analyze booking trends

### Receptionist (reception)
- Access to reception dashboard
- Verify bookings using QR codes
- Check today's arrivals
- View reception notifications

## Database Schema

The users are stored in the `users` table with the following structure:
- `user_id`: Primary key
- `username`: Unique username for login
- `email`: User's email address
- `password`: BCrypt hashed password
- `first_name`, `last_name`: User's full name
- `phone`: Contact number
- `is_active`: Account status (1 = active)
- `created_at`, `updated_at`: Timestamps

## Password Information

- **Current Password**: password123
- **Hash Algorithm**: BCrypt with strength 10
- **Hash Value**: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi

## Security Notes

- All passwords are currently set to "password123" for testing purposes
- For production deployment, consider implementing stronger password policies
- Users should change their passwords after initial login
- The system supports role-based access control (RBAC)
