# 🍺 Fenders Brewing Scheduler

A comprehensive web-based shift scheduling application designed for Fenders Brewing Company. This application allows employees to view and sign up for available shifts while providing administrators with powerful tools to manage shifts, users, and track all activities through comprehensive audit logs.

## 📋 Overview

The Fenders Brewing Scheduler is a modern, secure web application built with React (frontend) and Spring Boot (backend). It provides role-based access control with distinct experiences for regular employees and administrators.

### Key Features

- **Shift Management**: Create, edit, and delete shifts with detailed scheduling
- **User Management**: Admin-controlled user creation with secure first-time login flow
- **Role-Based Access**: Separate interfaces for employees and administrators
- **Calendar Integration**: Export shifts to personal calendars (iCal format)
- **Audit Logging**: Complete activity tracking for compliance and monitoring
- **Responsive Design**: Works seamlessly on desktop and mobile devices
- **Secure Authentication**: JWT-based authentication with password encryption

## 🎯 User Types

### Regular Employees
- View available shifts
- Sign up for shifts
- Manage their own shift assignments
- Export personal shifts to calendar applications
- Track pending signup requests

### Administrators  
- All employee capabilities plus:
- Create and manage shifts
- Create and manage user accounts
- Approve/reject employee shift signups
- View comprehensive audit logs
- Manage user roles and permissions

## 📚 Documentation

Choose your documentation based on your role:

- **[Employee User Guide](./USER_GUIDE.md)** - Complete guide for regular employees
- **[Administrator Guide](./ADMIN_GUIDE.md)** - Comprehensive admin documentation
- **[Installation Guide](./INSTALLATION.md)** - Setup and deployment instructions
- **[Railway Deployment Guide](./RAILWAY_DEPLOYMENT.md)** - Quick cloud deployment with Railway
- **[PostgreSQL Setup Guide](./POSTGRESQL_SETUP.md)** - Local PostgreSQL installation
- **[PostgreSQL Migration Guide](./POSTGRESQL_MIGRATION.md)** - Migration from MariaDB details
- **[Password Change Feature](./PASSWORD_CHANGE_FEATURE.md)** - User password management

## 🚀 Quick Start

1. **For Users**: See the [Employee User Guide](./USER_GUIDE.md)
2. **For Admins**: See the [Administrator Guide](./ADMIN_GUIDE.md)  
3. **For Setup**: See the [Installation Guide](./INSTALLATION.md)
4. **For Cloud Deployment**: See the [Railway Deployment Guide](./RAILWAY_DEPLOYMENT.md)
5. **For PostgreSQL Setup**: See the [PostgreSQL Setup Guide](./POSTGRESQL_SETUP.md)

## 🔐 Default Login

After initial setup, use these credentials to access the system:

- **Email**: `admin@example.com`
- **Password**: `admin123`

**⚠️ Important**: Change the default admin password immediately after first login!

## 🏗️ Architecture

### Frontend (React)
- Modern React 18 with functional components and hooks
- Bootstrap 5 for responsive UI design
- Zustand for state management
- React Router for navigation
- Axios for API communication

### Backend (Spring Boot)
- Spring Boot 3.x with Java 21
- Spring Security for authentication and authorization
- Spring Data JPA for database operations
- PostgreSQL for data persistence
- JWT tokens for stateless authentication

### Database Schema
- **Users**: Employee information and authentication
- **Roles**: User role definitions (EMPLOYEE, ADMIN)
- **Shifts**: Shift details and scheduling information
- **Shift Assignments**: Employee shift signups and approvals
- **Audit Logs**: Complete activity tracking

## 📱 Features by Role

### All Users
- Secure login with JWT authentication
- Responsive mobile-friendly interface
- Personal shift calendar export
- Real-time shift availability updates

### Employee Features
- Browse available shifts
- Sign up for shifts with instant confirmation
- View upcoming approved shifts
- Cancel pending signup requests
- Export personal schedule to calendar apps

### Admin Features
- Complete shift lifecycle management
- User account creation and management
- Shift approval/rejection workflow
- Comprehensive audit trail viewing
- System monitoring and reporting

## 🔧 Technical Requirements

- **Browser**: Modern web browser (Chrome, Firefox, Safari, Edge)
- **Server**: Java 21+, PostgreSQL 15+
- **Development**: Node.js 16+, Maven 3.6+

## 📞 Support

For technical support or questions:
1. Check the appropriate user guide for your role
2. Review the installation guide for setup issues
3. Create an issue in the repository for bugs or feature requests

## 📄 License

This project is licensed under the MIT License - see the main repository for details.

---

*Last updated: January 26, 2026*
