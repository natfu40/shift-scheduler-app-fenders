# Admin User Setup and Shift Management Guide

## Initial Admin User Creation

When the application starts for the first time, a default admin user is automatically created with the following credentials:

- **Email**: `admin@example.com`
- **Password**: `admin123`

**⚠️ IMPORTANT**: Change this password immediately in a production environment!

### Using the Default Admin Account

1. Navigate to `http://localhost:3000/login`
2. Enter the credentials above
3. Click "Login"
4. You will be redirected to the Employee Dashboard
5. Click "Admin" in the navigation bar to access the Admin Dashboard

## Admin Dashboard Features

### 1. Manage Shifts

The Admin Dashboard allows you to create, view, and manage shifts:

#### Creating a New Shift

1. Click the "Create New Shift" button
2. Fill in the shift details:
   - **Shift Name**: A descriptive name (e.g., "Morning Shift - Monday")
   - **Description**: Optional details about the shift (e.g., "7 AM - 3 PM, High traffic expected")
   - **Start Time**: Select the date and time when the shift begins
   - **End Time**: Select the date and time when the shift ends
   - **Available Slots**: Number of employees needed for this shift

3. Click "Create Shift" to add the shift to the system
4. The shift will immediately become available for employees to view and sign up for

#### Viewing Shifts

All created shifts are displayed in a table showing:
- Shift Name
- Start Time
- End Time
- Number of Available Slots
- Number of Filled Slots

### 2. Audit Logs

The Audit Logs tab shows a complete history of all system activities:

- **Timestamp**: When the action occurred
- **User**: Which user performed the action
- **Action**: Type of action (e.g., CREATE_SHIFT, LOGIN, SIGNUP)
- **Entity**: What was affected (e.g., Shift, User)
- **Description**: Details about the action
- **IP Address**: Where the request came from

## Managing Additional Admins

### Making a User an Admin

If you have an existing user account you want to promote to admin, you can use the admin API endpoint:

```bash
POST /api/admin/users/{userId}/make-admin
Authorization: Bearer {your_admin_token}
```

### Removing Admin Privileges

To remove admin privileges from a user:

```bash
POST /api/admin/users/{userId}/remove-admin
Authorization: Bearer {your_admin_token}
```

### Checking if a User is Admin

```bash
GET /api/admin/users/{userId}/is-admin
Authorization: Bearer {your_admin_token}
```

## Security Notes

1. **Default Password**: The demo admin account uses a weak password (`admin123`). Change it immediately in production.
2. **Role-Based Access Control**: Only users with the ADMIN role can:
   - Create shifts
   - Update shifts
   - Delete shifts
   - View audit logs
   - Manage other admins
3. **JWT Tokens**: All authenticated requests require a valid JWT token in the Authorization header

## Creating Additional Admin Users

### Via Database Script

If you have direct database access, you can create additional admin users:

```sql
-- 1. Insert a new user
INSERT INTO users (email, password, first_name, last_name, active, created_at, updated_at)
VALUES ('newadmin@example.com', '$2a$10$...bcrypted_password...', 'New', 'Admin', true, NOW(), NOW());

-- 2. Insert the ADMIN role if it doesn't exist
INSERT INTO roles (name, description, created_at)
VALUES ('ADMIN', 'Administrator role with full access', NOW())
ON DUPLICATE KEY UPDATE id=id;

-- 3. Assign the ADMIN role to the user
INSERT INTO user_roles (user_id, role_id, assigned_at)
SELECT u.id, r.id, NOW()
FROM users u
JOIN roles r ON r.name = 'ADMIN'
WHERE u.email = 'newadmin@example.com';
```

### Via Application Frontend

Use the API endpoint to promote an existing user to admin:

1. First, create a regular user account via signup
2. Get the user's ID from the database or API response
3. Call the admin endpoint (requires existing admin credentials):

```bash
curl -X POST http://localhost:8080/api/admin/users/{userId}/make-admin \
  -H "Authorization: Bearer {admin_token}" \
  -H "Content-Type: application/json"
```

## Troubleshooting

### I can't access the Admin Dashboard

- Ensure you're logged in with an account that has the ADMIN role
- Check that your JWT token hasn't expired
- Verify the user is properly assigned to the ADMIN role in the database

### Shifts aren't showing up

- Ensure the shift's start time is in the future
- Check that the shift is marked as active (default is true)
- Verify the user has proper admin permissions

### Can't create a shift

This usually means:
- Your user doesn't have the ADMIN role
- Your JWT token is invalid or expired
- The request payload is missing required fields
- The shift times are invalid (end time before start time)

## Environment Variables

For production, set these environment variables:

- `JWT_SECRET`: A strong, random string (minimum 32 characters)
- `DB_USERNAME`: Database username
- `DB_PASSWORD`: Database password
- `SPRING_DATASOURCE_URL`: Database connection URL

## Next Steps

1. Change the default admin password
2. Create additional admin users as needed
3. Start creating shifts for your employees
4. Employees can now view available shifts and sign up
5. Monitor activity through the Audit Logs

