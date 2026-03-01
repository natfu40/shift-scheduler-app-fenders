# 👨‍💼 Administrator Guide

Welcome to the Fenders Brewing Scheduler Administrator Guide. This comprehensive guide covers all administrative functions and system management tasks.

## 🔐 Getting Started as an Admin

### Initial System Setup

1. **Default Login Credentials**
   - Email: `admin@example.com`
   - Password: `admin123`
   - **⚠️ CRITICAL**: Change this password immediately after first login!

2. **First Login Checklist**
   - [ ] Log in with default credentials
   - [ ] Change the default admin password
   - [ ] Create additional user accounts
   - [ ] Set up initial shifts
   - [ ] Test the system functionality

### Admin Navigation

As an administrator, you have access to additional menu items:
- **🏠 Home**: Employee view (you can sign up for shifts too)
- **📅 Calendar**: Visual shift calendar
- **⚙️ Shift Management**: Core admin functions for shift management
- **👥 Users**: User account management
- **📋 Audits**: System activity logs
- **Logout**: Sign out of the system

## ⚙️ Shift Management

The **Shift Management** page is the core of administrative functionality.

### Creating New Shifts

#### Method 1: Create Individual Shift
1. Navigate to **Shift Management**
2. Click the green **"Create New Shift"** button
3. Fill out the shift form:
   - **Shift Name**: Brief, descriptive name (e.g., "Morning Brew", "Weekend Service")
   - **Description**: Detailed requirements and responsibilities
   - **Start Date & Time**: When the shift begins
   - **End Date & Time**: When the shift ends
   - **Available Slots**: Maximum number of employees for this shift
4. Click **"Create Shift"**
5. The shift will appear in the shifts list and become available to employees

#### Method 2: Add Default Shift Templates
1. Click the blue **"Add Default Shift"** button
2. Choose from pre-configured shift templates:
   - **Morning Shift**: 6:00 AM - 2:00 PM
   - **Afternoon Shift**: 2:00 PM - 10:00 PM
   - **Evening Shift**: 6:00 PM - 12:00 AM
   - **Weekend Shift**: 8:00 AM - 6:00 PM
3. Select the desired date
4. The template shift will be created with standard settings

### Managing Existing Shifts

#### Viewing Shifts
The main shifts table shows:
- **Shift Name and Description**
- **Date and Time Range**
- **Slot Information**: Filled/Available slots
- **Status Indicators**: Active, filled, or past shifts
- **Action Buttons**: Edit, Delete, View Signups

#### Editing Shifts
1. Find the shift in the list
2. Click the **"Edit"** button (pencil icon)
3. Modify any field:
   - Change times or dates
   - Update description
   - Increase/decrease available slots
   - **Note**: Be careful reducing slots if employees are already signed up
4. Click **"Save Changes"**

#### Deleting Shifts
1. Click the red **"Delete"** button (trash icon)
2. Confirm the deletion when prompted
3. **⚠️ Warning**: This permanently removes the shift and all associated signups

#### Bulk Operations

**Delete Shifts by Month**
1. Click the red **"Delete All Shifts for Month"** button
2. Select the target month and year
3. Confirm the bulk deletion
4. **Use Case**: Clearing old shifts or resetting schedules

### Managing Shift Signups

#### Viewing Shift Signups
1. Find the shift in the shifts list
2. Click **"View Signups"** to see all employee requests
3. The modal shows:
   - Employee name and contact information
   - Signup timestamp
   - Current status (Pending/Approved)
   - Action buttons for each signup

#### Approving Signups
1. Open the shift signups view
2. Find the pending signup request
3. Click the green **"Approve"** button
4. The employee will see the shift in their "Upcoming Shifts"
5. The shift's filled slots count will increase

#### Rejecting Signups
1. Open the shift signups view
2. Find the signup request
3. Click the red **"Reject"** button
4. Confirm the rejection
5. The employee will no longer see this in their pending requests
6. The shift slot remains available for other employees

## 👥 User Management

Navigate to the **Users** page to manage employee accounts.

### Creating New User Accounts

1. Click the green **"Add New User"** button
2. Fill out the user creation form:
   - **First Name**: Employee's first name
   - **Last Name**: Employee's last name
   - **Email Address**: Used for login (must be unique)
   - **Password**: Initial password for the employee
   - **Confirm Password**: Verify the password
3. Click **"Create User"**
4. The new user will be created with a `firstTimeLogin` flag
5. Provide the employee with their login credentials

