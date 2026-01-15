import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Button, Form, Spinner, Alert, Badge, Modal } from 'react-bootstrap';
import { shiftAPI } from '../api/client';
import { assignmentAPI } from '../api/client';
import { useAuthStore } from '../store/authStore';
import { format, getDaysInMonth, getMonth, getYear } from 'date-fns';

function ShiftCalendar() {
  const { user } = useAuthStore();
  const [shifts, setShifts] = useState([]);
  const [shiftsWithDetails, setShiftsWithDetails] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [selectedMonth, setSelectedMonth] = useState(new Date().getMonth());
  const [selectedYear, setSelectedYear] = useState(new Date().getFullYear());
  const [availableMonths, setAvailableMonths] = useState([]);

  // Signup modal states
  const [showSignupModal, setShowSignupModal] = useState(false);
  const [selectedShift, setSelectedShift] = useState(null);
  const [shiftDetails, setShiftDetails] = useState(null);
  const [loadingDetails, setLoadingDetails] = useState(false);

  // Fetch all shifts and determine available months
  useEffect(() => {
    fetchShifts();
  }, []);

  // Refetch shift details when shifts change to keep pending signups current
  useEffect(() => {
    if (shifts.length > 0 && Object.keys(shiftsWithDetails).length === 0) {
      // Fetch details for all shifts in parallel (much faster)
      const fetchAllDetails = async () => {
        const detailsMap = {};
        const detailPromises = shifts.map(shift =>
          shiftAPI.getShiftDetails(shift.id)
            .then(details => {
              detailsMap[shift.id] = details.data;
            })
            .catch(err => {
              console.error(`Failed to load details for shift ${shift.id}:`, err?.response?.status);
              detailsMap[shift.id] = { signups: [] };
            })
        );

        await Promise.all(detailPromises);
        setShiftsWithDetails(detailsMap);
      };

      fetchAllDetails();
    }
  }, [shifts]);

  // Update available months when shifts change
  useEffect(() => {
    const months = new Set();
    shifts.forEach(shift => {
      const shiftMonth = getMonth(new Date(shift.startTime));
      const shiftYear = getYear(new Date(shift.startTime));
      if (shiftYear === selectedYear) {
        months.add(shiftMonth);
      }
    });
    setAvailableMonths(Array.from(months).sort((a, b) => a - b));

    // If current selected month has no shifts, select first available month
    if (!months.has(selectedMonth) && months.size > 0) {
      setSelectedMonth(Math.min(...Array.from(months)));
    }
  }, [shifts, selectedYear]);

  const fetchShifts = async () => {
    try {
      setLoading(true);
      const res = await shiftAPI.getAvailableShifts(0, 1000);
      setShifts(res.data.content);
      setError('');
    } catch (err) {
      console.error('Failed to load shifts:', err);
      setError('Failed to load shifts.');
    } finally {
      setLoading(false);
    }
  };

  const fetchShiftDetails = async (shiftId) => {
    try {
      const details = await shiftAPI.getShiftDetails(shiftId);
      setShiftsWithDetails(prev => ({
        ...prev,
        [shiftId]: details.data
      }));
      return details.data;
    } catch (err) {
      console.error(`Failed to load details for shift ${shiftId}:`, err);
      return { signups: [] };
    }
  };

  const handleShiftClick = async (shift) => {
    setSelectedShift(shift);
    setShiftDetails(null); // Clear old details
    setLoadingDetails(true);
    setError(''); // Clear any old errors

    // Always fetch fresh details to ensure we have the latest signup information
    console.log(`Fetching fresh details for shift ${shift.id}`);
    try {
      const details = await shiftAPI.getShiftDetails(shift.id);
      console.log('Fresh shift details loaded:', details.data);
      setShiftDetails(details.data);
      // Also update cache
      setShiftsWithDetails(prev => ({
        ...prev,
        [shift.id]: details.data
      }));
    } catch (err) {
      console.error(`Failed to load details for shift ${shift.id}:`, err);
      setError('Failed to load shift details. Please try again.');
      setShiftDetails(null);
    } finally {
      setLoadingDetails(false);
      setShowSignupModal(true);
    }
  };

  const handleSignup = async () => {
    if (!selectedShift || !shiftDetails || !user || loadingDetails) return;

    // Ensure we have signups data
    if (!shiftDetails.signups) {
      setError('Unable to load signup information. Please try again.');
      return;
    }

    // FIRST: Check if user already has ANY signup (pending or accepted) for this shift
    // Convert to string for reliable comparison
    const userId = String(user.userId);
    const hasExistingSignup = shiftDetails.signups.some(s => String(s.userId) === userId);

    console.log('User ID:', userId);
    console.log('Shift signups:', shiftDetails.signups);
    console.log('Has existing signup:', hasExistingSignup);

    if (hasExistingSignup) {
      setError('You already have a signup for this shift.');
      console.log('User already has signup, blocking duplicate attempt');
      return;
    }

    // SECOND: Check if shift is full
    if (isShiftFull(selectedShift)) {
      setError('This shift is now full. No slots available.');
      return;
    }

    try {
      setError(''); // Clear any previous errors
      console.log(`Attempting signup for shift ${selectedShift.id} by user ${user.id}`);

      // THIRD: Attempt the signup
      await assignmentAPI.signupForShift(selectedShift.id);
      setSuccess('Successfully signed up for shift!');

      // FOURTH: Refresh shift details to show the new pending signup
      console.log('Signup successful, refreshing shift details...');
      const updatedDetails = await fetchShiftDetails(selectedShift.id);
      setShiftDetails(updatedDetails);
      console.log('Shift details updated:', updatedDetails);

      // FIFTH: Refresh shifts list
      try {
        const res = await shiftAPI.getAvailableShifts(0, 1000);
        setShifts(res.data.content);
      } catch (err) {
        console.error('Failed to refresh shifts:', err);
      }

      // SIXTH: Close the modal after successful signup
      setTimeout(() => {
        setShowSignupModal(false);
        setSelectedShift(null);
      }, 1500);

      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      console.error('Signup error:', err);
      if (err.response?.status === 401) {
        setError('Your session has expired. Please log in again.');
      } else if (err.response?.status === 400) {
        // Backend says user already signed up - refresh to show updated state
        console.log('Backend returned 400 - user may already be signed up');
        const updatedDetails = await fetchShiftDetails(selectedShift.id);
        setShiftDetails(updatedDetails);
        setError('You are already signed up for this shift.');
      } else if (err.response?.status === 403) {
        setError('You do not have permission to sign up for this shift.');
      } else {
        setError(err.response?.data?.message || 'Failed to sign up for shift. Please try again.');
      }
    }
  };

  const isShiftFull = (shift) => {
    return shift.filledSlots >= shift.availableSlots;
  };

  const getShiftBadgeColor = (shift, pendingCount) => {
    if (pendingCount > 0) {
      return 'warning'; // Yellow for pending signups
    } else if (isShiftFull(shift)) {
      return 'danger'; // Red for full
    } else {
      return 'primary'; // Blue for available
    }
  };

  const getDaysArray = () => {
    const daysInMonth = getDaysInMonth(new Date(selectedYear, selectedMonth, 1));
    const firstDay = new Date(selectedYear, selectedMonth, 1).getDay();
    const days = [];

    // Add empty cells for days before month starts
    for (let i = 0; i < firstDay; i++) {
      days.push(null);
    }

    // Add days of month
    for (let i = 1; i <= daysInMonth; i++) {
      days.push(i);
    }

    return days;
  };

  const getShiftsForDay = (day) => {
    if (!day) return [];

    return shifts.filter(shift => {
      const shiftDate = new Date(shift.startTime);
      return (
        getMonth(shiftDate) === selectedMonth &&
        getYear(shiftDate) === selectedYear &&
        shiftDate.getDate() === day
      );
    });
  };

  const monthNames = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ];

  const dayNames = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

  if (loading) {
    return (
      <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '100vh' }}>
        <Spinner animation="border" />
      </Container>
    );
  }

  const days = getDaysArray();

  return (
    <Container className="py-5">
      <h1 className="mb-4">Shift Calendar</h1>

      {error && <Alert variant="danger">{error}</Alert>}

      {availableMonths.length === 0 ? (
        <Alert variant="info">No shifts available for {selectedYear}.</Alert>
      ) : (
        <>
          <Row className="mb-4">
            <Col md={4} className="mb-3 mb-md-0">
              <Form.Group>
                <Form.Label>Select Month</Form.Label>
                <Form.Select
                  value={selectedMonth}
                  onChange={(e) => setSelectedMonth(parseInt(e.target.value))}
                >
                  {monthNames.map((month, index) => (
                    <option
                      key={index}
                      value={index}
                      disabled={!availableMonths.includes(index)}
                    >
                      {month}
                    </option>
                  ))}
                </Form.Select>
              </Form.Group>
            </Col>
            <Col md={4}>
              <Form.Group>
                <Form.Label>Year</Form.Label>
                <Form.Control
                  type="number"
                  value={selectedYear}
                  disabled
                  className="text-center"
                />
              </Form.Group>
            </Col>
          </Row>

          <Card>
            <Card.Header className="bg-primary text-white">
              <h4 className="mb-0">{monthNames[selectedMonth]} {selectedYear}</h4>
            </Card.Header>
            <Card.Body>
              {/* Day headers */}
              <div className="row text-center fw-bold mb-2" style={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)' }}>
                {dayNames.map(day => (
                  <div key={day} className="py-2 border-bottom">
                    {day}
                  </div>
                ))}
              </div>

              {/* Calendar days */}
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: '1px', backgroundColor: '#dee2e6' }}>
                {days.map((day, index) => {
                  const dayShifts = day ? getShiftsForDay(day) : [];

                  return (
                    <div
                      key={index}
                      style={{
                        minHeight: '150px',
                        backgroundColor: '#fff',
                        padding: '8px',
                        overflow: 'auto'
                      }}
                    >
                      {day && (
                        <>
                          <div className="fw-bold mb-2">{day}</div>
                          {dayShifts.length > 0 ? (
                            <div>
                              {dayShifts.map(shift => {
                                // Get pending count from cached shift details
                                const shiftDetails = shiftsWithDetails[shift.id];
                                const signups = shiftDetails?.signups || [];
                                const pendingCount = signups.filter(s => !s.accepted).length || 0;

                                // Determine badge color
                                let badgeColor = 'primary'; // blue - available
                                if (pendingCount > 0) {
                                  badgeColor = 'warning'; // yellow - pending signups
                                } else if (isShiftFull(shift)) {
                                  badgeColor = 'danger'; // red - full
                                }

                                return (
                                  <div key={shift.id} className="mb-2">
                                    <Badge
                                      bg={badgeColor}
                                      className="d-block text-wrap mb-1"
                                      style={{ cursor: 'pointer' }}
                                      onClick={() => handleShiftClick(shift)}
                                      title="Click to view details and sign up"
                                    >
                                      {shift.name}
                                    </Badge>
                                    <small className="text-muted d-block">
                                      {format(new Date(shift.startTime), 'h:mm a')} - {format(new Date(shift.endTime), 'h:mm a')}
                                    </small>
                                    <small className="text-secondary d-block">
                                      Slots: {shift.filledSlots}/{shift.availableSlots}
                                    </small>
                                    {pendingCount > 0 && (
                                      <small className="text-warning d-block">
                                        <strong>⏳ {pendingCount} pending</strong>
                                      </small>
                                    )}
                                  </div>
                                );
                              })}
                            </div>
                          ) : (
                            <small className="text-muted">No shifts</small>
                          )}
                        </>
                      )}
                    </div>
                  );
                })}
              </div>
            </Card.Body>
          </Card>

          <Row className="mt-4">
            <Col>
              <Card className="bg-light">
                <Card.Body>
                  <h6 className="mb-3">Legend:</h6>
                  <div className="mb-2">
                    <Badge bg="primary">Available Shift</Badge>
                    <span className="ms-2">- Has open slots</span>
                  </div>
                  <div className="mb-2">
                    <Badge bg="warning">Pending Signups</Badge>
                    <span className="ms-2">- Has pending approvals</span>
                  </div>
                  <div className="mb-2">
                    <Badge bg="danger">Full Shift</Badge>
                    <span className="ms-2">- No slots available</span>
                  </div>
                  <small className="text-muted">
                    Click on a shift name to view details and sign up (unless you already have a pending signup for it)
                  </small>
                </Card.Body>
              </Card>
            </Col>
          </Row>

          {/* Signup Modal */}
          <Modal show={showSignupModal} onHide={() => {
            setShowSignupModal(false);
            setSelectedShift(null);
            setShiftDetails(null);
            setError('');
          }} size="lg">
            <Modal.Header closeButton>
              <Modal.Title>Sign Up for Shift</Modal.Title>
            </Modal.Header>
            <Modal.Body>
              {loadingDetails ? (
                <Spinner animation="border" className="d-block mx-auto" />
              ) : selectedShift ? (
                <>
                  {error && <Alert variant="danger">{error}</Alert>}
                  {success && <Alert variant="success">{success}</Alert>}

                  <p><strong>Shift:</strong> {selectedShift.name}</p>
                  <p><strong>Date:</strong> {format(new Date(selectedShift.startTime), 'EEEE, MMMM dd, yyyy')}</p>
                  <p><strong>Time:</strong> {format(new Date(selectedShift.startTime), 'h:mm a')} - {format(new Date(selectedShift.endTime), 'h:mm a')}</p>
                  <p><strong>Description:</strong> {selectedShift.description || 'No description provided'}</p>
                  <p><strong>Available Slots:</strong> {selectedShift.availableSlots - selectedShift.filledSlots} of {selectedShift.availableSlots}</p>

                  {shiftDetails && (
                    <>
                      <p><strong>Pending Signups:</strong> {shiftDetails.signups?.filter(s => !s.accepted).length || 0}</p>

                      {shiftDetails.signups && shiftDetails.signups.filter(s => !s.accepted).length > 0 && (
                        <div className="mt-3 p-3 bg-light rounded">
                          <h6>Pending Approvals:</h6>
                          <ul className="mb-0">
                            {shiftDetails.signups
                              .filter(s => !s.accepted)
                              .map(signup => (
                                <li key={signup.id}>
                                  {signup.userFirstName && signup.userLastName
                                    ? `${signup.userFirstName} ${signup.userLastName}`
                                    : signup.userEmail || 'Unknown User'}
                                </li>
                              ))}
                          </ul>
                        </div>
                      )}

                      {shiftDetails.signups && shiftDetails.signups.filter(s => s.accepted).length > 0 && (
                        <div className="mt-3 p-3 bg-success bg-opacity-10 rounded">
                          <h6>Approved Signups:</h6>
                          <ul className="mb-0">
                            {shiftDetails.signups
                              .filter(s => s.accepted)
                              .map(signup => (
                                <li key={signup.id}>
                                  {signup.userFirstName && signup.userLastName
                                    ? `${signup.userFirstName} ${signup.userLastName}`
                                    : signup.userEmail || 'Unknown User'}
                                </li>
                              ))}
                          </ul>
                        </div>
                      )}
                    </>
                  )}

                  {isShiftFull(selectedShift) && (
                    <Alert variant="warning">
                      This shift is currently full. No slots available.
                    </Alert>
                  )}
                </>
              ) : null}
            </Modal.Body>
            <Modal.Footer>
              <Button variant="secondary" onClick={() => {
                setShowSignupModal(false);
                setSelectedShift(null);
                setShiftDetails(null);
                setError('');
              }}>
                Cancel
              </Button>
              <Button
                variant="primary"
                onClick={handleSignup}
                disabled={
                  loadingDetails ||
                  !shiftDetails ||
                  !user ||
                  (selectedShift && isShiftFull(selectedShift)) ||
                  (shiftDetails && shiftDetails.signups && shiftDetails.signups.length > 0 && shiftDetails.signups.some(s => String(s.userId) === String(user.userId)))
                }
              >
                {loadingDetails ? 'Loading...' : (shiftDetails && shiftDetails.signups && shiftDetails.signups.length > 0 && shiftDetails.signups.some(s => String(s.userId) === String(user.userId))) ? 'Already Signed Up' : 'Sign Up for Shift'}
              </Button>
            </Modal.Footer>
          </Modal>
        </>
      )}
    </Container>
  );
}

export default ShiftCalendar;

