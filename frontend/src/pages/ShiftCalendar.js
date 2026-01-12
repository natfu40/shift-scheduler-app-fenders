import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Button, Form, Spinner, Alert, Badge } from 'react-bootstrap';
import { shiftAPI } from '../api/client';
import { useAuthStore } from '../store/authStore';
import { format, getDaysInMonth, getMonth, getYear } from 'date-fns';

function ShiftCalendar() {
  const { user } = useAuthStore();
  const [shifts, setShifts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedMonth, setSelectedMonth] = useState(new Date().getMonth());
  const [selectedYear, setSelectedYear] = useState(new Date().getFullYear());
  const [availableMonths, setAvailableMonths] = useState([]);

  // Fetch all shifts and determine available months
  useEffect(() => {
    fetchShifts();
  }, []);

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
    } catch (err) {
      setError('Failed to load shifts.');
    } finally {
      setLoading(false);
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
        shiftDate.getDate() === day &&
        shift.active
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
                              {dayShifts.map(shift => (
                                <div key={shift.id} className="mb-2">
                                  <Badge bg="primary" className="d-block text-wrap mb-1">
                                    {shift.name}
                                  </Badge>
                                  <small className="text-muted d-block">
                                    {format(new Date(shift.startTime), 'h:mm a')} - {format(new Date(shift.endTime), 'h:mm a')}
                                  </small>
                                  <small className="text-secondary d-block">
                                    Slots: {shift.filledSlots}/{shift.availableSlots}
                                  </small>
                                </div>
                              ))}
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
                    <Badge bg="primary">Shift Name</Badge>
                    <span className="ms-2">- Available shift</span>
                  </div>
                  <small className="text-muted">
                    Click on a shift name to view more details and sign up
                  </small>
                </Card.Body>
              </Card>
            </Col>
          </Row>
        </>
      )}
    </Container>
  );
}

export default ShiftCalendar;

