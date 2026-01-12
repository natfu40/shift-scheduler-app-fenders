# Shift Edit & Signup Management Guide

## New Features

### 1. Edit Existing Shifts

Admin users can now edit shifts that have already been created:

#### How to Edit a Shift:

1. Go to Admin Dashboard (http://localhost:3000/admin)
2. Make sure you're on the "Manage Shifts" tab
3. Find the shift you want to edit in the table
4. Click the **"Edit"** button (yellow button)
5. A modal will open with the shift details
6. Modify any of the following fields:
   - **Shift Name** - The display name for the shift
   - **Description** - Additional details about the shift
   - **Start Time** - When the shift begins (date & time)
   - **End Time** - When the shift ends (date & time)
   - **Available Slots** - Number of employees needed
7. Click **"Update Shift"** to save changes
8. You'll see a success message and the table will refresh

**Note:** Changes are immediately visible to employees on their dashboard.

---

### 2. View Employee Signups

Admin users can now see exactly who has signed up for each shift:

#### How to View Signups:

1. Go to Admin Dashboard (http://localhost:3000/admin)
2. Make sure you're on the "Manage Shifts" tab
3. Find the shift you want to view in the table
4. Click the **"View Signups"** button (blue button)
5. A modal will open showing:
   - **Shift Details**
     - Start time
     - End time
     - Available slots
     - Filled slots (number of signups)
   - **Employees Signed Up** table with:
     - Employee email
     - Employee name (first and last)
     - When they signed up (timestamp)
     - Status (Accepted/Pending)

#### Signup Status:

- **Accepted (Green Badge)**: Employee's signup has been confirmed/accepted
- **Pending (Yellow Badge)**: Employee has signed up but status is pending approval

---

## Admin Dashboard Layout

```
┌─────────────────────────────────────────────────────────┐
│  Admin Dashboard                                         │
├─────────────────────────────────────────────────────────┤
│  [Manage Shifts] [Audit Logs]                           │
│  [Create New Shift]                                     │
├─────────────────────────────────────────────────────────┤
│  Shifts Table:                                          │
│  ┌──────────────────────────────────────────────────┐  │
│  │ Name | Start | End | Slots | Filled | Actions  │  │
│  ├──────────────────────────────────────────────────┤  │
│  │ Shift1 | ... | ... | 5 | 3 |[Signups][Edit][X]│  │
│  │ Shift2 | ... | ... | 3 | 2 |[Signups][Edit][X]│  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

---

## Shift Actions Available

For each shift, admins can:

| Action | Button | Color | Purpose |
|--------|--------|-------|---------|
| **View Signups** | Button | Blue | See who signed up for the shift |
| **Edit** | Button | Yellow | Modify shift details |
| **Delete** | Button | Red | Remove the shift from system |

---

## Backend API Endpoints

### Get Shift Details with Signups

```
GET /api/shifts/{shiftId}/details
Authorization: Bearer {admin_token}

Response:
{
  "id": 1,
  "name": "Morning Shift",
  "description": "7 AM - 3 PM",
  "startTime": "2026-01-20T07:00:00",
  "endTime": "2026-01-20T15:00:00",
  "availableSlots": 5,
  "filledSlots": 3,
  "active": true,
  "createdById": 1,
  "createdByName": "Admin User",
  "signups": [
    {
      "id": 1,
      "userId": 2,
      "userEmail": "employee1@example.com",
      "userFirstName": "John",
      "userLastName": "Doe",
      "shiftId": 1,
      "shiftName": "Morning Shift",
      "accepted": true,
      "signedUpAt": "2026-01-15T10:30:00",
      "acceptedAt": "2026-01-15T10:35:00"
    },
    // ... more signups
  ]
}
```

### Update Shift

```
PUT /api/shifts/{shiftId}
Authorization: Bearer {admin_token}
Content-Type: application/json

Request Body:
{
  "name": "Updated Shift Name",
  "description": "Updated description",
  "startTime": "2026-01-20T08:00:00",
  "endTime": "2026-01-20T16:00:00",
  "availableSlots": 6,
  "active": true
}

Response:
{
  "id": 1,
  "name": "Updated Shift Name",
  ...
}
```

---

## Common Admin Tasks

### Task: Update Shift Time
1. Click "Edit" on the shift
2. Update the start/end time
3. Click "Update Shift"
4. Employees will see the new time on their dashboard

### Task: Increase Available Slots
1. Click "Edit" on the shift
2. Change "Available Slots" to higher number
3. Click "Update Shift"
4. More employees can now sign up

### Task: Check Shift Capacity
1. Click "View Signups" on the shift
2. Compare "Available Slots" with "Filled Slots"
3. You'll see the exact count and names of all employees

### Task: Cancel a Shift
1. Click "Edit" on the shift
2. You can either:
   - Delete the shift completely (click Delete button)
   - Or keep it but mark as inactive (future feature)

---

## Data Displayed for Each Signup

When viewing signups, you see:

```
Employee Email: john.doe@company.com
Employee Name: John Doe
Signed Up: Jan 15, 2026 10:30
Status: Accepted ✓
```

This allows you to:
- See contact information for employees working the shift
- Know exactly who confirmed
- Track when they signed up
- Manage shift coverage

---

## Tips & Best Practices

### Before a Shift:
1. **Check Signups**: Review who's working
2. **Verify Coverage**: Make sure you have enough staff
3. **Prepare**: Contact employees if needed

### After a Shift:
1. **Mark as Complete**: Change status if needed (future feature)
2. **Review Attendance**: Check who actually showed up
3. **Log Issues**: Use audit logs if there were problems

### Managing Changes:
- **Small changes**: Use Edit function
- **Big changes**: Delete and create new shift
- **Recurring shifts**: Create new one for each occurrence

---

## Troubleshooting

**Q: Can't see signups button**
- A: You need ADMIN role to view signups

**Q: Signups list is empty**
- A: No employees have signed up for that shift yet
- Check if shift time is in the future
- Check if employees can see the shift

**Q: Edit button doesn't work**
- A: Make sure you're logged in as admin
- Try refreshing the page

**Q: Changed a shift but employees don't see it**
- A: Employees might have cached data
- Tell them to refresh the page

---

## Database Structure

### Shifts Table
```sql
shifts (
  id,
  name,
  description,
  startTime,
  endTime,
  availableSlots,
  filledSlots,
  active,
  createdBy (user_id),
  createdAt,
  updatedAt
)
```

### Shift Assignments Table
```sql
shift_assignments (
  id,
  user_id,
  shift_id,
  accepted,
  signedUpAt,
  acceptedAt
)
```

---

## Feature Comparison

### Before (Original)
- ✅ Create shifts
- ❌ Edit shifts
- ❌ View signups
- ❌ See employee details

### After (Updated)
- ✅ Create shifts
- ✅ Edit shifts
- ✅ View signups
- ✅ See employee email and names
- ✅ Track signup timestamps
- ✅ See acceptance status

---

## Security & Permissions

- **Admin Only**: All shift edit and view signups features require ADMIN role
- **Public Read**: Employees can view available shifts
- **Protected**: Only shift creators (admins) can edit
- **Audited**: All edits are logged in audit logs

---

## Next Steps

1. **Test Edit Feature**: Create a shift, edit it, verify changes
2. **Test Signups**: Have employees sign up, then view in admin
3. **Monitor Activity**: Check audit logs for all actions
4. **Provide Feedback**: Let us know what other features would help

---

**Last Updated**: January 12, 2026
**Version**: 2.0.0

