import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Button, Form, Table, Spinner, Alert, Modal } from 'react-bootstrap';
import { shiftAPI, assignmentAPI } from '../api/client';
import { useAuthStore } from '../store/authStore';
import { useNavigate } from 'react-router-dom';
import { format } from 'date-fns';

function ShiftManagement() {
  const { user, isAdmin } = useAuthStore();
  const navigate = useNavigate();

  const [shifts, setShifts] = useState([]);
  const [shiftsWithDetails, setShiftsWithDetails] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Create/Edit shift modal states
  const [showShiftModal, setShowShiftModal] = useState(false);
  const [editingShiftId, setEditingShiftId] = useState(null);
  const [newShift, setNewShift] = useState({
    name: '',
    description: '',
    startTime: '',
    endTime: '',
    availableSlots: '',
  });

  // Shift details modal states
  const [showDetailsModal, setShowDetailsModal] = useState(false);
  const [selectedShift, setSelectedShift] = useState(null);
  const [shiftSignups, setShiftSignups] = useState([]);
  const [loadingDetails, setLoadingDetails] = useState(false);

  // Default shifts modal states
  const [showDefaultShiftsModal, setShowDefaultShiftsModal] = useState(false);
  const [defaultShiftDate, setDefaultShiftDate] = useState('');
  const [selectedDefaultShift, setSelectedDefaultShift] = useState('');
  const [defaultShiftName, setDefaultShiftName] = useState('');
  const [populateMonthly, setPopulateMonthly] = useState(false);

  // Bulk delete shifts by month states
  const [showDeleteByMonthModal, setShowDeleteByMonthModal] = useState(false);
  const [deleteByMonthDate, setDeleteByMonthDate] = useState('');


  // Month filter state
  const [selectedMonth, setSelectedMonth] = useState('');

  // Sorting states for shifts table
  const [sortBy, setSortBy] = useState('startTime');
  const [sortOrder, setSortOrder] = useState('asc');

  // Default shift templates
  const defaultShiftTemplates = [
    {
      id: 'weekend-early',
      name: 'Weekend Early Shift',
      startTime: '12:00',
      endTime: '17:00',
      description: '12:00 PM - 5:00 PM'
    },
    {
      id: 'weekend-late',
      name: 'Weekend Night Shift',
      startTime: '17:00',
      endTime: '22:00',
      description: '5:00 PM - 10:00 PM'
    },
    {
      id: 'weekday',
      name: 'Weekday Shift',
      startTime: '16:30',
      endTime: '21:00',
      description: '4:30 PM - 9:00 PM'
    },
    {
      id: 'friday-night',
      name: 'Friday Night Shift',
      startTime: '16:30',
      endTime: '22:00',
      description: '4:30 PM - 10:00 PM'
    },
    {
      id: 'sunday-early',
      name: 'Sunday Early Shift',
      startTime: '12:00',
      endTime: '16:00',
      description: '12:00 PM - 4:00 PM'
    },
    {
      id: 'sunday-late',
      name: 'Sunday Late Shift',
      startTime: '16:00',
      endTime: '20:00',
      description: '4:00 PM - 8:00 PM'
    }
  ];

  useEffect(() => {
    // Immediately redirect if not admin
    if (!isAdmin) {
      navigate('/');
      return;
    }
  }, [user, isAdmin, navigate]);

  useEffect(() => {
    if (isAdmin) {
      setLoading(true);
      fetchData();
      setLoading(false);
    }
  }, [isAdmin]);


  const fetchData = async () => {
    try {
      setLoading(true);
      const res = await shiftAPI.getAvailableShifts(0, 1000);
      setShifts(res.data.content);

      // Fetch all shift details in parallel
      const detailsMap = {};
      const detailPromises = res.data.content.map(shift =>
        shiftAPI.getShiftDetails(shift.id)
          .then(detailRes => {
            detailsMap[shift.id] = detailRes.data;
            return detailRes.data;
          })
          .catch(err => {
            console.error(`Error fetching details for shift ${shift.id}:`, err);
            detailsMap[shift.id] = null;
            return null;
          })
      );

      await Promise.all(detailPromises);
      setShiftsWithDetails(detailsMap);
    } catch (err) {
      if (err.response?.status === 403) {
        setError('You do not have permission to access this resource.');
      } else {
        setError('Failed to load data.');
      }
    } finally {
      setLoading(false);
    }
  };


  const handleOpenShiftModal = (shift = null) => {
    if (shift) {
      setEditingShiftId(shift.id);

      // Convert times to 12-hour format with AM/PM for display
      const startParts = shift.startTime.split('T');
      const endParts = shift.endTime.split('T');

      const startTime = startParts[1]; // HH:MM
      const endTime = endParts[1]; // HH:MM

      const [startHour24, startMin] = startTime.split(':');
      const [endHour24, endMin] = endTime.split(':');

      // Convert 24-hour to 12-hour format
      const startHour = parseInt(startHour24);
      const startAmPm = startHour >= 12 ? 'PM' : 'AM';
      const startHour12 = startHour === 0 ? 12 : startHour > 12 ? startHour - 12 : startHour;

      const endHour = parseInt(endHour24);
      const endAmPm = endHour >= 12 ? 'PM' : 'AM';
      const endHour12 = endHour === 0 ? 12 : endHour > 12 ? endHour - 12 : endHour;

      const formattedStartTime = `${startParts[0]}T${startHour12}:${startMin}:${startAmPm}`;
      const formattedEndTime = `${endParts[0]}T${endHour12}:${endMin}:${endAmPm}`;

      setNewShift({
        name: shift.name,
        description: shift.description,
        startTime: formattedStartTime,
        endTime: formattedEndTime,
        availableSlots: shift.availableSlots,
      });
    } else {
      setEditingShiftId(null);
      setNewShift({
        name: '',
        description: '',
        startTime: '',
        endTime: '',
        availableSlots: '',
      });
    }
    setShowShiftModal(true);
  };

  const handleCloseShiftModal = () => {
    setShowShiftModal(false);
    setEditingShiftId(null);
    setNewShift({
      name: '',
      description: '',
      startTime: '',
      endTime: '',
      availableSlots: '',
    });
  };

  const handleSaveShift = async (e) => {
    e.preventDefault();
    try {
      // Format times properly for backend (24-hour format without AM/PM)
      const formatTimeForBackend = (timeString) => {
        if (!timeString) return '';
        const parts = timeString.split('T');
        if (parts.length !== 2) return timeString;

        const date = parts[0];
        const timeParts = parts[1].split(':');

        if (timeParts.length >= 3) {
          // Format: HH:MM:AM/PM
          const hour12 = parseInt(timeParts[0]);
          const minute = timeParts[1];
          const ampm = timeParts[2];

          // Convert 12-hour to 24-hour
          let hour24;
          if (ampm === 'AM') {
            hour24 = hour12 === 12 ? 0 : hour12;
          } else {
            hour24 = hour12 === 12 ? 12 : hour12 + 12;
          }

          return `${date}T${String(hour24).padStart(2, '0')}:${minute}`;
        } else if (timeParts.length === 2) {
          // Already in correct format
          return timeString;
        }
        return timeString;
      };

      const shiftData = {
        name: newShift.name,
        description: newShift.description,
        startTime: formatTimeForBackend(newShift.startTime),
        endTime: formatTimeForBackend(newShift.endTime),
        availableSlots: newShift.availableSlots,
        active: true,
      };

      console.log('Updating shift with data:', shiftData);
      console.log('Shift ID:', editingShiftId);

      if (editingShiftId) {
        const response = await shiftAPI.updateShift(editingShiftId, shiftData);
        console.log('Update response:', response);
        setSuccess('Shift updated successfully!');
      } else {
        const response = await shiftAPI.createShift(shiftData);
        console.log('Create response:', response);
        setSuccess('Shift created successfully!');
      }
      handleCloseShiftModal();
      fetchData();
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      if (err.response?.status === 401) {
        setError('Authentication failed. Please log in again.');
      } else if (err.response?.status === 403) {
        setError('You do not have permission to perform this action.');
      } else {
        setError(err.response?.data?.message || 'Failed to save shift.');
      }
    }
  };

  // Add token validation helper
  const checkTokenAndRole = () => {
    const token = localStorage.getItem('token');
    const userStr = localStorage.getItem('user');

    console.log('Token check:', {
      hasToken: !!token,
      tokenPreview: token ? token.substring(0, 20) + '...' : 'none',
      user: userStr ? JSON.parse(userStr) : null,
      isAdmin
    });

    if (!token) {
      setError('No authentication token found. Please log in again.');
      return false;
    }

    if (!isAdmin) {
      setError('You do not have admin privileges required to delete shifts.');
      return false;
    }

    return true;
  };

  const handleDeleteShift = async (shiftId) => {
    // Check authentication before attempting delete
    if (!checkTokenAndRole()) {
      return;
    }

    // Check if shift has signups to inform user
    const shiftDetails = shiftsWithDetails[shiftId];
    const signupCount = shiftDetails?.signups ? shiftDetails.signups.length : 0;

    const confirmMessage = signupCount > 0
      ? `Are you sure you want to delete this shift? This will also remove all ${signupCount} signup(s) (pending and approved) for this shift.`
      : 'Are you sure you want to delete this shift?';

    if (window.confirm(confirmMessage)) {
      try {
        // Backend now handles cascade deletion of assignments automatically
        await shiftAPI.deleteShift(shiftId);

        const successMessage = signupCount > 0
          ? `Shift and all ${signupCount} associated signup(s) deleted successfully!`
          : 'Shift deleted successfully!';

        setSuccess(successMessage);
        fetchData();
        setTimeout(() => setSuccess(''), 3000);
      } catch (err) {
        console.error('Delete shift error:', err);

        if (err.response?.status === 401) {
          setError('Authentication failed. Please log in again and ensure you have admin privileges.');
          // Clear potentially invalid token
          localStorage.removeItem('token');
          localStorage.removeItem('user');
        } else if (err.response?.status === 403) {
          setError('You do not have permission to delete shifts. Please contact an administrator.');
        } else if (err.response?.status === 409 || err.response?.data?.message?.includes('constraint')) {
          // Handle foreign key constraint errors (shouldn't happen now, but just in case)
          setError('Cannot delete shift due to database constraints. Please try refreshing the page and try again.');
          fetchData();
        } else {
          const errorMessage = err.response?.data?.message || 'Failed to delete shift.';
          setError(`Failed to delete shift: ${errorMessage}`);
        }
      }
    }
  };

  const handleDeleteShiftsByMonth = async (e) => {
    e.preventDefault();

    if (!deleteByMonthDate) {
      setError('Please select a month and year.');
      return;
    }

    // Parse the month and year from the input
    const [year, month] = deleteByMonthDate.split('-').map(Number);

    // Filter shifts that fall within this month/year
    const shiftsToDelete = shifts.filter(shift => {
      const shiftDate = new Date(shift.startTime);
      return shiftDate.getFullYear() === year && (shiftDate.getMonth() + 1) === month;
    });

    if (shiftsToDelete.length === 0) {
      setError('No shifts found for this month.');
      return;
    }

    if (window.confirm(`Are you sure you want to delete all ${shiftsToDelete.length} shift(s) for this month? Any signups will also be deleted. This cannot be undone.`)) {
      try {
        let deletedCount = 0;
        let failedCount = 0;

        // Delete all shifts in the month
        for (const shift of shiftsToDelete) {
          try {
            // The backend now handles cascade deletion of assignments automatically
            await shiftAPI.deleteShift(shift.id);
            deletedCount++;
          } catch (err) {
            console.error(`Failed to delete shift ${shift.id}:`, err);
            failedCount++;
          }
        }

        let successMsg = `${deletedCount} shift(s) deleted successfully!`;
        if (failedCount > 0) {
          successMsg += ` (${failedCount} failed to delete)`;
        }
        setSuccess(successMsg);

        setShowDeleteByMonthModal(false);
        setDeleteByMonthDate('');
        fetchData();
        setTimeout(() => setSuccess(''), 5000);
      } catch (err) {
        console.error('Error in delete by month:', err);
        setError('Failed to delete shifts. Please try again.');
      }
    }
  };

  const handleViewSignups = async (shift) => {
    try {
      setLoadingDetails(true);
      const res = await shiftAPI.getShiftDetails(shift.id);
      setSelectedShift(res.data);
      setShiftSignups(res.data.signups || []);
      setShowDetailsModal(true);
    } catch (err) {
      setError('Failed to load shift details.');
    } finally {
      setLoadingDetails(false);
    }
  };

  const handleCloseDetailsModal = () => {
    setShowDetailsModal(false);
    setSelectedShift(null);
    setShiftSignups([]);
  };

  const handleAcceptSignup = async (assignmentId) => {
    try {
      await assignmentAPI.acceptSignup(assignmentId);
      setSuccess('Signup accepted successfully!');
      // Refresh the signups list
      if (selectedShift) {
        const res = await shiftAPI.getShiftDetails(selectedShift.id);
        setSelectedShift(res.data);
        setShiftSignups(res.data.signups || []);
        // Update the cached shift details
        setShiftsWithDetails(prev => ({ ...prev, [selectedShift.id]: res }));

        // Update the main shifts array with the new filledSlots count
        setShifts(prev => prev.map(shift =>
          shift.id === selectedShift.id
            ? { ...shift, filledSlots: res.data.filledSlots }
            : shift
        ));
      }
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError('Failed to accept signup.');
    }
  };

  const handleRejectSignup = async (assignmentId) => {
    if (window.confirm('Are you sure you want to reject this signup?')) {
      try {
        await assignmentAPI.rejectSignup(assignmentId);
        setSuccess('Signup rejected successfully!');
        // Refresh the signups list
        if (selectedShift) {
          const res = await shiftAPI.getShiftDetails(selectedShift.id);
          setSelectedShift(res.data);
          setShiftSignups(res.data.signups || []);
          // Update the cached shift details
          setShiftsWithDetails(prev => ({ ...prev, [selectedShift.id]: res }));

          // Update the main shifts array with the new filledSlots count
          setShifts(prev => prev.map(shift =>
            shift.id === selectedShift.id
              ? { ...shift, filledSlots: res.data.filledSlots }
              : shift
          ));
        }
        setTimeout(() => setSuccess(''), 3000);
      } catch (err) {
        setError('Failed to reject signup.');
      }
    }
  };

  const handleRemoveSignup = async (assignmentId) => {
    if (window.confirm('Are you sure you want to remove this approved signup?')) {
      try {
        await assignmentAPI.rejectSignup(assignmentId);
        setSuccess('Signup removed successfully!');
        // Refresh the signups list
        if (selectedShift) {
          const res = await shiftAPI.getShiftDetails(selectedShift.id);
          setSelectedShift(res.data);
          setShiftSignups(res.data.signups || []);
          // Update the cached shift details
          setShiftsWithDetails(prev => ({ ...prev, [selectedShift.id]: res }));

          // Update the main shifts array with the new filledSlots count
          setShifts(prev => prev.map(shift =>
            shift.id === selectedShift.id
              ? { ...shift, filledSlots: res.data.filledSlots }
              : shift
          ));
        }
        setTimeout(() => setSuccess(''), 3000);
      } catch (err) {
        setError('Failed to remove signup.');
      }
    }
  };

  const handleAddDefaultShift = async (e) => {
    e.preventDefault();

    if (!selectedDefaultShift || !defaultShiftDate) {
      setError('Please select both a shift template and a date.');
      return;
    }

    try {
      const template = defaultShiftTemplates.find(t => t.id === selectedDefaultShift);

      // Parse date string as local date (YYYY-MM-DD)
      const [year, month, day] = defaultShiftDate.split('-').map(Number);
      const selectedDate = new Date(year, month - 1, day);
      const selectedDayOfWeek = selectedDate.getDay(); // 0-6 (Sunday-Saturday)
      const currentMonth = selectedDate.getMonth();
      const currentYear = selectedDate.getFullYear();

      // Determine which dates to create shifts for
      let datesToCreate = [defaultShiftDate];

      if (populateMonthly) {
        // Find all dates in the month with the same day of week that are on or after the selected date
        datesToCreate = [];
        const daysInMonth = new Date(currentYear, currentMonth + 1, 0).getDate();

        for (let dayNum = 1; dayNum <= daysInMonth; dayNum++) {
          const date = new Date(currentYear, currentMonth, dayNum);
          // Only include dates that match the day of week AND are on or after the selected date
          if (date.getDay() === selectedDayOfWeek && date >= selectedDate) {
            // Format as YYYY-MM-DD
            const dateStr = date.toISOString().split('T')[0];
            datesToCreate.push(dateStr);
          }
        }
      }

      // Create shifts for each date
      for (const dateString of datesToCreate) {
        const [startHour, startMin] = template.startTime.split(':');
        const [endHour, endMin] = template.endTime.split(':');

        const startDateTime = `${dateString}T${startHour}:${startMin}`;
        let endDateTime = `${dateString}T${endHour}:${endMin}`;

        // If end time is earlier than start time, it's next day
        if (endHour < startHour || (endHour === startHour && endMin < startMin)) {
          const endDate = new Date(dateString);
          endDate.setDate(endDate.getDate() + 1);
          const endDateString = endDate.toISOString().split('T')[0];
          endDateTime = `${endDateString}T${endHour}:${endMin}`;
        }

        // Parse date for day of week and date display
        const [dateYear, dateMonth, dateDay] = dateString.split('-').map(Number);
        const displayDate = new Date(dateYear, dateMonth - 1, dateDay);
        const dayOfWeekName = displayDate.toLocaleDateString('en-US', { weekday: 'long' });
        const formattedDate = displayDate.toLocaleDateString('en-US', { month: 'short', day: '2-digit' });

        // Use custom shift name if provided, otherwise use day of week - date format
        const shiftName = defaultShiftName.trim()
          ? `${defaultShiftName} - ${formattedDate}`
          : `${dayOfWeekName} - ${formattedDate}`;

        const shiftData = {
          name: shiftName,
          description: template.description,
          startTime: startDateTime,
          endTime: endDateTime,
          availableSlots: 1
        };

        await shiftAPI.createShift(shiftData);
      }

      const shiftCount = datesToCreate.length;
      const countText = shiftCount === 1 ? 'shift' : 'shifts';
      setSuccess(`${shiftCount} ${countText} created successfully!`);

      setShowDefaultShiftsModal(false);
      setDefaultShiftDate('');
      setSelectedDefaultShift('');
      setDefaultShiftName('');
      setPopulateMonthly(false);
      fetchData();
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError('Failed to add default shift.');
    }
  };

  const formatDateTime = (dateString) => {
    try {
      // Handle different date string formats from backend
      let date;

      if (dateString.includes('T') && !dateString.endsWith('Z')) {
        // If it's in format "2026-01-14T15:30:45" (no timezone info), treat as UTC
        date = new Date(dateString + 'Z');
      } else {
        // If it already has timezone info or is in other format
        date = new Date(dateString);
      }

      // Check if date is valid
      if (isNaN(date.getTime())) {
        return dateString;
      }

      return format(date, 'MMM dd, yyyy h:mm a');
    } catch (error) {
      console.error('Error formatting date:', error, dateString);
      return dateString;
    }
  };

  const handleSort = (column) => {
    if (sortBy === column) {
      // Toggle sort order if clicking the same column
      setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
    } else {
      // Set new column and default to ascending
      setSortBy(column);
      setSortOrder('asc');
    }
  };

  const getSortedAndFilteredShifts = () => {
    // First filter by month if selected
    let filteredShifts = shifts;
    if (selectedMonth) {
      const [year, month] = selectedMonth.split('-').map(Number);
      filteredShifts = shifts.filter(shift => {
        const shiftDate = new Date(shift.startTime);
        return shiftDate.getFullYear() === year && (shiftDate.getMonth() + 1) === month;
      });
    }

    // Then sort the filtered shifts
    const sorted = [...filteredShifts].sort((a, b) => {
      let valueA, valueB;

      if (sortBy === 'startTime') {
        valueA = new Date(a.startTime);
        valueB = new Date(b.startTime);
      } else if (sortBy === 'pending') {
        const detailsA = shiftsWithDetails[a.id];
        const detailsB = shiftsWithDetails[b.id];
        valueA = detailsA?.signups?.filter(s => !s.accepted).length || 0;
        valueB = detailsB?.signups?.filter(s => !s.accepted).length || 0;
      } else if (sortBy === 'name') {
        valueA = a.name.toLowerCase();
        valueB = b.name.toLowerCase();
      } else if (sortBy === 'slots') {
        valueA = a.availableSlots;
        valueB = b.availableSlots;
      } else if (sortBy === 'filled') {
        valueA = a.filledSlots || 0;
        valueB = b.filledSlots || 0;
      } else {
        return 0;
      }

      if (valueA < valueB) return sortOrder === 'asc' ? -1 : 1;
      if (valueA > valueB) return sortOrder === 'asc' ? 1 : -1;
      return 0;
    });

    return sorted;
  };

  const getFilteredShifts = () => {
    // Keep the old function name for compatibility, but use the new sorted version
    return getSortedAndFilteredShifts();
  };

  // Sortable header component
  const SortableHeader = ({ column, label }) => (
    <th
      onClick={() => handleSort(column)}
      style={{
        cursor: 'pointer',
        userSelect: 'none',
        backgroundColor: sortBy === column ? '#e9ecef' : 'transparent'
      }}
      title={`Click to sort by ${label}`}
    >
      {label}
      {sortBy === column && (
        <span className="ms-2">
          {sortOrder === 'asc' ? '▲' : '▼'}
        </span>
      )}
    </th>
  );

  if (!user) {
    return (
      <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '100vh' }}>
        <Alert variant="warning">Please log in to access the admin dashboard.</Alert>
      </Container>
    );
  }

  if (!isAdmin) {
    return (
      <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '100vh' }}>
        <Alert variant="danger">{error || 'You do not have admin privileges.'}</Alert>
      </Container>
    );
  }

  if (loading) {
    return (
      <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '100vh' }}>
        <Spinner animation="border" />
      </Container>
    );
  }

  return (
    <Container className="py-5">
      <h1 className="mb-4">Shift Management</h1>

      {error && <Alert variant="danger" onClose={() => setError('')} dismissible>{error}</Alert>}
      {success && <Alert variant="success" onClose={() => setSuccess('')} dismissible>{success}</Alert>}

      <div className="mb-4">
        <Button
          variant="success"
          className="me-2"
          onClick={() => handleOpenShiftModal()}
        >
          Create New Shift
        </Button>
        <Button
          variant="info"
          className="me-2"
          onClick={() => setShowDefaultShiftsModal(true)}
        >
          Add Default Shift
        </Button>
        <Button
          variant="danger"
          onClick={() => setShowDeleteByMonthModal(true)}
        >
          Delete Shifts by Month
        </Button>
      </div>

          {/* Create/Edit Shift Modal */}
          <Modal show={showShiftModal} onHide={handleCloseShiftModal} size="lg">
            <Modal.Header closeButton>
              <Modal.Title>{editingShiftId ? 'Edit Shift' : 'Create New Shift'}</Modal.Title>
            </Modal.Header>
            <Modal.Body>
              <Form onSubmit={handleSaveShift}>
                <Form.Group className="mb-3">
                  <Form.Label>Shift Name</Form.Label>
                  <Form.Control
                    type="text"
                    value={newShift.name}
                    onChange={(e) => setNewShift({ ...newShift, name: e.target.value })}
                    required
                  />
                </Form.Group>

                <Form.Group className="mb-3">
                  <Form.Label>Description</Form.Label>
                  <Form.Control
                    type="text"
                    value={newShift.description}
                    onChange={(e) => setNewShift({ ...newShift, description: e.target.value })}
                  />
                </Form.Group>

                <Form.Group className="mb-3">
                  <Form.Label>Start Time</Form.Label>
                  <div className="d-flex gap-2">
                    <Form.Control
                      type="date"
                      value={newShift.startTime?.split('T')[0] || ''}
                      onChange={(e) => {
                        const date = e.target.value;
                        const time = newShift.startTime?.split('T')[1] || '17:00:PM'; // Default to 5:00 PM
                        // Auto-populate end date with same date if not already set
                        const currentEndDate = newShift.endTime?.split('T')[0];
                        if (!currentEndDate) {
                          setNewShift({ ...newShift, startTime: `${date}T${time}`, endTime: `${date}T22:00:PM` });
                        } else {
                          setNewShift({ ...newShift, startTime: `${date}T${time}` });
                        }
                      }}
                      required
                      style={{ flex: 1 }}
                    />
                    <Form.Select
                      value={newShift.startTime?.split('T')[1]?.split(':')[0] || '5'}
                      onChange={(e) => {
                        const date = newShift.startTime?.split('T')[0] || new Date().toISOString().split('T')[0];
                        const minute = newShift.startTime?.split('T')[1]?.split(':')[1] || '00';
                        const ampm = newShift.startTime?.split('T')[1]?.split(':')[2] || 'PM';
                        setNewShift({ ...newShift, startTime: `${date}T${e.target.value}:${minute}:${ampm}` });
                      }}
                      style={{ flex: 0.5 }}
                    >
                      {[...Array(12)].map((_, i) => (
                        <option key={i} value={String(i + 1)}>
                          {String(i + 1)}
                        </option>
                      ))}
                    </Form.Select>
                    <Form.Select
                      value={newShift.startTime?.split('T')[1]?.split(':')[1] || '00'}
                      onChange={(e) => {
                        const date = newShift.startTime?.split('T')[0] || new Date().toISOString().split('T')[0];
                        const hour = newShift.startTime?.split('T')[1]?.split(':')[0] || '5';
                        const ampm = newShift.startTime?.split('T')[1]?.split(':')[2] || 'PM';
                        setNewShift({ ...newShift, startTime: `${date}T${hour}:${e.target.value}:${ampm}` });
                      }}
                      style={{ flex: 0.5 }}
                    >
                      <option value="00">00</option>
                      <option value="15">15</option>
                      <option value="30">30</option>
                      <option value="45">45</option>
                    </Form.Select>
                    <Form.Select
                      value={newShift.startTime?.split('T')[1]?.split(':')[2] || 'PM'}
                      onChange={(e) => {
                        const date = newShift.startTime?.split('T')[0] || new Date().toISOString().split('T')[0];
                        const hour12 = newShift.startTime?.split('T')[1]?.split(':')[0] || '5';
                        const minute = newShift.startTime?.split('T')[1]?.split(':')[1] || '00';
                        setNewShift({ ...newShift, startTime: `${date}T${hour12}:${minute}:${e.target.value}` });
                      }}
                      style={{ flex: 0.3 }}
                    >
                      <option value="AM">AM</option>
                      <option value="PM">PM</option>
                    </Form.Select>
                  </div>
                  <small className="text-muted">Format: Date | Hour (1-12) | Minutes (00, 15, 30, 45) | AM/PM</small>
                </Form.Group>

                <Form.Group className="mb-3">
                  <Form.Label>End Time</Form.Label>
                  <div className="d-flex gap-2">
                    <Form.Control
                      type="date"
                      value={newShift.endTime?.split('T')[0] || ''}
                      onChange={(e) => {
                        const date = e.target.value;
                        const time = newShift.endTime?.split('T')[1] || '22:00:PM'; // Default to 10:00 PM
                        setNewShift({ ...newShift, endTime: `${date}T${time}` });
                      }}
                      required
                      style={{ flex: 1 }}
                    />
                    <Form.Select
                      value={newShift.endTime?.split('T')[1]?.split(':')[0] || '10'}
                      onChange={(e) => {
                        const date = newShift.endTime?.split('T')[0] || new Date().toISOString().split('T')[0];
                        const minute = newShift.endTime?.split('T')[1]?.split(':')[1] || '00';
                        const ampm = newShift.endTime?.split('T')[1]?.split(':')[2] || 'PM';
                        setNewShift({ ...newShift, endTime: `${date}T${e.target.value}:${minute}:${ampm}` });
                      }}
                      style={{ flex: 0.5 }}
                    >
                      {[...Array(12)].map((_, i) => (
                        <option key={i} value={String(i + 1)}>
                          {String(i + 1)}
                        </option>
                      ))}
                    </Form.Select>
                    <Form.Select
                      value={newShift.endTime?.split('T')[1]?.split(':')[1] || '00'}
                      onChange={(e) => {
                        const date = newShift.endTime?.split('T')[0] || new Date().toISOString().split('T')[0];
                        const hour = newShift.endTime?.split('T')[1]?.split(':')[0] || '10';
                        const ampm = newShift.endTime?.split('T')[1]?.split(':')[2] || 'PM';
                        setNewShift({ ...newShift, endTime: `${date}T${hour}:${e.target.value}:${ampm}` });
                      }}
                      style={{ flex: 0.5 }}
                    >
                      <option value="00">00</option>
                      <option value="15">15</option>
                      <option value="30">30</option>
                      <option value="45">45</option>
                    </Form.Select>
                    <Form.Select
                      value={newShift.endTime?.split('T')[1]?.split(':')[2] || 'PM'}
                      onChange={(e) => {
                        const date = newShift.endTime?.split('T')[0] || new Date().toISOString().split('T')[0];
                        const hour12 = newShift.endTime?.split('T')[1]?.split(':')[0] || '10';
                        const minute = newShift.endTime?.split('T')[1]?.split(':')[1] || '00';
                        setNewShift({ ...newShift, endTime: `${date}T${hour12}:${minute}:${e.target.value}` });
                      }}
                      style={{ flex: 0.3 }}
                    >
                      <option value="AM">AM</option>
                      <option value="PM">PM</option>
                    </Form.Select>
                  </div>
                  <small className="text-muted">Format: Date | Hour (1-12) | Minutes (00, 15, 30, 45) | AM/PM</small>
                </Form.Group>

                <Form.Group className="mb-3">
                  <Form.Label>Available Slots</Form.Label>
                  <Form.Control
                    type="number"
                    value={newShift.availableSlots}
                    onChange={(e) => setNewShift({ ...newShift, availableSlots: parseInt(e.target.value) })}
                    required
                  />
                </Form.Group>

                <Button variant="primary" type="submit">
                  {editingShiftId ? 'Update Shift' : 'Create Shift'}
                </Button>
              </Form>
            </Modal.Body>
          </Modal>

          {/* Shift Details Modal */}
          <Modal show={showDetailsModal} onHide={handleCloseDetailsModal} size="lg">
            <Modal.Header closeButton>
              <Modal.Title>{selectedShift?.name} - Signups</Modal.Title>
            </Modal.Header>
            <Modal.Body>
              {loadingDetails ? (
                <Spinner animation="border" />
              ) : (
                <>
                  <div className="mb-3">
                    <h6>Shift Details</h6>
                    <p>
                      <strong>Start:</strong> {selectedShift && formatDateTime(selectedShift.startTime)}
                    </p>
                    <p>
                      <strong>End:</strong> {selectedShift && formatDateTime(selectedShift.endTime)}
                    </p>
                    <p>
                      <strong>Available Slots:</strong> {selectedShift?.availableSlots}
                    </p>
                    <p>
                      <strong>Filled Slots:</strong> {selectedShift?.filledSlots || shiftSignups.length}
                    </p>
                  </div>

                  <h6>Employees Signed Up</h6>
                  {shiftSignups.length === 0 ? (
                    <Alert variant="info">No employees have signed up for this shift yet.</Alert>
                  ) : (
                    <Table striped bordered hover size="sm">
                      <thead>
                        <tr>
                          <th>Email</th>
                          <th>Name</th>
                          <th>Signed Up</th>
                          <th>Status</th>
                          <th>Actions</th>
                        </tr>
                      </thead>
                      <tbody>
                        {shiftSignups.map((signup) => (
                          <tr key={signup.id}>
                            <td>{signup.userEmail}</td>
                            <td>{signup.userFirstName} {signup.userLastName}</td>
                            <td>{formatDateTime(signup.signedUpAt)}</td>
                            <td>
                              <span className={signup.accepted ? 'badge bg-success' : 'badge bg-warning'}>
                                {signup.accepted ? 'Accepted' : 'Pending'}
                              </span>
                            </td>
                            <td>
                              {!signup.accepted && (
                                <>
                                  <Button
                                    variant="success"
                                    size="sm"
                                    className="me-2"
                                    onClick={() => handleAcceptSignup(signup.id)}
                                  >
                                    Accept
                                  </Button>
                                  <Button
                                    variant="danger"
                                    size="sm"
                                    onClick={() => handleRejectSignup(signup.id)}
                                  >
                                    Reject
                                  </Button>
                                </>
                              )}
                              {signup.accepted && (
                                <Button
                                  variant="danger"
                                  size="sm"
                                  onClick={() => handleRemoveSignup(signup.id)}
                                >
                                  Remove
                                </Button>
                              )}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </Table>
                  )}
                </>
              )}
            </Modal.Body>
          </Modal>

          {/* Default Shifts Modal */}
          <Modal show={showDefaultShiftsModal} onHide={() => {
            setShowDefaultShiftsModal(false);
            setDefaultShiftName('');
            setPopulateMonthly(false);
          }} size="lg">
            <Modal.Header closeButton>
              <Modal.Title>Add Default Shift</Modal.Title>
            </Modal.Header>
            <Modal.Body>
              <Form onSubmit={handleAddDefaultShift}>
                <Form.Group className="mb-3">
                  <Form.Label>Select Shift Template</Form.Label>
                  <Form.Select
                    value={selectedDefaultShift}
                    onChange={(e) => setSelectedDefaultShift(e.target.value)}
                    required
                  >
                    <option value="">-- Choose a shift --</option>
                    {defaultShiftTemplates.map(template => (
                      <option key={template.id} value={template.id}>
                        {template.name} ({template.description})
                      </option>
                    ))}
                  </Form.Select>
                </Form.Group>

                <Form.Group className="mb-3">
                  <Form.Label>Select Date</Form.Label>
                  <Form.Control
                    type="date"
                    value={defaultShiftDate}
                    onChange={(e) => setDefaultShiftDate(e.target.value)}
                    required
                  />
                </Form.Group>

                <Form.Group className="mb-3">
                  <Form.Label>Shift Name (Optional)</Form.Label>
                  <Form.Control
                    type="text"
                    placeholder={defaultShiftDate ? (() => {
                      const [year, month, day] = defaultShiftDate.split('-').map(Number);
                      const date = new Date(year, month - 1, day);
                      const dayOfWeek = date.toLocaleDateString('en-US', { weekday: 'long' });
                      return `${dayOfWeek} Shift`;
                    })() : 'Select a date first'}
                    value={defaultShiftName}
                    onChange={(e) => setDefaultShiftName(e.target.value)}
                    disabled={!defaultShiftDate}
                  />
                  <Form.Text className="text-muted">
                    Leave blank to use template name. Day of week will be automatically added.
                  </Form.Text>
                </Form.Group>

                <Form.Group className="mb-3">
                  <Form.Check
                    type="checkbox"
                    label="Populate all matching weekdays in the month"
                    checked={populateMonthly}
                    onChange={(e) => setPopulateMonthly(e.target.checked)}
                    disabled={!defaultShiftDate}
                  />
                  <Form.Text className="text-muted d-block mt-2">
                    If checked, this shift will be created for the selected date and all future {defaultShiftDate ? (() => {
                      const [year, month, day] = defaultShiftDate.split('-').map(Number);
                      const date = new Date(year, month - 1, day);
                      return date.toLocaleDateString('en-US', { weekday: 'long' });
                    })() : 'matching weekday'}s in the month (past dates will be skipped).
                  </Form.Text>
                </Form.Group>

                <div className="mb-3 p-3 bg-light rounded">
                  <h6>Shift Preview:</h6>
                  {selectedDefaultShift && defaultShiftDate && (
                    <div>
                      {defaultShiftTemplates
                        .filter(t => t.id === selectedDefaultShift)
                        .map(template => {
                          // Parse date correctly as local date
                          const [year, month, day] = defaultShiftDate.split('-').map(Number);
                          const selectedDate = new Date(year, month - 1, day);
                          const dayOfWeekName = selectedDate.toLocaleDateString('en-US', { weekday: 'long' });
                          const formattedDate = selectedDate.toLocaleDateString('en-US', { month: 'short', day: '2-digit' });

                          const previewName = defaultShiftName.trim()
                            ? `${defaultShiftName} - ${formattedDate}`
                            : `${dayOfWeekName} - ${formattedDate}`;

                          const selectedDayOfWeek = selectedDate.getDay();
                          const currentMonth = selectedDate.getMonth();
                          const currentYear = selectedDate.getFullYear();
                          let daysInMonth = 0;

                          if (populateMonthly) {
                            const lastDay = new Date(currentYear, currentMonth + 1, 0).getDate();
                            for (let dayNum = 1; dayNum <= lastDay; dayNum++) {
                              const date = new Date(currentYear, currentMonth, dayNum);
                              // Only count dates that match the day of week AND are on or after the selected date
                              if (date.getDay() === selectedDayOfWeek && date >= selectedDate) {
                                daysInMonth++;
                              }
                            }
                          }

                          return (
                            <div key={template.id}>
                              <p><strong>Name:</strong> {previewName}</p>
                              <p><strong>Time:</strong> {template.description}</p>
                              <p><strong>Available Slots:</strong> 1</p>
                              {populateMonthly && (
                                <p><strong>Total Shifts to Create:</strong> {daysInMonth}</p>
                              )}
                            </div>
                          );
                        })
                      }
                    </div>
                  )}
                </div>

                <Button variant="primary" type="submit">
                  Add Shift{populateMonthly && defaultShiftDate ? `s (${(() => {
                    const [year, month, day] = defaultShiftDate.split('-').map(Number);
                    const selectedDate = new Date(year, month - 1, day);
                    const selectedDayOfWeek = selectedDate.getDay();
                    const currentMonth = selectedDate.getMonth();
                    const currentYear = selectedDate.getFullYear();
                    let count = 0;
                    const lastDay = new Date(currentYear, currentMonth + 1, 0).getDate();
                    for (let dayNum = 1; dayNum <= lastDay; dayNum++) {
                      const date = new Date(currentYear, currentMonth, dayNum);
                      // Only count dates that match the day of week AND are on or after the selected date
                      if (date.getDay() === selectedDayOfWeek && date >= selectedDate) {
                        count++;
                      }
                    }
                    return count;
                  })()})` : ''}
                </Button>
              </Form>
            </Modal.Body>
          </Modal>

          {/* Delete Shifts by Month Modal */}
          <Modal show={showDeleteByMonthModal} onHide={() => {
            setShowDeleteByMonthModal(false);
            setDeleteByMonthDate('');
          }} size="lg">
            <Modal.Header closeButton>
              <Modal.Title>Delete Shifts by Month</Modal.Title>
            </Modal.Header>
            <Modal.Body>
              <Form onSubmit={handleDeleteShiftsByMonth}>
                <Form.Group className="mb-3">
                  <Form.Label>Select Month and Year</Form.Label>
                  <Form.Control
                    type="month"
                    value={deleteByMonthDate}
                    onChange={(e) => setDeleteByMonthDate(e.target.value)}
                    required
                  />
                  <Form.Text className="text-muted">
                    Select the month and year to delete all shifts from that month.
                  </Form.Text>
                </Form.Group>

                <Alert variant="warning">
                  <strong>Warning:</strong> This will permanently delete all shifts for the selected month. This action cannot be undone.
                </Alert>

                <Button variant="danger" type="submit">
                  Delete All Shifts for This Month
                </Button>
              </Form>
            </Modal.Body>
          </Modal>

          {/* Month Filter and Clear Filter Section */}
          <Row className="mb-3">
            <Col md={6}>
              <Form.Group>
                <Form.Label>Filter by Month</Form.Label>
                <Form.Control
                  type="month"
                  value={selectedMonth}
                  onChange={(e) => setSelectedMonth(e.target.value)}
                  placeholder="Select month to filter shifts"
                />
                <Form.Text className="text-muted">
                  Select a month to filter shifts. Leave empty to show all shifts.
                </Form.Text>
              </Form.Group>
            </Col>
            <Col md={6} className="d-flex align-items-end">
              {selectedMonth && (
                <div>
                  <Button
                    variant="outline-secondary"
                    onClick={() => setSelectedMonth('')}
                    className="me-2"
                  >
                    Clear Filter
                  </Button>
                  <small className="text-muted">
                    Showing shifts for {(() => {
                      const [year, month] = selectedMonth.split('-').map(Number);
                      const date = new Date(year, month - 1, 1);
                      return date.toLocaleDateString('en-US', {
                        month: 'long',
                        year: 'numeric'
                      });
                    })()} ({getFilteredShifts().length} shifts)
                  </small>
                </div>
              )}
              {!selectedMonth && (
                <small className="text-muted">
                  Showing all shifts ({getFilteredShifts().length} total)
                </small>
              )}
            </Col>
          </Row>

          {/* Shifts Table */}
          <Table striped bordered hover responsive>
            <thead>
              <tr>
                <SortableHeader column="name" label="Name" />
                <SortableHeader column="startTime" label="Start Time" />
                <th>End Time</th>
                <SortableHeader column="slots" label="Slots" />
                <SortableHeader column="filled" label="Filled" />
                <SortableHeader column="pending" label="Pending" />
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {getFilteredShifts().map((shift) => (
                <tr key={shift.id}>
                  <td>{shift.name}</td>
                  <td>{formatDateTime(shift.startTime)}</td>
                  <td>{formatDateTime(shift.endTime)}</td>
                  <td>{shift.availableSlots}</td>
                  <td>{shift.filledSlots}</td>
                  <td>
                    {(() => {
                      const details = shiftsWithDetails[shift.id];
                      const pending = details?.signups?.filter(s => !s.accepted).length || 0;
                      return pending > 0 ? (
                        <span className="badge bg-warning">{pending}</span>
                      ) : (
                        <span className="text-muted">0</span>
                      );
                    })()}
                  </td>
                  <td>
                    <Button
                      variant="info"
                      size="sm"
                      className="me-2"
                      onClick={() => handleViewSignups(shift)}
                    >
                      View Signups
                    </Button>
                    <Button
                      variant="warning"
                      size="sm"
                      className="me-2"
                      onClick={() => handleOpenShiftModal(shift)}
                    >
                      Edit
                    </Button>
                    <Button
                      variant="danger"
                      size="sm"
                      onClick={() => handleDeleteShift(shift.id)}
                    >
                      Delete
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
    </Container>
  );
}

export default ShiftManagement;

