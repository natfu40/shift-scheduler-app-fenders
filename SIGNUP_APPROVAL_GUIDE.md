# Shift Signup Approval Process

## How Signup Works (Important!)

### Step 1: Employee Signs Up
- Employee clicks "Sign Up" for a shift
- Signup is created in database with status: **PENDING** (accepted = false)
- The shift's `filledSlots` counter does **NOT** increase yet

### Step 2: Admin Reviews Signup
- Admin goes to Admin Dashboard
- Clicks "View Signups" on the shift
- Sees ALL signups (both pending and accepted)
- Signups show with status badge:
  - 🟡 **Yellow "Pending"** = Waiting for admin approval
  - 🟢 **Green "Accepted"** = Admin approved

### Step 3: Admin Approves or Rejects
- Admin can **Accept** the signup:
  - Click "Accept" button (future feature)
  - Signup status changes to "Accepted"
  - `filledSlots` counter increases by 1
  - Shift slot is now "reserved"
  
- OR Admin can **Reject** the signup:
  - Click "Reject" button (future feature)
  - Signup is deleted
  - Employee must sign up again if they want to

---

## Why Admin Approval?

This design allows:
- ✅ Admins to review who signed up
- ✅ Prevent unauthorized signups
- ✅ Manage actual attendance vs. just interest
- ✅ Control shift capacity
- ✅ Contact employees before shift

---

## Viewing Signups in Admin Dashboard

### How to see signups:
1. Go to http://localhost:3000/admin
2. Click "View Signups" button on a shift
3. Modal opens showing:
   - **Shift Details**: Times, slots, filled count
   - **Employees List**: All who signed up (accepted & pending)
   - **Status Column**: Shows approval status

### What you see:

```
Employees Signed Up:
┌─────────────┬──────────┬─────────────────┬──────────┐
│ Email       │ Name     │ Signed Up       │ Status   │
├─────────────┼──────────┼─────────────────┼──────────┤
│ john@ex.com │ John Doe │ Jan 12, 10:30   │ Pending  │
│ jane@ex.com │ Jane Doe │ Jan 12, 10:45   │ Accepted │
└─────────────┴──────────┴─────────────────┴──────────┘
```

---

## Current Behavior

### Signup Created (Before Approval)
```
Status: PENDING (Yellow badge)
filledSlots: Still 3/5
Assignment Record: EXISTS in database
Employee can see: In "My Shifts" (pending approval)
Admin can see: In "View Signups" modal
```

### Signup Approved (After Admin Accepts)
```
Status: ACCEPTED (Green badge)
filledSlots: Now 4/5 (increased by 1)
Assignment Record: Updated with acceptedAt timestamp
Employee can see: In "My Shifts" (confirmed)
Admin can see: In "View Signups" (marked accepted)
```

### Signup Rejected (Admin Rejects)
```
Status: DELETED
filledSlots: No change
Assignment Record: REMOVED from database
Employee can see: Gone from "My Shifts"
Admin can see: No longer in "View Signups"
```

---

## Why Signups Might Appear Empty

### Reason 1: The Page Isn't Reloading
- Sign up as employee
- Don't refresh admin dashboard
- Admin dashboard was loaded before signup happened
- **Solution**: Refresh the page or click "View Signups" again

### Reason 2: API Error
- Signup failed silently
- Employee got error but signup wasn't created
- **Solution**: Check browser console for errors

### Reason 3: Wrong Shift
- Signed up for a different shift
- Looking at "View Signups" on wrong shift
- **Solution**: Double-check which shift you're viewing

---

## Testing the Signup Flow

### Complete Test:

1. **Setup**
   - Login as admin
   - Create a shift: "Test Shift" - now
   - Logout

2. **Employee Signs Up**
   - Create new account: testuser@example.com / password123
   - Login with that account
   - Go to Dashboard
   - Find "Test Shift"
   - Click "Sign Up"
   - See success message

3. **Admin Reviews**
   - Logout
   - Login as admin@example.com / admin123
   - Go to Admin Dashboard
   - Click "Manage Shifts"
   - Find "Test Shift"
   - Click "View Signups"
   - **SHOULD SEE**: testuser@example.com with "Pending" status

4. **Admin Approves** (Future feature - code is ready)
   - In "View Signups" modal
   - Click "Accept" button next to testuser
   - Status changes to "Accepted"
   - filledSlots increases
   - You'll see "Jan 12, 10:45 at Accepted" or similar

---

## Troubleshooting

### "View Signups" shows no one even though employee signed up

**Steps to debug:**

1. **Check if signup actually happened:**
   - Employee should see shift in "My Shifts" section
   - If not there, signup didn't work

2. **Check browser console for errors:**
   - Open browser DevTools (F12)
   - Check Console tab for red error messages
   - Report any errors

3. **Try refreshing:**
   - Refresh admin dashboard page
   - Click "View Signups" again
   - Data should load fresh from server

4. **Check database:**
   ```sql
   SELECT * FROM shift_assignments WHERE shift_id = 1;
   ```
   - If results empty, signup wasn't created
   - If results exist, query is working, UI issue

---

## Database Tables

### Shift
```
id | name | availableSlots | filledSlots | ...
```

### ShiftAssignment
```
id | user_id | shift_id | accepted | signedUpAt | acceptedAt
1  |    2    |    1     |  false   | 2026-01-12 | NULL (pending)
2  |    3    |    1     |  true    | 2026-01-12 | 2026-01-12 (accepted)
```

**Note**: `filledSlots` only counts assignments with `accepted = true`

---

## Future Enhancements

Coming soon (code is ready, UI buttons not yet visible):
- ✅ Accept button in modal
- ✅ Reject button in modal
- ✅ Bulk approve/reject
- ✅ Email notifications
- ✅ Automatic acceptance after X hours
- ✅ Status filtering (show only pending, only accepted, etc.)

---

## API Endpoints Reference

### Employee Signs Up
```
POST /api/shift-assignments/signup/{shiftId}
Authorization: Required
Returns: ShiftAssignmentDTO with accepted=false
```

### Admin Accepts Signup
```
PUT /api/shift-assignments/{assignmentId}/accept
Authorization: Required (ADMIN role)
Returns: ShiftAssignmentDTO with accepted=true, acceptedAt=NOW
Action: Increases shift.filledSlots by 1
```

### Admin Rejects Signup
```
DELETE /api/shift-assignments/{assignmentId}/reject
Authorization: Required (ADMIN role)
Action: Deletes the assignment record
```

### Get All Signups for a Shift
```
GET /api/shifts/{shiftId}/details
Authorization: Required (ADMIN role)
Returns: ShiftDetailDTO with all signups in array
```

---

**Understanding**: Signup is a 2-step process - Employee creates it, Admin approves it!

