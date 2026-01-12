# Quick Start - Admin Features

## TL;DR - Get Started in 2 Minutes

### 1. Login with Default Admin Credentials

```
Email: admin@example.com
Password: admin123
```

Go to: http://localhost:3000/login

### 2. Access Admin Dashboard

After login, click "Admin" in the navigation bar

### 3. Create Your First Shift

1. Click "Create New Shift"
2. Enter shift details:
   - Name: "Morning Shift"
   - Start Time: Pick a future date/time
   - End Time: Pick an end time
   - Available Slots: 5
3. Click "Create Shift"

### 4. Employees Can Now Sign Up

Regular users can:
- Go to the Employee Dashboard
- View available shifts
- Sign up for shifts they want to work

## Test with cURL

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "admin123"
  }'
```

Response will include a JWT token. Copy the `token` value.

### Create a Shift (using the token)
```bash
curl -X POST http://localhost:8080/api/shifts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{
    "name": "Evening Shift",
    "description": "5 PM to 1 AM",
    "startTime": "2026-01-15T17:00:00",
    "endTime": "2026-01-16T01:00:00",
    "availableSlots": 3
  }'
```

### Get Available Shifts (public endpoint, no auth needed)
```bash
curl -X GET "http://localhost:8080/api/shifts/available?page=0&size=10"
```

### View Audit Logs (requires admin)
```bash
curl -X GET "http://localhost:8080/api/audit-logs?page=0&size=50" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## What Changed?

✅ **Created Admin Features:**
- Admin role management system
- Admin user initialization on startup
- Admin Dashboard to create/manage shifts
- Audit logging for all admin actions
- Role-based access control (@PreAuthorize decorators)

✅ **Backend Services:**
- `AdminService`: Manages admin role assignments
- `AdminController`: REST endpoints for admin operations
- `DataInitializer`: Creates default admin user on first run
- `CustomUserDetailsService`: Now loads actual roles from database

✅ **Frontend Enhancements:**
- Updated `AdminDashboard.js` with permission checking
- Added admin API client methods
- Better error handling for 403 Forbidden responses

## Next Steps

1. **Change Default Password**
   - Login with admin@example.com / admin123
   - (Password change feature coming soon)

2. **Create Regular Users**
   - Use signup page
   - Create shifts and assign them slots
   - Regular users will get ROLE_USER automatically

3. **Promote Users to Admin**
   - Use the `/api/admin/users/{userId}/make-admin` endpoint
   - Or update the database directly

4. **Monitor Activity**
   - Check Admin Dashboard → Audit Logs tab
   - See all user actions and system events

## Files Modified/Created

### Backend
- ✅ `AdminController.java` - REST endpoints for admin management
- ✅ `AdminService.java` - Business logic for admin operations  
- ✅ `AdminResponse.java` - DTO for admin API responses
- ✅ `DataInitializer.java` - Initializes default admin user
- ✅ `CustomUserDetailsService.java` - Now loads roles from database
- ✅ `SecurityConfig.java` - Added @EnableMethodSecurity

### Frontend
- ✅ `AdminDashboard.js` - Enhanced with permission checks
- ✅ `client.js` - Added adminAPI methods

### Documentation
- ✅ `ADMIN_GUIDE.md` - Comprehensive admin guide
- ✅ `QUICKSTART_ADMIN.md` - This file

## Security Reminders

⚠️ **Important for Production:**
- Change the default admin password immediately
- Set a strong JWT_SECRET (min 32 characters)
- Use HTTPS instead of HTTP
- Keep database credentials secure
- Regular database backups
- Monitor audit logs regularly

