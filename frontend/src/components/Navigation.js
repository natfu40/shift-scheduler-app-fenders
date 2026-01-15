import React from 'react';
import { Navbar, Nav, Container, Button } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';

function Navigation() {
  const { user, isAdmin, logout } = useAuthStore();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
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
                  Dashboard
                </Nav.Link>
                <Nav.Link as={Link} to="/calendar" className="text-white">
                  Calendar
                </Nav.Link>
                {isAdmin && (
                  <>
                    <Nav.Link as={Link} to="/admin" className="text-white">
                      Admin
                    </Nav.Link>
                    <Nav.Link as={Link} to="/users" className="text-white">
                      Users
                    </Nav.Link>
                    <Nav.Link as={Link} to="/audits" className="text-white">
                      Audits
                    </Nav.Link>
                  </>
                )}
                <span className="navbar-text ms-3 me-3 text-white">
                  {user.firstName} {user.lastName}
                </span>
                <Button variant="outline-light" onClick={handleLogout}>
                  Logout
                </Button>
              </>
            )}
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
}

export default Navigation;

