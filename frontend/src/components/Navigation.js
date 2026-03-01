import React, { useState } from 'react';
import { Navbar, Nav, Container, Dropdown } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import PasswordChangeModal from './PasswordChangeModal';
import UserProfileModal from './UserProfileModal';

function Navigation() {
  const { user, isAdmin, logout } = useAuthStore();
  const navigate = useNavigate();
  const [showPasswordModal, setShowPasswordModal] = useState(false);
  const [showProfileModal, setShowProfileModal] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const handlePasswordChange = () => {
    setShowPasswordModal(false);
    // Optionally show success message or refresh user info
  };

  const handleProfileUpdate = (updatedUser) => {
    setShowProfileModal(false);
    // Profile is already updated in the store via the modal
  };

  return (
    <>
      <Navbar bg="dark" expand="lg" sticky="top">
        <Container>
          <Navbar.Brand as={Link} to="/" className="fw-bold text-white">
            🍺 Fenders Brewing Scheduler
          </Navbar.Brand>
          <Navbar.Toggle aria-controls="basic-navbar-nav" />
          <Navbar.Collapse id="basic-navbar-nav">
            <Nav className="ms-auto">
              {user && (
                <>
                  <Nav.Link as={Link} to="/" className="text-white">
                    Home
                  </Nav.Link>
                  <Nav.Link as={Link} to="/calendar" className="text-white">
                    Calendar
                  </Nav.Link>
                  {isAdmin && (
                    <>
                      <Nav.Link as={Link} to="/admin" className="text-white">
                        Shift Management
                      </Nav.Link>
                      <Nav.Link as={Link} to="/users" className="text-white">
                        Users
                      </Nav.Link>
                      <Nav.Link as={Link} to="/audits" className="text-white">
                        Audits
                      </Nav.Link>
                    </>
                  )}
                  <Dropdown align="end">
                    <Dropdown.Toggle
                      as="button"
                      className="btn btn-link text-white text-decoration-none border-0 bg-transparent"
                      style={{ boxShadow: 'none' }}
                    >
                      <i className="bi bi-person-circle me-2"></i>
                      {user.firstName} {user.lastName}
                      <i className="bi bi-chevron-down ms-2"></i>
                    </Dropdown.Toggle>

                    <Dropdown.Menu>
                      <Dropdown.Item onClick={() => setShowProfileModal(true)}>
                        <i className="bi bi-person-gear me-2"></i>
                        Update Profile
                      </Dropdown.Item>
                      <Dropdown.Item onClick={() => setShowPasswordModal(true)}>
                        <i className="bi bi-key me-2"></i>
                        Change Password
                      </Dropdown.Item>
                      <Dropdown.Divider />
                      <Dropdown.Item onClick={handleLogout}>
                        <i className="bi bi-box-arrow-right me-2"></i>
                        Logout
                      </Dropdown.Item>
                    </Dropdown.Menu>
                  </Dropdown>
                </>
              )}
            </Nav>
          </Navbar.Collapse>
        </Container>
      </Navbar>

      {/* Password Change Modal */}
      <PasswordChangeModal
        show={showPasswordModal}
        onHide={() => setShowPasswordModal(false)}
        onPasswordChanged={handlePasswordChange}
        isRequired={false}
      />

      {/* User Profile Modal */}
      <UserProfileModal
        show={showProfileModal}
        onHide={() => setShowProfileModal(false)}
        onProfileUpdated={handleProfileUpdate}
      />
    </>
  );
}

export default Navigation;

