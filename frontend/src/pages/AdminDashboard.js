import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Button, Form, Table, Spinner, Alert, Modal } from 'react-bootstrap';
import { shiftAPI, auditAPI, assignmentAPI } from '../api/client';
import { useAuthStore } from '../store/authStore';
import { useNavigate } from 'react-router-dom';
import { format } from 'date-fns';

function AdminDashboard() {
  const { user, isAdmin } = useAuthStore();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('shifts');
  const [shifts, setShifts] = useState([]);
  const [auditLogs, setAuditLogs] = useState([]);
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
  }, [activeTab, isAdmin]);

  const checkAdminStatus = async () => {
    try {
      setLoading(true);
      await shiftAPI.getAvailableShifts(0, 1);
    } catch (err) {
      if (err.response?.status === 403) {
        setError('You do not have admin privileges. Contact an administrator.');
      } else {
        setError('Failed to load data.');
      }
    } finally {
      setLoading(false);
    }
  };

  const fetchData = async () => {
    try {
      setLoading(true);
      if (activeTab === 'shifts') {
        const res = await shiftAPI.getAvailableShifts(0, 100);
        setShifts(res.data.content);
      } else if (activeTab === 'logs') {
        const res = await auditAPI.getAllLogs(0, 50);
        setAuditLogs(res.data.content);
      }
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

  const convertTo24Hour = (hour12, ampm) => {
    if (ampm === 'AM') {
      return hour12 === 12 ? 0 : hour12;
    } else {
      return hour12 === 12 ? 12 : hour12 + 12;
    }
  };

  const convertTo12Hour = (hour24) => {
    const hour = parseInt(hour24);
    if (hour === 0) return { hour12: 12, ampm: 'AM' };
    if (hour < 12) return { hour12: hour, ampm: 'AM' };
    if (hour === 12) return { hour12: 12, ampm: 'PM' };
    return { hour12: hour - 12, ampm: 'PM' };
  };

  const handleOpenShiftModal = (shift = null) => {
    if (shift) {
      setEditingShiftId(shift.id);

      // Convert times to 12-hour format with AM/PM for display
      const startParts = shift.startTime.split('T');
      const endParts = shift.endTime.split('T');

      const startTime = startParts[1]; // HH:MM
      const endTime = endParts[1]; // HH:MM

      const [startHour, startMin] = startTime.split(':');
      const [endHour, endMin] = endTime.split(':');

      const startConverted = convertTo12Hour(startHour);
      const endConverted = convertTo12Hour(endHour);

      const formattedStartTime = `${shift.startTime.split('T')[0]}T${String(startConverted.hour12).padStart(2, '0')}:${startMin}:${startConverted.ampm}`;
      const formattedEndTime = `${shift.endTime.split('T')[0]}T${String(endConverted.hour12).padStart(2, '0')}:${endMin}:${endConverted.ampm}`;

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
          const hour24 = String(parseInt(timeParts[0])).padStart(2, '0');
          const minute = timeParts[1];
          return `${date}T${hour24}:${minute}`;
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
      };

      if (editingShiftId) {
        await shiftAPI.updateShift(editingShiftId, shiftData);
        setSuccess('Shift updated successfully!');
      } else {
        await shiftAPI.createShift(shiftData);
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

  const handleDeleteShift = async (shiftId) => {
    if (window.confirm('Are you sure you want to delete this shift?')) {
      try {
        await shiftAPI.deleteShift(shiftId);
        setSuccess('Shift deleted successfully!');
        fetchData();
        setTimeout(() => setSuccess(''), 3000);
      } catch (err) {
        setError('Failed to delete shift.');
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
        // Also refresh the main shifts list
        fetchData();
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
          // Also refresh the main shifts list
          fetchData();
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
          // Also refresh the main shifts list
          fetchData();
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
        // Find all dates in the month with the same day of week
        datesToCreate = [];
        const daysInMonth = new Date(currentYear, currentMonth + 1, 0).getDate();

        for (let dayNum = 1; dayNum <= daysInMonth; dayNum++) {
          const date = new Date(currentYear, currentMonth, dayNum);
          if (date.getDay() === selectedDayOfWeek) {
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

        // Parse date for day of week display
        const [dateYear, dateMonth, dateDay] = dateString.split('-').map(Number);
        const displayDate = new Date(dateYear, dateMonth - 1, dateDay);
        const dayOfWeekName = displayDate.toLocaleDateString('en-US', { weekday: 'long' });

        // Use custom shift name if provided, otherwise use just the day of week
        const shiftName = defaultShiftName.trim()
          ? `${defaultShiftName}`
          : `${dayOfWeekName}`;

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
      return format(new Date(dateString), 'MMM dd, yyyy h:mm a');
    } catch {
      return dateString;
    }
  };


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
      <h1 className="mb-4">Admin Dashboard</h1>

      {error && <Alert variant="danger" onClose={() => setError('')} dismissible>{error}</Alert>}
      {success && <Alert variant="success" onClose={() => setSuccess('')} dismissible>{success}</Alert>}

      <div className="mb-4">
        <Button
          variant={activeTab === 'shifts' ? 'primary' : 'outline-primary'}
          onClick={() => setActiveTab('shifts')}
          className="me-2"
        >
          Manage Shifts
        </Button>
        <Button
          variant={activeTab === 'logs' ? 'primary' : 'outline-primary'}
          onClick={() => setActiveTab('logs')}
        >
          Audit Logs
        </Button>
      </div>

      {activeTab === 'shifts' && (
        <div>
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
              onClick={() => setShowDefaultShiftsModal(true)}
            >
              Add Default Shift
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
                        const time = newShift.startTime?.split('T')[1] || '12:00';
                        // Auto-populate end date with same date if not already set
                        const currentEndDate = newShift.endTime?.split('T')[0];
                        if (!currentEndDate) {
                          setNewShift({ ...newShift, startTime: `${date}T${time}`, endTime: `${date}T${time}` });
                        } else {
                          setNewShift({ ...newShift, startTime: `${date}T${time}` });
                        }
                      }}
                      required
                      style={{ flex: 1 }}
                    />
                    <Form.Select
                      value={newShift.startTime?.split('T')[1]?.split(':')[0] || '12'}
                      onChange={(e) => {
                        const date = newShift.startTime?.split('T')[0] || new Date().toISOString().split('T')[0];
                        const minute = newShift.startTime?.split('T')[1]?.split(':')[1] || '00';
                        const ampm = newShift.startTime?.split('T')[1]?.split(':')[2] || 'AM';
                        const hour24 = convertTo24Hour(parseInt(e.target.value), ampm);
                        setNewShift({ ...newShift, startTime: `${date}T${String(hour24).padStart(2, '0')}:${minute}:${ampm}` });
                      }}
                      style={{ flex: 0.5 }}
                    >
                      {[...Array(12)].map((_, i) => (
                        <option key={i} value={String(i + 1).padStart(2, '0')}>
                          {String(i + 1).padStart(2, '0')}
                        </option>
                      ))}
                    </Form.Select>
                    <Form.Select
                      value={newShift.startTime?.split('T')[1]?.split(':')[1] || '00'}
                      onChange={(e) => {
                        const date = newShift.startTime?.split('T')[0] || new Date().toISOString().split('T')[0];
                        const hour = newShift.startTime?.split('T')[1]?.split(':')[0] || '12';
                        const ampm = newShift.startTime?.split('T')[1]?.split(':')[2] || 'AM';
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
                      value={newShift.startTime?.split('T')[1]?.split(':')[2] || 'AM'}
                      onChange={(e) => {
                        const date = newShift.startTime?.split('T')[0] || new Date().toISOString().split('T')[0];
                        const hour12 = newShift.startTime?.split('T')[1]?.split(':')[0] || '12';
                        const minute = newShift.startTime?.split('T')[1]?.split(':')[1] || '00';
                        const hour24 = convertTo24Hour(parseInt(hour12), e.target.value);
                        setNewShift({ ...newShift, startTime: `${date}T${String(hour24).padStart(2, '0')}:${minute}:${e.target.value}` });
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
                        const time = newShift.endTime?.split('T')[1] || '12:00';
                        setNewShift({ ...newShift, endTime: `${date}T${time}` });
                      }}
                      required
                      style={{ flex: 1 }}
                    />
                    <Form.Select
                      value={newShift.endTime?.split('T')[1]?.split(':')[0] || '12'}
                      onChange={(e) => {
                        const date = newShift.endTime?.split('T')[0] || new Date().toISOString().split('T')[0];
                        const minute = newShift.endTime?.split('T')[1]?.split(':')[1] || '00';
                        const ampm = newShift.endTime?.split('T')[1]?.split(':')[2] || 'AM';
                        const hour24 = convertTo24Hour(parseInt(e.target.value), ampm);
                        setNewShift({ ...newShift, endTime: `${date}T${String(hour24).padStart(2, '0')}:${minute}:${ampm}` });
                      }}
                      style={{ flex: 0.5 }}
                    >
                      {[...Array(12)].map((_, i) => (
                        <option key={i} value={String(i + 1).padStart(2, '0')}>
                          {String(i + 1).padStart(2, '0')}
                        </option>
                      ))}
                    </Form.Select>
                    <Form.Select
                      value={newShift.endTime?.split('T')[1]?.split(':')[1] || '00'}
                      onChange={(e) => {
                        const date = newShift.endTime?.split('T')[0] || new Date().toISOString().split('T')[0];
                        const hour = newShift.endTime?.split('T')[1]?.split(':')[0] || '12';
                        const ampm = newShift.endTime?.split('T')[1]?.split(':')[2] || 'AM';
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
                      value={newShift.endTime?.split('T')[1]?.split(':')[2] || 'AM'}
                      onChange={(e) => {
                        const date = newShift.endTime?.split('T')[0] || new Date().toISOString().split('T')[0];
                        const hour12 = newShift.endTime?.split('T')[1]?.split(':')[0] || '12';
                        const minute = newShift.endTime?.split('T')[1]?.split(':')[1] || '00';
                        const hour24 = convertTo24Hour(parseInt(hour12), e.target.value);
                        setNewShift({ ...newShift, endTime: `${date}T${String(hour24).padStart(2, '0')}:${minute}:${e.target.value}` });
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
                    If checked, this shift will be created for every {defaultShiftDate ? (() => {
                      const [year, month, day] = defaultShiftDate.split('-').map(Number);
                      const date = new Date(year, month - 1, day);
                      return date.toLocaleDateString('en-US', { weekday: 'long' });
                    })() : 'selected day'} in the month.
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

                          const previewName = defaultShiftName.trim()
                            ? `${defaultShiftName}`
                            : `${dayOfWeekName}`;

                          const selectedDayOfWeek = selectedDate.getDay();
                          const currentMonth = selectedDate.getMonth();
                          const currentYear = selectedDate.getFullYear();
                          let daysInMonth = 0;

                          if (populateMonthly) {
                            const lastDay = new Date(currentYear, currentMonth + 1, 0).getDate();
                            for (let dayNum = 1; dayNum <= lastDay; dayNum++) {
                              const date = new Date(currentYear, currentMonth, dayNum);
                              if (date.getDay() === selectedDayOfWeek) {
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
                    const selectedDate = new Date(defaultShiftDate);
                    const selectedDayOfWeek = selectedDate.getDay();
                    const currentMonth = selectedDate.getMonth();
                    const currentYear = selectedDate.getFullYear();
                    let count = 0;
                    const lastDay = new Date(currentYear, currentMonth + 1, 0).getDate();
                    for (let day = 1; day <= lastDay; day++) {
                      const date = new Date(currentYear, currentMonth, day);
                      if (date.getDay() === selectedDayOfWeek) count++;
                    }
                    return count;
                  })()})` : ''}
                </Button>
              </Form>
            </Modal.Body>
          </Modal>

          {/* Shifts Table */}
          <Table striped bordered hover responsive>
            <thead>
              <tr>
                <th>Name</th>
                <th>Start Time</th>
                <th>End Time</th>
                <th>Slots</th>
                <th>Filled</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {shifts.map((shift) => (
                <tr key={shift.id}>
                  <td>{shift.name}</td>
                  <td>{formatDateTime(shift.startTime)}</td>
                  <td>{formatDateTime(shift.endTime)}</td>
                  <td>{shift.availableSlots}</td>
                  <td>{shift.filledSlots}</td>
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
        </div>
      )}

      {activeTab === 'logs' && (
        <div>
          <h2 className="mb-4">Audit Logs</h2>
          <Table striped bordered hover size="sm" responsive>
            <thead>
              <tr>
                <th>Timestamp</th>
                <th>User</th>
                <th>Action</th>
                <th>Entity</th>
                <th>Description</th>
                <th>IP Address</th>
              </tr>
            </thead>
            <tbody>
              {auditLogs.map((log) => (
                <tr key={log.id}>
                  <td>{formatDateTime(log.actionAt)}</td>
                  <td>{log.user ? log.user.email : 'System'}</td>
                  <td>{log.action}</td>
                  <td>{log.entity}</td>
                  <td>{log.description}</td>
                  <td>{log.ipAddress}</td>
                </tr>
              ))}
            </tbody>
          </Table>
        </div>
      )}
    </Container>
  );
}

export default AdminDashboard;

