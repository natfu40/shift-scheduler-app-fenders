# Quick Guide: Signup Approval Workflow

## The 3-Step Process

### Step 1: Employee Signs Up ✓
```
Employee Dashboard 
  → Find shift "Morning Shift" 
  → Click "Sign Up" 
  → Success message
  → Shift appears in "My Shifts" (Pending)
```

### Step 2: Admin Reviews ✓
```
Admin Dashboard 
  → Find shift "Morning Shift" 
  → Click "View Signups" button 
  → Modal opens showing all signups
  → See employee with yellow "Pending" badge
```

### Step 3: Admin Approves ✓
```
In "View Signups" Modal 
  → Find employee's pending signup 
  → Click green "Accept" button 
  → Status changes to green "Accepted" 
  → filledSlots increases 
  → Success message shown
```

---

## What You'll See

### Before Approval
```
Filled Slots: 0/5
Employee Status: Yellow "Pending"
Action: Accept / Reject buttons visible
```

### After Approval
```
Filled Slots: 1/5
Employee Status: Green "Accepted"
Action: No buttons (already approved)
```

---

## Common Actions

| What You Want | How to Do It |
|---|---|
| See who signed up | Click "View Signups" on shift |
| Approve signup | Click green "Accept" button |
| Decline signup | Click red "Reject" button |
| See slot count | Look at "Filled" column in table |
| Check timestamps | See "Signed Up" column in modal |
| Check employee info | See Email and Name columns |

---

## Status Meanings

| Badge | Meaning | What to Do |
|---|---|---|
| 🟡 Pending | Waiting for approval | Click "Accept" or "Reject" |
| 🟢 Accepted | Approved and confirmed | No action needed |

---

## Tips

- **Refresh After Signup**: If you don't see a signup, close and reopen "View Signups" modal
- **Check Email**: Use employee email to contact them about shift
- **Verify Before Approving**: Check the signup time to ensure it's valid
- **Monitor Slots**: Keep an eye on filledSlots vs availableSlots

---

## Troubleshooting

| Problem | Solution |
|---|---|
| Don't see "Accept/Reject" buttons | Signup is already approved (no action needed) |
| Buttons disappeared after clicking | Page refreshed automatically (working as intended) |
| Signup not showing at all | Try refreshing the page or check if employee actually signed up |
| Error message when accepting | Try again or check if you have ADMIN role |

---

**That's it!** The system handles the rest automatically. 🚀