### New User First-Time Login Process

When a new employee logs in for the first time:
1. They enter their email and initial password
2. The system automatically prompts them to change their password
3. They must create a new password (minimum 6 characters)
4. After password change, they can access the system normally
5. The `firstTimeLogin` flag is removed automatically

### Managing Existing Users

#### User List Overview
The Users page displays:
- **Employee Name**: Full name as entered
- **Email Address**: Login email
- **Account Status**: Active/Inactive
- **Admin Status**: Whether user has admin privileges
- **Account Actions**: Edit, deactivate, or manage roles

#### User Account Actions
- **Edit User**: Modify name or email (password changes require user to do it)
- **Toggle Admin Role**: Grant or remove administrative privileges
- **Deactivate Account**: Disable login without deleting the account
- **View User Activity**: See shifts and actions performed by this user

### Admin Role Management

#### Promoting Users to Admin
1. Find the user in the Users list
2. Look for users marked as "Regular User"
3. Contact your system administrator to change roles
4. **Note**: Role changes may require backend configuration

#### Admin Privileges
Administrators can:
- Access all employee functions (sign up for shifts)
- Create and manage shifts
- Approve/reject employee signups
- Create and manage user accounts
- View comprehensive audit logs
- Perform system maintenance tasks

## 📋 Audit Logs

The **Audits** page provides comprehensive activity tracking for compliance and monitoring.

### Understanding Audit Logs

Each audit entry contains:
- **Timestamp**: Exact time of the action
- **User**: Who performed the action
- **Action Type**: What was done
- **Details**: Specific information about the action
- **IP Address**: Where the action originated (if applicable)

### Common Audit Actions

#### User Actions
- `USER_LOGIN`: User successfully logged in
- `USER_LOGOUT`: User logged out
- `PASSWORD_CHANGE`: User changed their password
- `FAILED_LOGIN`: Unsuccessful login attempt

#### Shift Actions
- `SHIFT_CREATED`: New shift was created
- `SHIFT_UPDATED`: Existing shift was modified
- `SHIFT_DELETED`: Shift was permanently removed
- `SHIFT_SIGNUP`: Employee signed up for a shift
- `SIGNUP_APPROVED`: Admin approved a shift signup
- `SIGNUP_REJECTED`: Admin rejected a shift signup
- `SIGNUP_CANCELLED`: Employee cancelled their own signup

#### Administrative Actions
- `USER_CREATED`: New user account was created
- `USER_UPDATED`: User information was modified
- `ROLE_CHANGED`: User permissions were modified
- `ADMIN_ACCESS`: Administrative functions were accessed

### Using Audit Logs

#### Security Monitoring
- Review failed login attempts for security breaches
- Monitor after-hours access
- Track administrative actions for accountability
- Identify unusual activity patterns

#### Compliance Reporting
- Generate activity reports for specific time periods
- Track shift assignment history
- Monitor user account changes
- Document administrative decisions

#### Troubleshooting
- Identify when issues occurred
- Track the sequence of actions leading to problems
- Verify that processes were followed correctly
- Determine impact of system changes

### Audit Log Best Practices

1. **Regular Review**: Check audit logs weekly for unusual activity
2. **Retention**: Logs are stored permanently - ensure adequate storage
3. **Privacy**: Audit logs contain sensitive information - restrict access
4. **Compliance**: Use logs to demonstrate regulatory compliance
5. **Investigation**: Detailed logs help resolve disputes or issues

## 📊 System Monitoring and Maintenance

### Daily Administrative Tasks

#### Morning Checklist
- [ ] Review new shift signups from overnight
- [ ] Approve/reject pending signup requests
- [ ] Check for any system alerts or errors
- [ ] Verify upcoming shifts have adequate coverage

#### Weekly Maintenance
- [ ] Review audit logs for unusual activity
- [ ] Create next week's shifts
- [ ] Follow up on understaffed shifts
- [ ] Update user accounts as needed
- [ ] Check system performance and storage

### Shift Scheduling Best Practices

#### Planning Ahead
1. **Create shifts weekly**: Plan at least one week in advance
2. **Consider demand**: Popular shifts may need more slots
3. **Balance coverage**: Ensure adequate staffing across all periods
4. **Seasonal adjustment**: Modify schedules for busy periods

#### Managing Employee Requests
1. **First-come basis**: Approve signups in the order received
2. **Fairness**: Ensure equal opportunity across all employees
3. **Communication**: Provide feedback on rejected signups when possible
4. **Flexibility**: Consider employee preferences and constraints

