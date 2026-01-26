import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Button, Modal, Alert } from 'react-bootstrap';
import { shiftAPI, assignmentAPI } from '../api/client';
import { useAuthStore } from '../store/authStore';
import { useApiCall } from '../hooks/useApiCall';
import { LoadingSpinner, ErrorAlert, SuccessAlert } from '../components/CommonUI';
import { formatShiftTime, isTodayOrLater } from '../utils/dateUtils';

function EmployeeDashboard() {
  const [shifts, setShifts] = useState([]);
  const [userAssignments, setUserAssignments] = useState([]);
  const [signupSuccess, setSignupSuccess] = useState('');
  const [showCalendarModal, setShowCalendarModal] = useState(false);
  const { user } = useAuthStore();
  const { loading, error, execute, clearError } = useApiCall();

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    await execute(
      async () => {
        const [shiftsRes, assignmentsRes] = await Promise.all([
          shiftAPI.getAvailableShifts(0, 100),
          assignmentAPI.getUserAssignments(),
        ]);

        setShifts(shiftsRes.data.content);

        // Filter assignments to only include shifts that still exist
        const availableShiftIds = new Set(shiftsRes.data.content.map(s => s.id));
        const validAssignments = assignmentsRes.data.filter(a => availableShiftIds.has(a.shiftId));
        setUserAssignments(validAssignments);
      }
    );
  };

  const handleSignup = async (shiftId) => {
    await execute(
      () => assignmentAPI.signupForShift(shiftId),
      () => {
        setSignupSuccess('Successfully signed up for shift!');
        fetchData();
        setTimeout(() => setSignupSuccess(''), 3000);
      }
    );
  };

  const handleDeleteSignup = async (assignmentId) => {
    if (!window.confirm('Are you sure you want to cancel this signup request?')) return;

    await execute(
      () => assignmentAPI.rejectSignup(assignmentId),
      () => {
        setSignupSuccess('Signup request cancelled successfully!');
        fetchData();
        setTimeout(() => setSignupSuccess(''), 3000);
      }
    );
  };

  const isUserSignedUp = (shiftId) => {
    return userAssignments.some((a) => a.shiftId === shiftId);
  };

  const getUpcomingApprovedShifts = () => {
    const approvedAssignments = userAssignments.filter(a => a.accepted);

    return approvedAssignments
      .map(assignment => {
        const shift = shifts.find(s => s.id === assignment.shiftId);
        return shift ? { ...shift, assignmentId: assignment.id } : null;
      })
      .filter(shift => shift && isTodayOrLater(shift.startTime))
      .sort((a, b) => new Date(a.startTime) - new Date(b.startTime));
  };

  const getPendingSignups = () => {
    const pendingAssignments = userAssignments.filter(a => !a.accepted);

    return pendingAssignments
      .map(assignment => {
        const shift = shifts.find(s => s.id === assignment.shiftId);
        return shift ? { ...shift, assignmentId: assignment.id } : null;
      })
      .filter(Boolean)
      .sort((a, b) => new Date(a.startTime) - new Date(b.startTime));
  };

  const getAvailableShifts = () => {
    return shifts
      .filter(shift => {
        const hasAvailableSlots = shift.availableSlots > shift.filledSlots;
        const userHasSignup = isUserSignedUp(shift.id);
        const isUpcoming = isTodayOrLater(shift.startTime);
        return hasAvailableSlots && !userHasSignup && isUpcoming;
      })
      .sort((a, b) => new Date(a.startTime) - new Date(b.startTime));
  };

  // Calendar subscription functions
  const getCalendarSubscriptionUrl = () => {
    const token = localStorage.getItem('token');
    const baseUrl = 'http://localhost:8080/api/calendar/user/my-shifts.ics';
    return `${baseUrl}?token=${encodeURIComponent(token)}`;
  };

  const handleCopyCalendarUrl = async () => {
    try {
      const url = getCalendarSubscriptionUrl();
      await navigator.clipboard.writeText(url);
      setSignupSuccess('Calendar subscription URL copied to clipboard!');
      setTimeout(() => setSignupSuccess(''), 3000);
    } catch (err) {
      // Clipboard API may not be available in all browsers
      setSignupSuccess('Failed to copy URL. Please copy it manually.');
      setTimeout(() => setSignupSuccess(''), 3000);
    }
  };

  const handleDownloadICS = () => {
    const token = localStorage.getItem('token');
    const url = `http://localhost:8080/api/calendar/user/my-shifts.ics?token=${encodeURIComponent(token)}`;

    // Open the URL in a new window to trigger download
    window.open(url, '_blank');
  };


  if (loading) return <LoadingSpinner />;

  return (
    <Container className="mt-4">
      <h2>Welcome, {user?.firstName}!</h2>

      <ErrorAlert error={error} onDismiss={clearError} />
      <SuccessAlert message={signupSuccess} onDismiss={() => setSignupSuccess('')} />

      {/* Calendar Subscription Button */}
      <div className="mb-3 text-end">
        <Button
          variant="outline-info"
          onClick={() => setShowCalendarModal(true)}
          className="me-2"
        >
          📅 Add Shifts to Calendar
        </Button>
      </div>

      <Row>
        <Col md={6}>
          <Card className="mb-4">
            <Card.Header>
              <h5>Your Upcoming Shifts</h5>
            </Card.Header>
            <Card.Body>
              {getUpcomingApprovedShifts().length === 0 ? (
                <p>No upcoming approved shifts.</p>
              ) : (
                getUpcomingApprovedShifts().map((shift) => (
                  <div key={shift.id} className="border-bottom pb-2 mb-2">
                    <strong>{shift.name}</strong>
                    <br />
                    <small className="text-muted">{formatShiftTime(shift.startTime, shift.endTime)}</small>
                    <br />
                    <small>{shift.description}</small>
                  </div>
                ))
              )}
            </Card.Body>
          </Card>

          <Card>
            <Card.Header>
              <h5>Pending Signup Requests</h5>
            </Card.Header>
            <Card.Body>
              {getPendingSignups().length === 0 ? (
                <p>No pending signup requests.</p>
              ) : (
                getPendingSignups().map((shift) => (
                  <div key={shift.id} className="border-bottom pb-2 mb-2 d-flex justify-content-between align-items-center">
                    <div>
                      <strong>{shift.name}</strong>
                      <br />
                      <small className="text-muted">{formatShiftTime(shift.startTime, shift.endTime)}</small>
                      <br />
                      <small>{shift.description}</small>
                    </div>
                    <Button
                      variant="outline-danger"
                      size="sm"
                      onClick={() => handleDeleteSignup(shift.assignmentId)}
                      disabled={loading}
                    >
                      Cancel
                    </Button>
                  </div>
                ))
              )}
            </Card.Body>
          </Card>
        </Col>

        <Col md={6}>
          <Card>
            <Card.Header>
              <h5>Available Shifts</h5>
            </Card.Header>
            <Card.Body style={{ maxHeight: '600px', overflowY: 'auto' }}>
              {getAvailableShifts().length === 0 ? (
                <p>No available shifts at the moment.</p>
              ) : (
                getAvailableShifts().map((shift) => (
                  <div key={shift.id} className="border-bottom pb-2 mb-2">
                    <div className="d-flex justify-content-between align-items-start">
                      <div>
                        <strong>{shift.name}</strong>
                        <br />
                        <small className="text-muted">{formatShiftTime(shift.startTime, shift.endTime)}</small>
                        <br />
                        <small>{shift.description}</small>
                        <br />
                        <small className="text-info">
                          {shift.filledSlots}/{shift.availableSlots} slots filled
                        </small>
                      </div>
                      <Button
                        variant="primary"
                        size="sm"
                        onClick={() => handleSignup(shift.id)}
                        disabled={loading}
                      >
                        Sign Up
                      </Button>
                    </div>
                  </div>
                ))
              )}
            </Card.Body>
          </Card>
        </Col>
      </Row>

      {/* Calendar Subscription Modal */}
      <Modal show={showCalendarModal} onHide={() => setShowCalendarModal(false)} size="lg">
        <Modal.Header closeButton>
          <Modal.Title>📅 Add Your Shifts to Calendar</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Alert variant="info">
            <strong>Auto-Sync Calendar Subscription</strong><br />
            Subscribe to your shift calendar to automatically receive updates when shifts are added, changed, or removed.
          </Alert>

          <div className="mb-4">
            <h5>📱 For Mobile (iPhone/Android):</h5>
            <ol>
              <li>Copy the calendar URL below</li>
              <li>Open your phone's Calendar app</li>
              <li>Look for "Add Calendar" or "Subscribe to Calendar"</li>
              <li>Paste the URL and save</li>
            </ol>

            <div className="d-grid gap-2 mt-3">
              <Button
                variant="primary"
                onClick={handleCopyCalendarUrl}
                className="mb-2"
              >
                📋 Copy Calendar Subscription URL
              </Button>
            </div>
          </div>

          <div className="mb-4">
            <h5>💻 For Desktop:</h5>

            <div className="row">
              <div className="col-md-6">
                <h6>Google Calendar:</h6>
                <ol>
                  <li>Open <a href="https://calendar.google.com" target="_blank" rel="noopener noreferrer">Google Calendar</a></li>
                  <li>Click the "+" next to "Other calendars"</li>
                  <li>Select "From URL"</li>
                  <li>Paste the URL from the button above</li>
                </ol>
              </div>

              <div className="col-md-6">
                <h6>Apple Calendar:</h6>
                <ol>
                  <li>Open Calendar app</li>
                  <li>File → New Calendar Subscription</li>
                  <li>Paste the URL from the button above</li>
                  <li>Choose refresh frequency (recommended: Every hour)</li>
                </ol>
              </div>
            </div>
          </div>

          <div className="mb-3">
            <h6>🔄 Automatic Updates:</h6>
            <ul>
              <li>✅ New approved shifts will appear automatically</li>
              <li>✅ Cancelled or rejected shifts will be removed</li>
              <li>✅ Shift time changes will update</li>
              <li>⏰ 30-minute reminder notifications included</li>
            </ul>
          </div>

          <Alert variant="warning">
            <strong>One-time Download Option:</strong><br />
            If you prefer a one-time import (won't auto-update), you can download the calendar file directly.
          </Alert>

          <div className="d-grid gap-2">
            <Button
              variant="outline-secondary"
              onClick={handleDownloadICS}
            >
              📥 Download Calendar File (One-time)
            </Button>
          </div>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowCalendarModal(false)}>
            Close
          </Button>
        </Modal.Footer>
      </Modal>
    </Container>
  );
}

export default EmployeeDashboard;

