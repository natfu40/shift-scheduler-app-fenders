# Database Administration Guide

## Connecting to the Database

### Via Docker
```bash
docker-compose exec mariadb mysql -u root -p shift_scheduler
```
Password: `root` (default)

### Via MySQL Client
```bash
mysql -h localhost -u root -p shift_scheduler
```

## Creating Admin Users Manually

### Method 1: Create New Admin User (SQL)

```sql
-- Step 1: Create the user
INSERT INTO users (email, password, first_name, last_name, active, created_at, updated_at)
VALUES (
  'nenadmin@example.com',
  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36gBS43e', -- "password"
  'New',
  'Admin',
  true,
  NOW(),
  NOW()
);

-- Step 2: Get the user ID (note the ID for next step)
SELECT id FROM users WHERE email = 'nenadmin@example.com';

-- Step 3: Ensure ADMIN role exists (it should, auto-created)
INSERT INTO roles (name, description, created_at) 
VALUES ('ADMIN', 'Administrator role with full access', NOW())
ON DUPLICATE KEY UPDATE id=id;

-- Step 4: Assign ADMIN role to the new user (replace USER_ID with actual ID from step 2)
INSERT INTO user_roles (user_id, role_id, assigned_at)
SELECT USER_ID, id, NOW()
FROM roles
WHERE name = 'ADMIN';
```

### Method 2: Promote Existing User to Admin

```sql
-- Step 1: Find the user ID
SELECT id FROM users WHERE email = 'user@example.com';

-- Step 2: Ensure ADMIN role exists
INSERT INTO roles (name, description, created_at)
VALUES ('ADMIN', 'Administrator role with full access', NOW())
ON DUPLICATE KEY UPDATE id=id;

-- Step 3: Assign ADMIN role (replace USER_ID with actual ID)
INSERT INTO user_roles (user_id, role_id, assigned_at)
SELECT USER_ID, id, NOW()
FROM roles
WHERE name = 'ADMIN'
AND USER_ID NOT IN (
  SELECT user_id FROM user_roles 
  WHERE role_id = (SELECT id FROM roles WHERE name = 'ADMIN')
);
```

## Viewing Admin Users

### List All Admin Users
```sql
SELECT u.id, u.email, u.first_name, u.last_name, r.name as role
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
WHERE r.name = 'ADMIN'
ORDER BY u.created_at;
```

### List All Users and Their Roles
```sql
SELECT u.id, u.email, u.first_name, u.last_name, 
       GROUP_CONCAT(r.name) as roles
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
GROUP BY u.id
ORDER BY u.email;
```

### Check If User Has Admin Role
```sql
SELECT u.email, 
       CASE 
         WHEN ur.id IS NOT NULL THEN 'YES'
         ELSE 'NO'
       END as is_admin
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id AND ur.role_id = (SELECT id FROM roles WHERE name = 'ADMIN')
WHERE u.email = 'admin@example.com';
```

## Removing Admin Role

### Remove Admin from User
```sql
DELETE FROM user_roles
WHERE user_id = (SELECT id FROM users WHERE email = 'user@example.com')
AND role_id = (SELECT id FROM roles WHERE name = 'ADMIN');
```

## Password Management

### Update User Password

First, generate a bcrypt hash. Using a bcrypt tool or command:

```bash
# Using htpasswd (Apache utility)
htpasswd -nbBC 10 "" your_password | tr -d ':\n' | sed 's/\$2y/\$2a/'

# Using Python
python3 -c "import bcrypt; print(bcrypt.hashpw(b'your_password', bcrypt.gensalt(10)).decode())"
```

Then update the database:

```sql
UPDATE users 
SET password = '$2a$10$...' -- paste the bcrypt hash here
WHERE email = 'user@example.com';
```

### Reset Default Admin Password
```sql
UPDATE users 
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36gBS43e' -- "password"
WHERE email = 'admin@example.com';
```

## Viewing Shift Information

### List All Shifts
```sql
SELECT id, name, description, start_time, end_time, available_slots, active, created_by, created_at
FROM shifts
ORDER BY start_time DESC;
```

### List Active Shifts
```sql
SELECT id, name, start_time, end_time, available_slots
FROM shifts
WHERE active = true AND start_time > NOW()
ORDER BY start_time;
```

### View Shift Assignments (Signups)
```sql
SELECT 
  s.id as shift_id,
  s.name as shift_name,
  u.email as employee_email,
  sa.status,
  sa.assigned_at
FROM shift_assignments sa
JOIN shifts s ON sa.shift_id = s.id
JOIN users u ON sa.user_id = u.id
ORDER BY s.start_time, u.email;
```

## Audit Log Queries

### View Recent Audit Logs
```sql
SELECT id, user_id, action, entity, entity_id, description, ip_address, action_at
FROM audit_logs
ORDER BY action_at DESC
LIMIT 100;
```

### View Actions by Specific User
```sql
SELECT action, entity, description, action_at
FROM audit_logs
WHERE user_id = (SELECT id FROM users WHERE email = 'admin@example.com')
ORDER BY action_at DESC;
```

### View Admin Actions Only
```sql
SELECT 
  al.id,
  u.email as user_email,
  al.action,
  al.entity,
  al.description,
  al.action_at
FROM audit_logs al
JOIN users u ON al.user_id = u.id
WHERE u.id IN (SELECT user_id FROM user_roles WHERE role_id = (SELECT id FROM roles WHERE name = 'ADMIN'))
ORDER BY al.action_at DESC
LIMIT 50;
```

## Backup and Maintenance

### Backup Database
```bash
# Docker backup
docker-compose exec mariadb mysqldump -u root -p shift_scheduler > backup.sql

# Or without password prompt (if using environment variable)
docker-compose exec mariadb mysqldump -u root -proot shift_scheduler > backup.sql
```

### Restore from Backup
```bash
docker-compose exec -T mariadb mysql -u root -proot shift_scheduler < backup.sql
```

## Common Tasks

### Give User Admin Access in 3 Steps

```bash
# 1. Login to MySQL
docker-compose exec mariadb mysql -u root -proot shift_scheduler

# 2. Run this command (replace with actual user's ID)
INSERT INTO user_roles (user_id, role_id, assigned_at)
VALUES (3, (SELECT id FROM roles WHERE name = 'ADMIN'), NOW());

# 3. User now has admin access after next login
```

### Count Active Users
```sql
SELECT COUNT(*) as active_users FROM users WHERE active = true;
```

### Find Inactive Admins
```sql
SELECT u.email, u.last_name, u.updated_at
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
WHERE ur.role_id = (SELECT id FROM roles WHERE name = 'ADMIN')
AND u.active = false;
```

## Safety Tips

⚠️ **Important:**
1. Always backup before making bulk changes
2. Test queries on a development database first
3. Use WHERE clauses carefully - check affected rows before deleting
4. Keep audit logs for compliance
5. Log all admin password changes
6. Regularly review audit logs for suspicious activity

## Troubleshooting

### User Can't Login After Password Change
- Verify bcrypt hash format is correct (starts with `$2a$`)
- Check user is marked as `active = true`
- Verify email matches exactly (case-sensitive in some systems)

### User Has ADMIN role but Can't Create Shifts
- Verify role name is exactly "ADMIN" (case-sensitive)
- Check user_roles table has entry for user
- Verify roles table has ADMIN role
- User needs to logout and login again to get new role

### Audit Logs Are Empty
- Check if `logging.level.com.shiftscheduler=DEBUG` is set in properties
- Verify AuditLogService is being called
- Check database size limits

---

**Last Updated**: January 2026

