# 🚀 Quick Reference Card

## Login Credentials

```
👤 Default Admin Account
Email:    admin@example.com
Password: admin123
URL:      http://localhost:3000/login
```

⚠️ **Change this password immediately in production!**

---

## Top 5 Things You Can Do Now

### 1. Login as Admin
- Go to: http://localhost:3000/login
- Enter: admin@example.com / admin123
- Click: Login

### 2. Create a Shift
- Click: "Admin" in navbar
- Click: "Create New Shift"
- Fill in: Name, Times, Slots
- Click: "Create Shift"

### 3. View Available Shifts
- Go to: http://localhost:3000/ (Employee Dashboard)
- See all shifts
- Click shift to see details

### 4. Sign Up for a Shift
- Create a new account via signup page
- Go to Dashboard
- Find a shift
- Click "Sign Up"

### 5. Monitor Activity
- Admin Dashboard → "Audit Logs" tab
- See who did what when

---

## API Quick Commands

### Test Admin Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"admin123"}'
```

### Create a Shift (Replace TOKEN with JWT from login response)
```bash
curl -X POST http://localhost:8080/api/shifts \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name":"Morning Shift",
    "description":"7 AM - 3 PM",
    "startTime":"2026-01-20T07:00:00",
    "endTime":"2026-01-20T15:00:00",
    "availableSlots":5
  }'
```

### Get Available Shifts (No auth needed)
```bash
curl http://localhost:8080/api/shifts/available
```

---

## URLs Reference

| Purpose | URL |
|---------|-----|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080/api |
| Login | http://localhost:3000/login |
| Signup | http://localhost:3000/signup |
| Dashboard | http://localhost:3000/ |
| Admin Panel | http://localhost:3000/admin |
| Database | localhost:3306 (MySQL) |

---

## Database Access

```bash
# Access MySQL
docker-compose exec mariadb mysql -u root -proot shift_scheduler

# Example: Create another admin
INSERT INTO users (email, password, first_name, last_name, active, created_at, updated_at)
VALUES ('admin2@example.com', '$2a$10$...', 'Admin', 'Two', true, NOW(), NOW());

INSERT INTO user_roles (user_id, role_id, assigned_at)
VALUES (2, 1, NOW());
```

See: `DATABASE_ADMIN_GUIDE.md` for more commands

---

## Docker Commands

```bash
# Start containers
docker-compose up -d

# Stop containers
docker-compose down

# Rebuild and start
docker-compose up -d --build

# View logs
docker-compose logs -f backend
docker-compose logs -f frontend

# Access database
docker-compose exec mariadb mysql -u root -proot shift_scheduler
```

---

## File Locations

### Documentation
- `ADMIN_GUIDE.md` - Complete admin guide
- `QUICKSTART_ADMIN.md` - Quick start guide
- `DATABASE_ADMIN_GUIDE.md` - Database admin commands
- `ADMIN_IMPLEMENTATION_SUMMARY.md` - Technical details

### Source Code
- Backend: `backend/src/main/java/com/shiftscheduler/`
- Frontend: `frontend/src/`
- Database: `docker-compose.yml`

---

## 🎯 Common Tasks & Solutions

### Change Admin Password
1. Login to database: `docker-compose exec mariadb mysql -u root -proot shift_scheduler`
2. Run SQL from `DATABASE_ADMIN_GUIDE.md` section "Password Management"

### Promote User to Admin
1. Get user ID from database
2. Run: `INSERT INTO user_roles VALUES (user_id, admin_role_id, NOW());`
3. See `DATABASE_ADMIN_GUIDE.md` for full SQL

### Create Test Data
1. Create account via signup page
2. Login as admin
3. Create shifts
4. Logout and login with test account
5. Sign up for shifts

### View Audit Logs
1. Login as admin
2. Go to Admin Dashboard
3. Click "Audit Logs" tab
4. Or query: `SELECT * FROM audit_logs ORDER BY action_at DESC;`

### Reset Database
```bash
docker-compose down -v
docker-compose up -d --build
# Database will be recreated and repopulated with default admin
```

---

## ⚠️ Important Security Notes

| Item | Action | Urgency |
|------|--------|---------|
| Default Password | Change from `admin123` | 🔴 HIGH |
| JWT Secret | Change to random 32+ chars | 🔴 HIGH |
| HTTP → HTTPS | Use HTTPS in production | 🟠 MEDIUM |
| Database Access | Restrict to localhost | 🟠 MEDIUM |
| Backups | Set up regular backups | 🟠 MEDIUM |

---

## Status Indicators

✅ = Working
⚠️ = Needs attention
❌ = Not implemented

| Feature | Status | Notes |
|---------|--------|-------|
| Admin Login | ✅ | Works |
| Create Shifts | ✅ | Admin only |
| Employee Signup | ✅ | Works |
| Audit Logs | ✅ | Complete history |
| Role Management | ✅ | Via API or DB |
| Password Change UI | ⚠️ | Can use DB |
| Email Notifications | ❌ | Future feature |
| Two-Factor Auth | ❌ | Future feature |

---

## Emergency Contacts

If something breaks:
1. Check Docker logs: `docker-compose logs backend`
2. Read: `ADMIN_GUIDE.md` → Troubleshooting section
3. Reset database: `docker-compose down -v && docker-compose up -d --build`
4. Access DB directly if UI is broken: `docker-compose exec mariadb ...`

---

## Version Info

- **Java**: 21
- **Spring Boot**: 3.2.1
- **React**: 18
- **MariaDB**: 10.6
- **Docker Compose**: Latest

---

**🎉 You're all set!**

Start with the Admin Dashboard and create some shifts.
Then test the employee signup flow.

Questions? See the documentation files listed above.

---

**Last Updated**: January 12, 2026
**Implementation**: Complete ✅

