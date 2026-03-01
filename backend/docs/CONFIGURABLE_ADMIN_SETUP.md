# ✅ Configurable Admin User Setup - Complete

## What's Now Configurable ✅

### 1. Admin Email Address
- **Environment Variable:** `ADMIN_EMAIL`
- **Default:** `admin@example.com` (if not set)
- **Purpose:** Customize the admin user's email address

### 2. Admin Password  
- **Environment Variable:** `ADMIN_PASSWORD` (required)
- **Default:** None (must be set)
- **Purpose:** Set secure admin password (no hardcoded passwords)

### 3. Security Features
- ✅ **One-time creation only** (won't recreate existing admin)
- ✅ **Forces password change** on first login
- ✅ **Environment variable based** (no hardcoded values)
- ✅ **Memory cleanup** after password processing

## 🚀 Railway Environment Variables

Set these in Railway Dashboard → Your Service → Variables:

### Required Variable
```bash
ADMIN_PASSWORD=YourSecureAdminPassword123!
```

### Optional Variables  
```bash
ADMIN_EMAIL=yourname@yourdomain.com    # Optional: defaults to admin@example.com
JWT_SECRET=your-512-bit-jwt-secret     # Already set
SPRING_PROFILES_ACTIVE=railway         # Already set
```

## 📋 Configuration Examples

### Example 1: Default Setup (Minimal)
```bash
ADMIN_PASSWORD=MySecurePassword123!
# Admin will be created with: admin@example.com
```

### Example 2: Custom Email
```bash
ADMIN_PASSWORD=MySecurePassword123!
ADMIN_EMAIL=nate@mycompany.com
# Admin will be created with: nate@mycompany.com
```

### Example 3: Production Setup
```bash
ADMIN_PASSWORD=Prod-SecurePassword-2026!
ADMIN_EMAIL=admin@mycompany.com
JWT_SECRET=production-jwt-secret-512-bits...
SPRING_PROFILES_ACTIVE=railway
```

## 🔍 Verification After Deployment

### Check Admin Creation (Public Endpoint)
```bash
curl https://shift-scheduler-backend-production-9d3a.up.railway.app/api/secure-admin/check-admin-exists
```

**Response Example:**
```json
{
  "adminExists": true,
  "adminEmail": "nate@mycompany.com",
  "message": "Admin user exists with email: nate@mycompany.com"
}
```

### Check Railway Logs
Look for these log messages:
```
Creating initial admin user (one-time only)...
Admin email: nate@mycompany.com
✅ Initial admin user created successfully with email: nate@mycompany.com
⚠️ Admin must change password on first login for security
```

## 🔑 Login Process

### Step 1: Calculate SHA256 Hash
```bash
# Replace with your actual ADMIN_PASSWORD
echo -n "MySecurePassword123!" | sha256sum
# Output: a1b2c3d4e5f6... (copy this hash)
```

### Step 2: Login with Hash
```bash
curl -X POST https://shift-scheduler-backend-production-9d3a.up.railway.app/api/auth/login-hashed \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nate@mycompany.com",
    "password": "YOUR_SHA256_HASH_HERE"
  }'
```

**Success Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "userId": 1,
  "email": "nate@mycompany.com",
  "firstName": "Admin",
  "lastName": "User",
  "firstTimeLogin": true
}
```

## 🛡️ Security Benefits

### Environment Variables Only
- ✅ **No hardcoded credentials** in source code
- ✅ **Different values per environment** (dev/staging/prod)
- ✅ **Secure Railway variable storage**
- ✅ **Version control safe** (no secrets in Git)

### One-Time Creation
- ✅ **Creates admin only once** (first deployment)
- ✅ **Won't overwrite existing** admin users
- ✅ **Production deployment safe**
- ✅ **No accidental password resets**

### Forced Security
- ✅ **Must change password** on first login (`firstTimeLogin: true`)
- ✅ **SHA256 + BCrypt** double hashing
- ✅ **Memory cleanup** after processing
- ✅ **Audit logging** of admin creation

## 🚀 Deployment Steps

### 1. Set Environment Variables
In Railway Dashboard:
```bash
ADMIN_PASSWORD=YourSecurePassword123!
ADMIN_EMAIL=your-email@domain.com  # Optional
```

### 2. Deploy Changes
```bash
git add .
git commit -m "Add configurable admin email via ADMIN_EMAIL environment variable"
git push
```

### 3. Verify Deployment
```bash
# Check if admin was created
curl https://shift-scheduler-backend-production-9d3a.up.railway.app/api/secure-admin/check-admin-exists

# Check Railway logs for creation messages
```

### 4. First Login
```bash
# Calculate SHA256 of your ADMIN_PASSWORD
echo -n "YourSecurePassword123!" | sha256sum

# Login with the hash
curl -X POST .../api/auth/login-hashed ...
```

## 📝 Configuration Summary

| Variable | Required | Default | Purpose |
|----------|----------|---------|---------|
| `ADMIN_PASSWORD` | ✅ Yes | None | Admin user password |
| `ADMIN_EMAIL` | No | `admin@example.com` | Admin user email |
| `JWT_SECRET` | ✅ Yes | None | JWT signing key |
| `SPRING_PROFILES_ACTIVE` | ✅ Yes | None | Activate railway profile |

## Files Modified ✅

1. ✅ **DataInitializer.java** - Added `ADMIN_EMAIL` environment variable support
2. ✅ **SecureAdminController.java** - Updated to use configurable admin email
3. ✅ **SecurityConfig.java** - Already configured for secure admin endpoints

---

## 🎉 Your admin user is now fully configurable and secure!

Set your environment variables and deploy. You can now use any email address and password for your admin user, with no hardcoded values in your source code.