### Troubleshooting Common Issues

#### Employees Can't Sign Up
**Possible Causes:**
- Shift is full (no available slots)
- Employee already signed up for this shift
- Shift has passed (can't sign up for past shifts)
- Employee account is inactive

**Solutions:**
1. Check shift slot availability
2. Verify employee hasn't already signed up
3. Confirm shift date is in the future
4. Check employee account status

#### Missing Shifts in Employee View
**Possible Causes:**
- Shift isn't marked as active
- No available slots remaining
- Shift date has passed
- Employee already has a signup for this shift

**Solutions:**
1. Verify shift status and availability
2. Check shift date and time settings
3. Review employee's existing signups
4. Ensure shift is properly configured

#### Calendar Integration Problems
**Common Issues:**
- Subscription URL not working
- Shifts not appearing in calendar apps
- Outdated shift information

**Solutions:**
1. Verify the calendar subscription URL is correct
2. Check that the backend calendar service is running
3. Test with .ics file download as alternative
4. Ensure user has approved shifts to display

## 🔧 Advanced Configuration

### Shift Templates

Default shift templates are pre-configured with these settings:

| Template | Start Time | End Time | Default Slots | Description |
|----------|------------|----------|---------------|-------------|
| Morning Shift | 6:00 AM | 2:00 PM | 3 | Early morning brewery operations |
| Afternoon Shift | 2:00 PM | 10:00 PM | 4 | Peak service hours |
| Evening Shift | 6:00 PM | 12:00 AM | 2 | Evening service and cleanup |
| Weekend Shift | 8:00 AM | 6:00 PM | 5 | Extended weekend coverage |

### System Limits and Guidelines

#### Shift Constraints
- **Maximum Duration**: 24 hours per shift
- **Minimum Duration**: 1 hour per shift
- **Maximum Slots**: 50 employees per shift
- **Advance Booking**: Up to 365 days in advance

#### User Account Limits
- **Maximum Users**: No system limit (database dependent)
- **Email Uniqueness**: Each user must have a unique email
- **Password Policy**: Minimum 6 characters (configurable)

### Security Considerations

#### Access Control
- **Admin Verification**: Regularly audit admin user list
- **Password Policies**: Enforce strong passwords
- **Session Management**: Users are logged out after inactivity
- **HTTPS Required**: Ensure secure connections in production

#### Data Protection
- **Password Encryption**: All passwords are hashed with BCrypt
- **JWT Tokens**: Stateless authentication with expiration
- **Audit Trails**: Complete activity logging for compliance
- **Data Backup**: Regular database backups recommended

## 🚨 Emergency Procedures

### Critical Issues

#### System Downtime
1. Check backend and frontend services
2. Verify database connectivity
3. Review system logs for errors
4. Contact technical support if needed
5. Communicate status to employees

#### Data Loss or Corruption
1. Stop all system access immediately
2. Contact technical support
3. Restore from most recent backup
4. Verify data integrity after restoration
5. Communicate recovery status

#### Security Breach
1. Change all administrative passwords immediately
2. Review audit logs for unauthorized access
3. Disable any compromised user accounts
4. Check for data modifications or theft
5. Document incident and response actions

### Contact Information

For technical support:
- **System Administrator**: [Contact details]
- **Database Issues**: [Contact details]
- **Security Incidents**: [Contact details]
- **General Support**: [Contact details]

## 📈 Tips for Success

### Efficient Administration
1. **Batch Operations**: Create multiple shifts at once using templates
2. **Regular Schedule**: Establish consistent shift creation patterns
3. **Employee Communication**: Keep staff informed about scheduling policies
4. **Documentation**: Maintain records of administrative decisions
5. **Training**: Ensure backup administrators know the system

### Employee Relations
1. **Fair Policies**: Apply signup rules consistently
2. **Timely Responses**: Approve/reject signups promptly
3. **Clear Communication**: Explain rejection reasons when possible
4. **Flexibility**: Consider special requests when feasible
5. **Recognition**: Acknowledge reliable employees

### System Optimization
1. **Regular Maintenance**: Perform weekly system checks
2. **Data Cleanup**: Archive old shifts and audit logs periodically
3. **Performance Monitoring**: Watch for slow response times
4. **User Feedback**: Listen to employee suggestions for improvements
5. **Stay Updated**: Keep informed about system updates and new features

---

*For additional support or questions, refer to the main README or contact your system administrator.*

*Last updated: January 26, 2026*
