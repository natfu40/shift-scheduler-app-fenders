import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Button, Table, Spinner, Alert, Modal, Form } from 'react-bootstrap';
import CryptoJS from 'crypto-js';
import { adminAPI, userAPI } from '../api/client';
import { useAuthStore } from '../store/authStore';
import { useNavigate } from 'react-router-dom';

function UsersPage() {
  const { isAdmin } = useAuthStore();
  const navigate = useNavigate();

  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Edit admin role modal states
  const [showAdminModal, setShowAdminModal] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  const [isChangingAdminRole, setIsChangingAdminRole] = useState(false);

  // Delete user modal states
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [userToDelete, setUserToDelete] = useState(null);
  const [isDeletingUser, setIsDeletingUser] = useState(false);

  // Add new user modal states
  const [showAddUserModal, setShowAddUserModal] = useState(false);
  const [isCreatingUser, setIsCreatingUser] = useState(false);
  const [newUserData, setNewUserData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: '',
  });

  useEffect(() => {
    // Redirect if not admin
    if (!isAdmin) {
      navigate('/');
      return;
    }
    fetchUsers();
  }, [isAdmin, navigate]);

  const fetchUsers = async () => {
    try {
      setLoading(true);

      // Fetch all users from the backend
      const usersRes = await userAPI.getAllUsers();

      // Get admin status for each user
      const usersWithAdminStatus = await Promise.all(
        usersRes.data.map(async (u) => {
          try {
            const isUserAdmin = await adminAPI.isUserAdmin(u.id);
            return {
              id: u.id,
              name: `${u.firstName} ${u.lastName}`,
              email: u.email,
              isAdmin: isUserAdmin.data
            };
          } catch (err) {
            console.error(`Failed to check admin status for user ${u.id}:`, err);
            return {
              id: u.id,
              name: `${u.firstName} ${u.lastName}`,
              email: u.email,
              isAdmin: false
            };
          }
        })
      );

      // Sort by name
      usersWithAdminStatus.sort((a, b) => a.name.localeCompare(b.name));

      setUsers(usersWithAdminStatus);
      setError('');
    } catch (err) {
      console.error('Failed to load users:', err);
      setError('Failed to load users. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleOpenAdminModal = (userToEdit) => {
    setSelectedUser(userToEdit);
    setShowAdminModal(true);
  };

  const handleCloseAdminModal = () => {
    setShowAdminModal(false);
    setSelectedUser(null);
  };

  const handleOpenDeleteModal = (userToRemove) => {
    setUserToDelete(userToRemove);
    setShowDeleteModal(true);
  };

  const handleCloseDeleteModal = () => {
    setShowDeleteModal(false);
    setUserToDelete(null);
  };

  const handleOpenAddUserModal = () => {
    setShowAddUserModal(true);
  };

  const handleCloseAddUserModal = () => {
    setShowAddUserModal(false);
    setNewUserData({
      firstName: '',
      lastName: '',
      email: '',
      password: '',
      confirmPassword: '',
    });
  };

  const handleNewUserDataChange = (e) => {
    const { name, value } = e.target;
    setNewUserData(prev => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleCreateUser = async (e) => {
    e.preventDefault();

    if (newUserData.password !== newUserData.confirmPassword) {
      setError('Passwords do not match');
      return;
    }

    if (newUserData.password.length < 6) {
      setError('Password must be at least 6 characters long');
      return;
    }

    setIsCreatingUser(true);
    try {
      // Hash password on client side before sending to prevent clear text transmission
      const hashedPassword = CryptoJS.SHA256(newUserData.password).toString();

      const userData = {
        firstName: newUserData.firstName,
        lastName: newUserData.lastName,
        email: newUserData.email,
        password: hashedPassword,
      };

      await userAPI.createUser(userData);
      setSuccess(`User ${newUserData.firstName} ${newUserData.lastName} has been created successfully. They will need to change their password on first login.`);

      // Refresh users list
      await fetchUsers();

      handleCloseAddUserModal();
      setTimeout(() => setSuccess(''), 5000);
    } catch (err) {
      console.error('Failed to create user:', err);
      setError(err.response?.data?.message || 'Failed to create user. Please try again.');
    } finally {
      setIsCreatingUser(false);
    }
  };

  const handleDeleteUser = async () => {
    if (!userToDelete) return;

    setIsDeletingUser(true);
    try {
      await userAPI.deleteUser(userToDelete.id);
      setSuccess(`${userToDelete.name} has been deleted successfully. All their shift assignments have been removed.`);

      // Remove user from the list
      setUsers(prev => prev.filter(u => u.id !== userToDelete.id));

      handleCloseDeleteModal();
      setTimeout(() => setSuccess(''), 4000);
    } catch (err) {
      console.error('Failed to delete user:', err);
      setError(err.response?.data?.message || 'Failed to delete user.');
    } finally {
      setIsDeletingUser(false);
    }
  };

  const handleToggleAdminRole = async () => {
    if (!selectedUser) return;

    setIsChangingAdminRole(true);
    try {
      if (selectedUser.isAdmin) {
        // Remove admin role
        await adminAPI.removeUserAdmin(selectedUser.id);
        setSuccess(`${selectedUser.name} is no longer an admin.`);
      } else {
        // Make user admin
        await adminAPI.makeUserAdmin(selectedUser.id);
        setSuccess(`${selectedUser.name} is now an admin.`);
      }

      // Update local state
      setUsers(prev =>
        prev.map(u =>
          u.id === selectedUser.id ? { ...u, isAdmin: !u.isAdmin } : u
        )
      );

      handleCloseAdminModal();
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      console.error('Failed to update admin role:', err);
      setError(err.response?.data?.message || 'Failed to update admin role.');
    } finally {
      setIsChangingAdminRole(false);
    }
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
      <Row className="mb-4">
        <Col>
          <div className="d-flex justify-content-between align-items-center">
            <h1>User Management</h1>
            <Button variant="success" onClick={handleOpenAddUserModal}>
              Add New User
            </Button>
          </div>
        </Col>
      </Row>

      {error && <Alert variant="danger">{error}</Alert>}
      {success && <Alert variant="success">{success}</Alert>}

      <Row>
        <Col>
          <Card>
            <Card.Header>Users</Card.Header>
            <Card.Body>
              {users.length === 0 ? (
                <p>No users found.</p>
              ) : (
                <Table striped bordered hover responsive>
                  <thead>
                    <tr>
                      <th>Name</th>
                      <th>Email</th>
                      <th>ID</th>
                      <th>Admin Status</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {users.map((u) => (
                      <tr key={u.id}>
                        <td>{u.name}</td>
                        <td>{u.email}</td>
                        <td>{u.id}</td>
                        <td>
                          {u.isAdmin ? (
                            <span className="badge bg-success">Admin</span>
                          ) : (
                            <span className="badge bg-secondary">User</span>
                          )}
                        </td>
                        <td>
                          <Button
                            variant={u.isAdmin ? 'warning' : 'primary'}
                            size="sm"
                            className="me-2"
                            onClick={() => handleOpenAdminModal(u)}
                          >
                            {u.isAdmin ? 'Remove Admin' : 'Make Admin'}
                          </Button>
                          <Button
                            variant="danger"
                            size="sm"
                            onClick={() => handleOpenDeleteModal(u)}
                          >
                            Delete User
                          </Button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </Table>
              )}
            </Card.Body>
          </Card>
        </Col>
      </Row>

      {/* Edit Admin Role Modal */}
      <Modal show={showAdminModal} onHide={handleCloseAdminModal}>
        <Modal.Header closeButton>
          <Modal.Title>
            {selectedUser?.isAdmin ? 'Remove Admin Role' : 'Make User Admin'}
          </Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {selectedUser && (
            <>
              <p>
                Are you sure you want to{' '}
                <strong>{selectedUser.isAdmin ? 'remove admin role from' : 'make admin'}</strong>{' '}
                <strong>{selectedUser.name}</strong>?
              </p>
              {selectedUser.isAdmin && (
                <Alert variant="warning">
                  Removing admin role will prevent this user from managing shifts, approving signups, and accessing the admin dashboard.
                </Alert>
              )}
              {!selectedUser.isAdmin && (
                <Alert variant="info">
                  Making this user an admin will give them access to the admin dashboard and the ability to manage shifts and signups.
                </Alert>
              )}
            </>
          )}
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={handleCloseAdminModal} disabled={isChangingAdminRole}>
            Cancel
          </Button>
          <Button
            variant={selectedUser?.isAdmin ? 'warning' : 'primary'}
            onClick={handleToggleAdminRole}
            disabled={isChangingAdminRole}
          >
            {isChangingAdminRole ? 'Updating...' : selectedUser?.isAdmin ? 'Remove Admin' : 'Make Admin'}
          </Button>
        </Modal.Footer>
      </Modal>

      {/* Delete User Modal */}
      <Modal show={showDeleteModal} onHide={handleCloseDeleteModal}>
        <Modal.Header closeButton>
          <Modal.Title>Delete User</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {userToDelete && (
            <>
              <p>
                Are you sure you want to delete <strong>{userToDelete.name}</strong>?
              </p>
              <Alert variant="danger">
                <strong>Warning:</strong> This action cannot be undone. All shift assignments (pending and approved) for this user will be removed.
              </Alert>
            </>
          )}
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={handleCloseDeleteModal} disabled={isDeletingUser}>
            Cancel
          </Button>
          <Button
            variant="danger"
            onClick={handleDeleteUser}
            disabled={isDeletingUser}
          >
            {isDeletingUser ? 'Deleting...' : 'Delete User'}
          </Button>
        </Modal.Footer>
      </Modal>

      {/* Add New User Modal */}
      <Modal show={showAddUserModal} onHide={handleCloseAddUserModal}>
        <Modal.Header closeButton>
          <Modal.Title>Add New User</Modal.Title>
        </Modal.Header>
        <Form onSubmit={handleCreateUser}>
          <Modal.Body>
            <Alert variant="info">
              The new user will be required to change their password on first login.
            </Alert>

            <Form.Group className="mb-3">
              <Form.Label>First Name</Form.Label>
              <Form.Control
                type="text"
                placeholder="Enter first name"
                name="firstName"
                value={newUserData.firstName}
                onChange={handleNewUserDataChange}
                required
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Last Name</Form.Label>
              <Form.Control
                type="text"
                placeholder="Enter last name"
                name="lastName"
                value={newUserData.lastName}
                onChange={handleNewUserDataChange}
                required
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Email</Form.Label>
              <Form.Control
                type="email"
                placeholder="Enter email address"
                name="email"
                value={newUserData.email}
                onChange={handleNewUserDataChange}
                required
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Initial Password</Form.Label>
              <Form.Control
                type="password"
                placeholder="Enter initial password"
                name="password"
                value={newUserData.password}
                onChange={handleNewUserDataChange}
                required
                minLength={6}
              />
              <Form.Text className="text-muted">
                Password must be at least 6 characters long. User will need to change this on first login.
              </Form.Text>
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Confirm Password</Form.Label>
              <Form.Control
                type="password"
                placeholder="Confirm password"
                name="confirmPassword"
                value={newUserData.confirmPassword}
                onChange={handleNewUserDataChange}
                required
              />
            </Form.Group>
          </Modal.Body>
          <Modal.Footer>
            <Button variant="secondary" onClick={handleCloseAddUserModal} disabled={isCreatingUser}>
              Cancel
            </Button>
            <Button
              variant="success"
              type="submit"
              disabled={isCreatingUser}
            >
              {isCreatingUser ? 'Creating User...' : 'Create User'}
            </Button>
          </Modal.Footer>
        </Form>
      </Modal>
    </Container>
  );
}

export default UsersPage;

