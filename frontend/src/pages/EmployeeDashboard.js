import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Button, Spinner, Alert } from 'react-bootstrap';
import { shiftAPI, assignmentAPI } from '../api/client';
import { useAuthStore } from '../store/authStore';
import { format } from 'date-fns';

function EmployeeDashboard() {
  const [shifts, setShifts] = useState([]);
  const [userAssignments, setUserAssignments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [signupSuccess, setSignupSuccess] = useState('');
  const { user } = useAuthStore();

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [shiftsRes, assignmentsRes] = await Promise.all([
        shiftAPI.getUpcomingShifts(0, 20),
        assignmentAPI.getUserAssignments(),
      ]);
      setShifts(shiftsRes.data.content);
      setUserAssignments(assignmentsRes.data);
    } catch (err) {
      setError('Failed to load shifts. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleSignup = async (shiftId) => {
    try {
      await assignmentAPI.signupForShift(shiftId);
      setSignupSuccess('Successfully signed up for shift!');
      fetchData();
      setTimeout(() => setSignupSuccess(''), 3000);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to sign up for shift.');
    }
  };

  const isUserSignedUp = (shiftId) => {
    return userAssignments.some((a) => a.shiftId === shiftId);
  };

  if (loading) {
    return (
      <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '100vh' }}>
        <Spinner animation="border" />
      </Container>
    );
  }

  return (
    <Container className="py-5">
      <h1 className="mb-4">Welcome, {user?.firstName}!</h1>

      {error && <Alert variant="danger">{error}</Alert>}
      {signupSuccess && <Alert variant="success">{signupSuccess}</Alert>}

      <Row className="mb-5">
        <Col md={6}>
          <Card>
            <Card.Header>Your Shifts</Card.Header>
            <Card.Body>
              {userAssignments.length === 0 ? (
                <p>You haven't signed up for any shifts yet.</p>
              ) : (
                <ul>
                  {userAssignments.map((assignment) => (
                    <li key={assignment.id}>
                      <strong>{assignment.shiftName}</strong>
                      <br />
                      Status: {assignment.accepted ? '✓ Accepted' : '⏳ Pending'}
                    </li>
                  ))}
                </ul>
              )}
            </Card.Body>
          </Card>
        </Col>
      </Row>

      <h2 className="mb-4">Available Shifts</h2>
      <Row>
        {shifts.map((shift) => (
          <Col md={6} lg={4} key={shift.id} className="mb-4">
            <Card>
              <Card.Body>
                <Card.Title>{shift.name}</Card.Title>
                <Card.Text>
                  <strong>Date & Time:</strong>
                  <br />
                  Start: {format(new Date(shift.startTime), 'MMM dd, yyyy HH:mm')}
                  <br />
                  End: {format(new Date(shift.endTime), 'MMM dd, yyyy HH:mm')}
                  <br />
                  <br />
                  <strong>Available Slots:</strong> {shift.availableSlots - shift.filledSlots} / {shift.availableSlots}
                </Card.Text>
                {shift.description && (
                  <Card.Text>
                    <strong>Description:</strong> {shift.description}
                  </Card.Text>
                )}
                <Button
                  variant="primary"
                  onClick={() => handleSignup(shift.id)}
                  disabled={isUserSignedUp(shift.id) || shift.availableSlots === shift.filledSlots}
                >
                  {isUserSignedUp(shift.id) ? 'Already Signed Up' : 'Sign Up'}
                </Button>
              </Card.Body>
            </Card>
          </Col>
        ))}
      </Row>
    </Container>
  );
}

export default EmployeeDashboard;

