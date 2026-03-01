import React, { useState, useEffect, useCallback } from 'react';
import { Container, Row, Col, Card, Button, Form, Table, Alert } from 'react-bootstrap';
import { useAuthStore } from '../store/authStore';
import { useNavigate } from 'react-router-dom';
import { format } from 'date-fns';
import { API_CONFIG } from '../constants/apiConstants';

function Audits() {
  const { user, isAdmin } = useAuthStore();
  const navigate = useNavigate();

  const [auditLogs, setAuditLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Audit log filtering and grouping states
  const [auditFilters, setAuditFilters] = useState({
    user: '',
    userId: '', // New field for user dropdown filter
    action: '',
    entity: '',
    startDate: '',
    endDate: '',
    description: ''
  });
  const [auditGroupBy, setAuditGroupBy] = useState(''); // '', 'user', 'action', 'entity'
  const [auditPageSize, setAuditPageSize] = useState(100);
  const [auditPage, setAuditPage] = useState(0);
  const [auditTotalPages, setAuditTotalPages] = useState(0);
  const [availableUsers, setAvailableUsers] = useState([]); // New state for user dropdown

  const applyClientSideFilters = useCallback((logs) => {
    console.log('Applying filters:', auditFilters);
    console.log('Total logs before filtering:', logs.length);

    // Check if we actually have any filters set
    const hasDateFilter = auditFilters.startDate === 'LAST_24H' ||
                         (auditFilters.startDate && auditFilters.startDate !== '') ||
                         (auditFilters.endDate && auditFilters.endDate !== '');
    const hasOtherFilters = auditFilters.user || auditFilters.userId || auditFilters.action || auditFilters.entity || auditFilters.description;

    // If no filters are set, return all logs
    if (!hasDateFilter && !hasOtherFilters) {
      console.log('No filters set - returning all logs');
      return logs;
    }

    const filtered = logs.filter(log => {
      // Date range filter
      if (hasDateFilter) {
        // Parse the log timestamp using consistent helper
        let logDate = parseLogDate(log.actionAt);

        // If date parsing failed, include this log (don't filter out)
        if (isNaN(logDate.getTime()) || logDate.getTime() === 0) {
          console.warn('Invalid date found in log:', log.actionAt);
          return true; // Include logs with invalid dates rather than excluding them
        }

        // Handle special "Last 24h" case
        if (auditFilters.startDate === 'LAST_24H') {
          const now = new Date();
          const twentyFourHoursAgo = new Date(now.getTime() - (24 * 60 * 60 * 1000));
          const hoursDiff = (now.getTime() - logDate.getTime()) / (1000 * 60 * 60);
          const isWithin24h = logDate >= twentyFourHoursAgo;

          console.log(`24h filter - Log ${log.id} (${log.action}): ${logDate.toISOString()} - ${hoursDiff.toFixed(1)}h ago - ${isWithin24h ? 'INCLUDED ✅' : 'FILTERED OUT ❌'}`);

          if (logDate < twentyFourHoursAgo) {
            return false;
          }
        } else {
          // Handle regular date filtering
          if (auditFilters.startDate && auditFilters.startDate !== '') {
            const startDate = new Date(auditFilters.startDate);
            startDate.setHours(0, 0, 0, 0); // Start of day
            console.log('Comparing log date:', logDate.toISOString(), 'with start date:', startDate.toISOString());
            if (logDate < startDate) {
              console.log('Log filtered out - before start date');
              return false;
            }
          }

          if (auditFilters.endDate && auditFilters.endDate !== '') {
            let endDate;
            if (auditFilters.endDate === 'NOW') {
              // Use current date/time for "NOW" marker
              endDate = new Date();
              console.log('Using NOW as end date:', endDate.toISOString());
            } else {
              // Use end of selected date
              endDate = new Date(auditFilters.endDate);
              endDate.setHours(23, 59, 59, 999); // End of day
            }

            console.log('Comparing log date:', logDate.toISOString(), 'with end date:', endDate.toISOString());
            if (logDate > endDate) {
              console.log('Log filtered out - after end date');
              return false;
            }
          }
        }
      }

      // User filter (text search)
      if (auditFilters.user) {
        const userText = log.user ?
          `${log.user.firstName} ${log.user.lastName} ${log.user.email}`.toLowerCase() :
          'system';
        if (!userText.includes(auditFilters.user.toLowerCase())) return false;
      }

      // User ID filter (dropdown selection)
      if (auditFilters.userId) {
        if (!log.user || String(log.user.id) !== String(auditFilters.userId)) {
          return false;
        }
      }

      // Action filter
      if (auditFilters.action) {
        if (log.action !== auditFilters.action) return false;
      }

      // Entity filter
      if (auditFilters.entity) {
        if (log.entity !== auditFilters.entity) return false;
      }

      // Description filter
      if (auditFilters.description) {
        if (!log.description?.toLowerCase().includes(auditFilters.description.toLowerCase())) return false;
      }

      return true;
    });

    console.log('Filtered logs count:', filtered.length);
    return filtered;
  }, [auditFilters]);

  const fetchAuditLogs = useCallback(async () => {
    try {
      setLoading(true);

      console.log(`Fetching logs: page=${auditPage}, size=${auditPageSize}`);

      // Remove backend sorting and fetch larger batch on first page to ensure we get recent logs
      const fetchSize = auditPage === 0 ? Math.max(auditPageSize * 5, 500) : auditPageSize;
      const response = await fetch(`${API_CONFIG.BASE_URL}/audit-logs?page=${auditPage}&size=${fetchSize}`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        }
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      let logs = data.content || [];

      console.log('Raw logs from API:', logs.slice(0, 5).map(log => ({
        id: log.id,
        action: log.action,
        actionAt: log.actionAt
      })));

      // Look for Jan 14th logs
      const jan14Logs = logs.filter(log => {
        const dateStr = log.actionAt;
        const parsedDate = parseLogDate(log.actionAt);
        const isJan14String = dateStr && dateStr.includes('2026-01-14');
        const isJan14Parsed = parsedDate.getFullYear() === 2026 &&
                             parsedDate.getMonth() === 0 && // January is month 0
                             parsedDate.getDate() === 14;
        return isJan14String || isJan14Parsed;
      });

      console.log(`🔍 Found ${jan14Logs.length} Jan 14th logs:`, jan14Logs.map(log => ({
        id: log.id,
        action: log.action,
        actionAt: log.actionAt
      })));

      // Apply client-side filtering only if filters are set
      const hasFilters = auditFilters.startDate === 'LAST_24H' ||
                        auditFilters.endDate === 'NOW' ||
                        auditFilters.user ||
                        auditFilters.userId ||
                        auditFilters.action ||
                        auditFilters.entity ||
                        auditFilters.description ||
                        (auditFilters.startDate && auditFilters.startDate !== '') ||
                        (auditFilters.endDate && auditFilters.endDate !== '');

      if (hasFilters) {
        console.log('🔄 Applying client-side filtering...');
        logs = applyClientSideFilters(logs);
      }

      // Sort by date to ensure newest first (backend sorting is not reliable)
      logs.sort((a, b) => {
        const dateA = parseLogDate(a.actionAt);
        const dateB = parseLogDate(b.actionAt);
        return dateB.getTime() - dateA.getTime(); // Descending order (newest first)
      });

      // If we're on first page and fetched extra logs, slice back to page size
      if (auditPage === 0 && logs.length > auditPageSize) {
        console.log(`Slicing ${logs.length} logs back to ${auditPageSize} for first page display`);
        logs = logs.slice(0, auditPageSize);
      }

      console.log('Final logs (first 3):', logs.slice(0, 3).map(log => ({
        id: log.id,
        action: log.action,
        actionAt: log.actionAt,
        parsedDate: parseLogDate(log.actionAt).toISOString()
      })));

      if (logs.length > 0) {
        const newestLogTime = parseLogDate(logs[0].actionAt);
        console.log('🕐 Newest log available:', newestLogTime.toISOString());
      }

      // Extract unique users for dropdown filter
      const uniqueUsers = logs.reduce((users, log) => {
        if (log.user) {
          if (!users.find(u => u.id === log.user.id)) {
            users.push({
              id: log.user.id,
              firstName: log.user.firstName,
              lastName: log.user.lastName,
              email: log.user.email,
              fullName: `${log.user.firstName} ${log.user.lastName}`
            });
          }
        }
        return users;
      }, []);

      uniqueUsers.sort((a, b) => a.fullName.localeCompare(b.fullName));

      // Merge with existing users from previous pages
      setAvailableUsers(prevUsers => {
        const allUsers = [...prevUsers];
        uniqueUsers.forEach(newUser => {
          if (!allUsers.find(u => u.id === newUser.id)) {
            allUsers.push(newUser);
          }
        });
        return allUsers.sort((a, b) => a.fullName.localeCompare(b.fullName));
      });


      setAuditLogs(logs);
      setAuditTotalPages(data.totalPages || 0);

    } catch (err) {
      console.error('Error fetching audit logs:', err);
      if (err.response?.status === 401 || err.status === 401) {
        setError('Authentication failed. Please log in again.');
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        navigate('/login');
      } else if (err.response?.status === 403 || err.status === 403) {
        setError('You do not have permission to access audit logs.');
      } else {
        setError('Failed to load audit logs.');
      }
    } finally {
      setLoading(false);
    }
  }, [auditPage, auditPageSize, auditFilters, navigate, applyClientSideFilters]);

  useEffect(() => {
    // Immediately redirect if not admin
    if (!isAdmin) {
      navigate('/');
      return;
    }
  }, [user, isAdmin, navigate]);

  useEffect(() => {
    if (isAdmin) {
      fetchAuditLogs();
    }
  }, [auditPage, auditPageSize, isAdmin, fetchAuditLogs]);

  // Separate useEffect for filter changes to avoid infinite loops
  useEffect(() => {
    if (isAdmin) {
      setAuditPage(0); // Reset to first page whenever filters change
      fetchAuditLogs();
    }
  }, [auditFilters, isAdmin, fetchAuditLogs]);


  // Helper functions for audit logs
  const handleAuditFilterChange = (field, value) => {
    setAuditFilters(prev => ({ ...prev, [field]: value }));
    setAuditPage(0); // Reset to first page when filtering
  };

  const clearAuditFilters = () => {
    console.log('Clearing all filters and grouping');
    setAuditFilters({
      user: '',
      userId: '', // Clear the new userId filter
      action: '',
      entity: '',
      startDate: '',
      endDate: '',
      description: ''
    });
    setAuditGroupBy(''); // Reset grouping to "No Grouping"
    setAuditPage(0);
  };

  const setQuickDateFilter = (days) => {
    const now = new Date(); // Current date/time
    const startDate = new Date();

    console.log('Setting quick date filter for days:', days);
    console.log('Current time:', now.toISOString());

    if (days === 1) {
      // For "Last 24h", go back exactly 24 hours from now
      startDate.setTime(now.getTime() - (24 * 60 * 60 * 1000));

      console.log('Setting 24h filter:', {
        now: now.toISOString(),
        startDate: startDate.toISOString(),
        nowFormatted: now.toISOString().split('T')[0],
        startFormatted: startDate.toISOString().split('T')[0]
      });

      // For 24h filter, we'll use a special approach in the filter function
      setAuditFilters(prev => ({
        ...prev,
        startDate: 'LAST_24H', // Special marker
        endDate: ''
      }));
    } else {
      // For other periods, go back the specified number of days
      startDate.setDate(now.getDate() - days);
      startDate.setHours(0, 0, 0, 0); // Start of that day

      console.log('Setting date filter:', {
        days,
        startDate: startDate.toISOString(),
        endDate: now.toISOString(),
        startFormatted: startDate.toISOString().split('T')[0],
        endFormatted: 'NOW (current time)'
      });

      // For multi-day filters, use 'NOW' as end date to include logs up to current time
      setAuditFilters(prev => ({
        ...prev,
        startDate: startDate.toISOString().split('T')[0],
        endDate: 'NOW' // Special marker to indicate current time
      }));
    }

    setAuditPage(0);
    console.log('Quick filter set, should trigger fetchAuditLogs');
  };

  const getGroupedAuditLogs = () => {
    if (!auditGroupBy) return { 'All Logs': auditLogs };

    return auditLogs.reduce((groups, log) => {
      let groupKey;
      switch (auditGroupBy) {
        case 'user':
          groupKey = log.user ? `${log.user.firstName} ${log.user.lastName} (${log.user.email})` : 'System';
          break;
        case 'action':
          groupKey = log.action;
          break;
        case 'entity':
          groupKey = log.entity;
          break;
        default:
          groupKey = 'All Logs';
      }

      if (!groups[groupKey]) {
        groups[groupKey] = [];
      }
      groups[groupKey].push(log);
      return groups;
    }, {});
  };

  const formatDateTime = (dateString) => {
    try {
      // Use consistent date parsing
      const date = parseLogDate(dateString);

      // Check if date is valid
      if (isNaN(date.getTime()) || date.getTime() === 0) {
        return dateString;
      }

      return format(date, 'MMM dd, yyyy h:mm a');
    } catch (error) {
      console.error('Error formatting date:', error, dateString);
      return dateString;
    }
  };

  // Helper function to consistently parse log dates
  const parseLogDate = (dateString) => {
    if (!dateString) {
      console.warn('Empty date string provided to parseLogDate');
      return new Date(0);
    }

    try {
      let parsedDate;

      // Convert to string if not already
      const dateStr = String(dateString);

      // Try different parsing approaches
      if (dateStr.includes('T')) {
        // ISO-like format
        parsedDate = new Date(dateStr);

        // If that didn't work and there's no timezone, try adding UTC
        if (isNaN(parsedDate.getTime()) && !dateStr.endsWith('Z') && !dateStr.includes('+') && !dateStr.includes('-', 10)) {
          parsedDate = new Date(dateStr + 'Z');
        }
      } else {
        // Simple date format
        parsedDate = new Date(dateStr);
      }

      // Final validation
      if (isNaN(parsedDate.getTime())) {
        console.warn('Failed to parse date:', dateStr, '- using epoch time');
        return new Date(0);
      }

      return parsedDate;
    } catch (e) {
      console.error('Exception parsing date:', dateString, e);
      return new Date(0);
    }
  };

  if (!user) {
    return (
      <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '100vh' }}>
        <Alert variant="warning">Please log in to access audit logs.</Alert>
      </Container>
    );
  }

  if (!isAdmin) {
    return (
      <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '100vh' }}>
        <Alert variant="danger">You do not have admin privileges to access audit logs.</Alert>
      </Container>
    );
  }

  return (
    <Container className="py-5">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h1>Audit Logs</h1>
        <div className="d-flex align-items-center gap-2">
          <Button
            variant="outline-primary"
            size="sm"
            onClick={() => {
              console.log('Refreshing audit logs...');
              fetchAuditLogs();
            }}
          >
            🔄 Refresh
          </Button>
          <span className="text-muted">
            Showing {auditLogs.length} logs
            {(auditFilters.startDate || auditFilters.endDate || auditFilters.user || auditFilters.userId || auditFilters.action || auditFilters.entity || auditFilters.description) && ' (filtered)'}
          </span>
          <Form.Select
            size="sm"
            value={auditPageSize}
            onChange={(e) => {
              setAuditPageSize(parseInt(e.target.value));
              setAuditPage(0);
            }}
            style={{ width: 'auto' }}
          >
            <option value={50}>50 per page</option>
            <option value={100}>100 per page</option>
            <option value={200}>200 per page</option>
          </Form.Select>
        </div>
      </div>

      {error && <Alert variant="danger" onClose={() => setError('')} dismissible>{error}</Alert>}

      {/* Filters and Controls */}
      <Card className="mb-4">
        <Card.Header>
          <h5>Filters & Options</h5>
        </Card.Header>
        <Card.Body>
          <Row className="g-3">
            {/* Date Range Filters */}
            <Col md={6}>
              <h6>Time Period</h6>
              <div className="d-flex gap-2 mb-2">
                <Button size="sm" variant="outline-primary" onClick={() => setQuickDateFilter(1)}>
                  Last 24h
                </Button>
                <Button size="sm" variant="outline-primary" onClick={() => setQuickDateFilter(7)}>
                  Last 7 days
                </Button>
                <Button size="sm" variant="outline-primary" onClick={() => setQuickDateFilter(30)}>
                  Last 30 days
                </Button>
                <Button size="sm" variant="outline-primary" onClick={() => setQuickDateFilter(90)}>
                  Last 90 days
                </Button>
              </div>
              <div className="d-flex gap-2">
                <Form.Control
                  type="date"
                  size="sm"
                  placeholder="Start Date"
                  value={auditFilters.startDate === 'LAST_24H' ? '' : auditFilters.startDate}
                  onChange={(e) => handleAuditFilterChange('startDate', e.target.value)}
                />
                <Form.Control
                  type="date"
                  size="sm"
                  placeholder="End Date"
                  value={auditFilters.endDate === 'NOW' ? '' : auditFilters.endDate}
                  onChange={(e) => handleAuditFilterChange('endDate', e.target.value)}
                />
              </div>
              {auditFilters.startDate === 'LAST_24H' && (
                <small className="text-info mt-1 d-block">
                  📅 Currently showing: Last 24 hours
                </small>
              )}
              {auditFilters.endDate === 'NOW' && auditFilters.startDate !== 'LAST_24H' && (
                <small className="text-info mt-1 d-block">
                  📅 Currently showing: {auditFilters.startDate} to now (current time)
                </small>
              )}
            </Col>

            {/* Column Filters */}
            <Col md={6}>
              <h6>Column Filters</h6>
              <div className="d-flex gap-2 mb-2">
                <Form.Select
                  size="sm"
                  value={auditFilters.userId}
                  onChange={(e) => handleAuditFilterChange('userId', e.target.value)}
                  style={{ minWidth: '200px' }}
                >
                  <option value="">All Users</option>
                  {availableUsers.map(user => (
                    <option key={user.id} value={user.id}>
                      {user.fullName} ({user.email})
                    </option>
                  ))}
                </Form.Select>
                <Form.Select
                  size="sm"
                  value={auditFilters.action}
                  onChange={(e) => handleAuditFilterChange('action', e.target.value)}
                >
                  <option value="">All Actions</option>
                  <option value="CREATE_SHIFT">Create Shift</option>
                  <option value="UPDATE_SHIFT">Update Shift</option>
                  <option value="DELETE_SHIFT">Delete Shift</option>
                  <option value="ACCEPT_SIGNUP">Accept Signup</option>
                  <option value="REJECT_SIGNUP">Reject Signup</option>
                  <option value="SIGNUP_SHIFT">Signup Shift</option>
                  <option value="LOGIN">Login</option>
                  <option value="LOGOUT">Logout</option>
                </Form.Select>
              </div>
              <div className="d-flex gap-2">
                <Form.Control
                  size="sm"
                  placeholder="Search by user name/email..."
                  value={auditFilters.user}
                  onChange={(e) => handleAuditFilterChange('user', e.target.value)}
                />
                <Form.Select
                  size="sm"
                  value={auditFilters.entity}
                  onChange={(e) => handleAuditFilterChange('entity', e.target.value)}
                >
                  <option value="">All Entities</option>
                  <option value="Shift">Shift</option>
                  <option value="ShiftAssignment">Shift Assignment</option>
                  <option value="User">User</option>
                  <option value="UserRole">User Role</option>
                </Form.Select>
              </div>
              <div className="d-flex gap-2 mt-2">
                <Form.Control
                  size="sm"
                  placeholder="Filter description..."
                  value={auditFilters.description}
                  onChange={(e) => handleAuditFilterChange('description', e.target.value)}
                />
              </div>
            </Col>
          </Row>

          <Row className="mt-3">
            <Col md={6}>
              <h6>Group By</h6>
              <Form.Select
                size="sm"
                value={auditGroupBy}
                onChange={(e) => setAuditGroupBy(e.target.value)}
              >
                <option value="">No Grouping</option>
                <option value="user">Group by User</option>
                <option value="action">Group by Action</option>
                <option value="entity">Group by Entity</option>
              </Form.Select>
            </Col>
            <Col md={6} className="d-flex align-items-end">
              <Button variant="outline-secondary" size="sm" onClick={clearAuditFilters}>
                Clear All Filters
              </Button>
            </Col>
          </Row>
        </Card.Body>
      </Card>

      {/* Logs Display */}
      {loading && (
        <div className="text-center py-4">
          <div className="spinner-border" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
        </div>
      )}

      {!loading && (auditGroupBy ? (
        // Grouped display with scrollable content
        Object.entries(getGroupedAuditLogs()).map(([groupName, logs]) => (
          <Card key={groupName} className="mb-3">
            <Card.Header>
              <h6 className="mb-0">{groupName} ({logs.length} logs)</h6>
            </Card.Header>
            <Card.Body className="p-0">
              <div style={{ maxHeight: '400px', overflowY: 'auto' }}>
                <Table striped bordered hover size="sm" responsive className="mb-0">
                  <thead className="table-light sticky-top">
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
                    {logs.map((log) => (
                      <tr key={log.id}>
                        <td className="text-nowrap">{formatDateTime(log.actionAt)}</td>
                        <td className="text-nowrap">
                          {log.user ? (
                            <div>
                              <div>{log.user.firstName} {log.user.lastName}</div>
                              <small className="text-muted">{log.user.email}</small>
                            </div>
                          ) : (
                            <span className="text-muted">System</span>
                          )}
                        </td>
                        <td>
                          <span className={`badge ${
                            log.action.includes('DELETE') ? 'bg-danger' : 
                            log.action.includes('CREATE') ? 'bg-success' : 
                            log.action.includes('UPDATE') ? 'bg-warning' : 
                            'bg-primary'
                          }`}>
                            {log.action}
                          </span>
                        </td>
                        <td>{log.entity}</td>
                        <td>{log.description}</td>
                        <td className="text-nowrap">{log.ipAddress}</td>
                      </tr>
                    ))}
                  </tbody>
                </Table>
              </div>
              {logs.length > 10 && (
                <div className="text-center p-2 bg-light border-top">
                  <small className="text-muted">
                    📜 Scroll up to see all {logs.length} logs in this group
                  </small>
                </div>
              )}
            </Card.Body>
          </Card>
        ))
      ) : (
        // Regular table display - using pagination instead of scrolling
        <Table striped bordered hover size="sm" responsive>
          <thead className="table-dark">
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
                <td className="text-nowrap">{formatDateTime(log.actionAt)}</td>
                <td className="text-nowrap">
                  {log.user ? (
                    <div>
                      <div>{log.user.firstName} {log.user.lastName}</div>
                      <small className="text-muted">{log.user.email}</small>
                    </div>
                  ) : (
                    <span className="text-muted">System</span>
                  )}
                </td>
                <td>
                  <span className={`badge ${
                    log.action.includes('DELETE') ? 'bg-danger' : 
                    log.action.includes('CREATE') ? 'bg-success' : 
                    log.action.includes('UPDATE') ? 'bg-warning' : 
                    'bg-primary'
                  }`}>
                    {log.action}
                  </span>
                </td>
                <td>{log.entity}</td>
                <td>{log.description}</td>
                <td className="text-nowrap">{log.ipAddress}</td>
              </tr>
            ))}
          </tbody>
        </Table>
      ))}

      {/* Pagination */}
      {auditTotalPages > 1 && (
        <div className="d-flex justify-content-between align-items-center mt-3">
          <div className="text-muted">
            Page {auditPage + 1} of {auditTotalPages}
          </div>
          <div className="d-flex gap-2">
            <Button
              variant="outline-primary"
              size="sm"
              disabled={auditPage === 0}
              onClick={() => setAuditPage(0)}
            >
              First
            </Button>
            <Button
              variant="outline-primary"
              size="sm"
              disabled={auditPage === 0}
              onClick={() => setAuditPage(auditPage - 1)}
            >
              Previous
            </Button>
            <span className="align-self-center mx-2">
              Page {auditPage + 1} of {auditTotalPages}
            </span>
            <Button
              variant="outline-primary"
              size="sm"
              disabled={auditPage >= auditTotalPages - 1}
              onClick={() => setAuditPage(auditPage + 1)}
            >
              Next
            </Button>
            <Button
              variant="outline-primary"
              size="sm"
              disabled={auditPage >= auditTotalPages - 1}
              onClick={() => setAuditPage(auditTotalPages - 1)}
            >
              Last
            </Button>
          </div>
        </div>
      )}

      {!loading && auditLogs.length === 0 && (
        <Alert variant="info" className="text-center">
          <h5>No audit logs found</h5>
          {(auditFilters.startDate || auditFilters.endDate || auditFilters.user || auditFilters.userId || auditFilters.action || auditFilters.entity || auditFilters.description) ? (
            <p>Try adjusting your filters to see more results.</p>
          ) : (
            <div>
              <p>No audit logs have been generated yet.</p>
              <p className="small text-muted">
                Expected actions that generate audit logs: Creating shifts, updating shifts, deleting shifts, accepting/rejecting signups, user logins, etc.
              </p>
            </div>
          )}
        </Alert>
      )}

      {!loading && auditLogs.length > 0 && !auditFilters.startDate && !auditFilters.endDate && !auditFilters.action && (
        <Alert variant="success" className="mb-3">
          <small>
            <strong>✅ Audit logging is working!</strong> Showing {auditLogs.length} recent logs.
            Look for CREATE_SHIFT actions when you create new shifts.
          </small>
        </Alert>
      )}

      {!loading && auditFilters.startDate === 'LAST_24H' && auditLogs.length === 0 && (
        <Alert variant="info" className="mb-3">
          <h6>No logs from the last 24 hours</h6>
          <p>There haven't been any activities in the past 24 hours that would generate audit logs.</p>
          <p className="mb-0">
            <strong>Try:</strong> Create a new shift, login/logout, or perform other actions to generate new audit logs.
            Then click "🔄 Refresh" to see them appear.
          </p>
        </Alert>
      )}

      {!loading && auditLogs.length > 0 && auditFilters.startDate === 'LAST_24H' && (
        <Alert variant="warning" className="mb-3">
          <small>
            <strong>📅 Last 24h filter active:</strong> Showing {auditLogs.length} logs from the past 24 hours.
            The newest available log is from {auditLogs.length > 0 ? formatDateTime(auditLogs[0].actionAt) : 'N/A'}.
          </small>
        </Alert>
      )}
    </Container>
  );
}

export default Audits;
