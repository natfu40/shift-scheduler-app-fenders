import React, { useState } from 'react';
import { Container, Form, Button, Alert, Card } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import CryptoJS from 'crypto-js';
import { authAPI } from '../api/client';
import { useAuthStore } from '../store/authStore';
import PasswordChangeModal from '../components/PasswordChangeModal';

function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPasswordChange, setShowPasswordChange] = useState(false);
  // eslint-disable-next-line no-unused-vars
  const [preventNavigation, setPreventNavigation] = useState(false);
  // eslint-disable-next-line no-unused-vars
  const [isFirstTimeUser, setIsFirstTimeUser] = useState(false);
  const [pendingLoginData, setPendingLoginData] = useState(null);
  const { login, setAdmin, setFirstTimeLogin } = useAuthStore();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      // Hash password on client side before sending to prevent clear text transmission
      const hashedPassword = CryptoJS.SHA256(password).toString();

      const response = await authAPI.login({ email, password: hashedPassword });
      const { token, userId, firstName, lastName, firstTimeLogin: isFirstTimeLogin, admin } = response.data;

      console.log('🔍 Login response data:', response.data);
      console.log('🔍 Admin field from backend:', admin);
      console.log('🔍 Admin field type:', typeof admin);

      // Use admin field directly from login response (backend now provides this)
      let isAdmin = admin === true || admin === 'true';

      console.log('🔍 Final isAdmin value:', isAdmin);


      // Handle first-time login users
      if (isFirstTimeLogin === true) {
        const loginData = { userId, email, firstName, lastName, token, isAdmin };

        // Temporarily store token for password change API call
        localStorage.setItem('tempToken', token);

        // Set states to show password change modal
        setIsFirstTimeUser(true);
        setShowPasswordChange(true);
        setPendingLoginData(loginData);

        setLoading(false);
        return;
      }

      // Normal login flow
      login({ userId, email, firstName, lastName }, token, isAdmin, isFirstTimeLogin);
      setAdmin(isAdmin);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed. Please check your credentials and try again.');
    } finally {
      setLoading(false);
    }
  };

  const handlePasswordChanged = () => {
    // Get stored login data from state
    if (pendingLoginData) {
      // Complete the login process with firstTimeLogin = false
      login({
        userId: pendingLoginData.userId,
        email: pendingLoginData.email,
        firstName: pendingLoginData.firstName,
        lastName: pendingLoginData.lastName
      }, pendingLoginData.token, pendingLoginData.isAdmin, false);

      setAdmin(pendingLoginData.isAdmin);

      // Clean up states and temporary token
      setPendingLoginData(null);
      setIsFirstTimeUser(false);
      localStorage.removeItem('tempToken');
    }

    setFirstTimeLogin(false);
    setShowPasswordChange(false);
    setPreventNavigation(false);
    navigate('/');
  };


  return (
    <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '100vh' }}>
      <Card className="w-100" style={{ maxWidth: '400px' }}>
        <Card.Body>
          <h2 className="text-center mb-4">Login</h2>
          {error && <Alert variant="danger">{error}</Alert>}

          {/* Only show login form if not showing password change modal */}
          {!showPasswordChange && (
            <Form onSubmit={handleSubmit}>
            <Form.Group className="mb-3">
              <Form.Label>Email</Form.Label>
              <Form.Control
                type="email"
                placeholder="Enter email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Password</Form.Label>
              <Form.Control
                type="password"
                placeholder="Enter password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </Form.Group>

            <Button
              variant="primary"
              type="submit"
              className="w-100"
              disabled={loading}
            >
              {loading ? 'Logging in...' : 'Login'}
            </Button>
          </Form>
          )}

          {/* Show message when password change modal is active */}
          {showPasswordChange && (
            <div className="text-center">
              <Alert variant="info">
                <i className="bi bi-info-circle me-2"></i>
                Please change your password to continue.
              </Alert>
            </div>
          )}
        </Card.Body>
      </Card>

      <PasswordChangeModal
        show={showPasswordChange}
        onHide={() => {}} // Prevent closing modal during first-time login
        onPasswordChanged={handlePasswordChanged}
        isRequired={true} // Mark as required for first-time login
        tempToken={pendingLoginData?.token} // Pass temporary token for first-time login
      />
    </Container>
  );
}

export default LoginPage;

