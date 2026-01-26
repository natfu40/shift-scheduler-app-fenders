# 📋 Quick Reference Card

## 🚀 Application URLs
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080/api
- **Database**: localhost:5432

## 🔐 Default Login
- **Email**: `admin@example.com`
- **Password**: `admin123`
- **⚠️ CHANGE IMMEDIATELY AFTER FIRST LOGIN!**

## 🐳 Docker Commands
```bash
# Start application
docker-compose up -d --build

# Stop application
docker-compose down

# View logs
docker-compose logs

# Check status
docker-compose ps
```

## 👤 User Types & Capabilities

### Regular Employees
✅ View available shifts  
✅ Sign up for shifts  
✅ Cancel pending signups  
✅ Export shifts to calendar  
✅ View upcoming shifts  

### Administrators  
✅ All employee capabilities  
✅ Create/edit/delete shifts  
✅ Approve/reject signups  
✅ Create/manage users  
✅ View audit logs  
✅ System administration  

## 🧭 Navigation Menu

### All Users
- **🏠 Home**: Main dashboard
- **📅 Calendar**: Visual shift view
- **👤 Profile**: User information
- **🚪 Logout**: Sign out

### Admin Only
- **⚙️ Shift Management**: Shift admin tools
- **👥 Users**: User management
- **📋 Audits**: Activity logs

## ⚡ Quick Actions

### For Employees
| Action | Location | Button |
|--------|----------|--------|
| Sign up for shift | Home → Available Shifts | Green "Sign Up" |
| Cancel signup | Home → Pending Requests | Red "Cancel" |
| Add to calendar | Top of Home page | "📅 Add to Calendar" |
| Change password | Navigation → User Menu | "🔑 Change Password" |

### For Admins
| Action | Location | Button |
|--------|----------|--------|
| Create shift | Shift Management | Green "Create New Shift" |
| Edit shift | Shift Management | "Edit" (pencil icon) |
| Approve signup | Shift Management → View Signups | Green "Approve" |
| Create user | Users page | Green "Add New User" |

## 🔧 System Requirements

### Development
- Java 21+
- Node.js 16+
- Maven 3.6+
- PostgreSQL 15+

### Production
- 4GB+ RAM (8GB recommended)
- 10GB+ storage
- HTTPS capability
- Backup system

## 📞 Support Escalation

1. **User Issues** → Check User Guide
2. **Admin Questions** → Check Admin Guide  
3. **Setup Problems** → Check Installation Guide
4. **Technical Issues** → Contact System Administrator
5. **Bugs/Features** → Create Repository Issue

## 🔍 Common Troubleshooting

### Can't Login
- Verify email/password
- Check Caps Lock
- Try password reset (contact admin)

### No Available Shifts
- Check if shifts exist for future dates
- Verify you haven't already signed up
- Contact admin if shifts should be there

### Calendar Not Working
- Copy subscription URL carefully
- Try downloading .ics file instead
- Check calendar app settings

### Docker Issues
```bash
# Restart everything
docker-compose down
docker-compose up -d --build

# Check what's running
docker-compose ps

# View specific service logs
docker-compose logs backend
```

## 📊 File Structure
```
docs/
├── README.md                    # Main overview
├── USER_GUIDE.md               # Employee instructions
├── ADMIN_GUIDE.md              # Administrator manual
├── INSTALLATION.md             # Setup instructions
├── RAILWAY_DEPLOYMENT.md       # Railway cloud deployment
├── POSTGRESQL_SETUP.md         # PostgreSQL installation
├── POSTGRESQL_MIGRATION.md     # Migration from MariaDB
├── PASSWORD_CHANGE_FEATURE.md  # Password management feature
└── QUICK_REFERENCE.md          # This file
```

## ✅ Verification

### Quick PostgreSQL Test
```bash
# Windows
verify-postgresql.bat

# Linux/macOS  
./verify-postgresql.sh

# Manual verification
docker-compose up -d postgres
docker-compose exec postgres psql -U postgres -d shift_scheduler -c "SELECT version();"
```

## 🛡️ Security Checklist

✅ Change default admin password  
✅ Use HTTPS in production  
✅ Secure database credentials  
✅ Generate strong JWT secret  
✅ Enable audit logging  
✅ Regular backups  
✅ Monitor failed logins  
✅ Keep system updated  

---

*For detailed information, see the specific guides in the docs folder.*
