import React, { useState } from 'react';
import { Modal, Form, Button, Alert } from 'react-bootstrap';
import CryptoJS from 'crypto-js';
import { authAPI } from '../api/client';
import axiosInstance from '../api/client';
import { API_ENDPOINTS } from '../constants/apiConstants';

function PasswordChangeModal({ show, onHide, onPasswordChanged, isRequired = false, tempToken = null }) {

  const [formData, setFormData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const [passwordStrength, setPasswordStrength] = useState(0);
  const [showPasswords, setShowPasswords] = useState(false);

  const calculatePasswordStrength = (password) => {
    let strength = 0;
    if (password.length >= 8) strength += 25;
    if (/[a-z]/.test(password)) strength += 25;
    if (/[A-Z]/.test(password)) strength += 25;
    if (/[0-9]/.test(password)) strength += 15;
    if (/[^A-Za-z0-9]/.test(password)) strength += 10;
    return Math.min(100, strength);
  };

  const getPasswordStrengthColor = (strength) => {
    if (strength < 30) return 'danger';
    if (strength < 60) return 'warning';
    if (strength < 80) return 'info';
    return 'success';
  };

  const getPasswordStrengthText = (strength) => {
    if (strength < 30) return 'Weak';
    if (strength < 60) return 'Fair';
    if (strength < 80) return 'Good';
    return 'Strong';
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));

    // Calculate password strength for new password
    if (name === 'newPassword') {
      setPasswordStrength(calculatePasswordStrength(value));
    }

    // Clear error when user starts typing
    if (error) setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    // Enhanced validation
    if (formData.newPassword !== formData.confirmPassword) {
      setError('New passwords do not match');
      return;
    }

    if (formData.newPassword.length < 6) {
      setError('New password must be at least 6 characters long');
      return;
    }

    if (passwordStrength < 30) {
      setError('Password is too weak. Please use a stronger password.');
      return;
    }

    if (formData.currentPassword === formData.newPassword) {
      setError('New password must be different from current password');
      return;
    }

    setLoading(true);

    try {
      // Hash passwords on client side before sending to prevent clear text transmission
      const hashedCurrentPassword = CryptoJS.SHA256(formData.currentPassword).toString();
      const hashedNewPassword = CryptoJS.SHA256(formData.newPassword).toString();

      // Prepare the password change data
      const passwordChangeData = {
        currentPassword: hashedCurrentPassword,
        newPassword: hashedNewPassword,
      };

      // Use temporary token if available for first-time login
      const tokenToUse = tempToken || localStorage.getItem('tempToken');

      if (tokenToUse) {
        // Make a direct call using the configured axios instance with the temporary token
        await axiosInstance.post(API_ENDPOINTS.AUTH.CHANGE_PASSWORD_HASHED, passwordChangeData, {
          headers: {
            'Authorization': `Bearer ${tokenToUse}`,
            'Content-Type': 'application/json'
          }
        });
      } else {
        // Use the normal authAPI call (which will use token from localStorage)
        await authAPI.changePassword(passwordChangeData);
      }

      setSuccess('Password changed successfully!');
      setFormData({
        currentPassword: '',
        newPassword: '',
        confirmPassword: '',
      });
      setPasswordStrength(0);

      // Close modal after brief delay to show success message
      setTimeout(() => {
        setSuccess('');
        onPasswordChanged();
      }, 1500);

    } catch (err) {
      const errorMessage = err.response?.data?.message || err.response?.data || 'Failed to change password. Please try again.';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    if (!isRequired) {
      setFormData({
        currentPassword: '',
        newPassword: '',
        confirmPassword: '',
      });
      setError('');
      setSuccess('');
      setPasswordStrength(0);
      onHide();
    }
  };

  return (
    <Modal show={show} onHide={handleClose} backdrop={isRequired ? "static" : true} keyboard={!isRequired}>
      <Modal.Header closeButton={!isRequired}>
        <Modal.Title>
          <i className="bi bi-shield-lock me-2"></i>
          Change Password
        </Modal.Title>
      </Modal.Header>
      <Modal.Body>
        {isRequired && (
          <Alert variant="warning">
            <i className="bi bi-exclamation-triangle me-2"></i>
            You must change your password before continuing.
          </Alert>
        )}

        <Alert variant="info" className="d-flex align-items-center">
          <i className="bi bi-info-circle me-2"></i>
          <small>
            Your password is transmitted securely over HTTPS encryption.
            Choose a strong password with at least 6 characters.
          </small>
        </Alert>

        {error && <Alert variant="danger">{error}</Alert>}
        {success && <Alert variant="success">{success}</Alert>}

        <Form onSubmit={handleSubmit}>
          <Form.Group className="mb-3">
            <Form.Label>Current Password</Form.Label>
            <div className="input-group">
              <Form.Control
                type={showPasswords ? "text" : "password"}
                placeholder="Enter current password"
                name="currentPassword"
                value={formData.currentPassword}
                onChange={handleChange}
                required
                disabled={loading}
              />
            </div>
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Label>New Password</Form.Label>
            <div className="input-group">
              <Form.Control
                type={showPasswords ? "text" : "password"}
                placeholder="Enter new password"
                name="newPassword"
                value={formData.newPassword}
                onChange={handleChange}
                required
                minLength={6}
                disabled={loading}
              />
            </div>
            {formData.newPassword && (
              <div className="mt-2">
                <div className="d-flex justify-content-between align-items-center">
                  <small className="text-muted">Password strength:</small>
                  <small className={`text-${getPasswordStrengthColor(passwordStrength)}`}>
                    {getPasswordStrengthText(passwordStrength)}
                  </small>
                </div>
                <div className="progress" style={{ height: '4px' }}>
                  <div
                    className={`progress-bar bg-${getPasswordStrengthColor(passwordStrength)}`}
                    style={{ width: `${passwordStrength}%` }}
                  ></div>
                </div>
              </div>
            )}
            <Form.Text className="text-muted">
              Use at least 6 characters with a mix of letters, numbers, and symbols for better security.
            </Form.Text>
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Label>Confirm New Password</Form.Label>
            <div className="input-group">
              <Form.Control
                type={showPasswords ? "text" : "password"}
                placeholder="Confirm new password"
                name="confirmPassword"
                value={formData.confirmPassword}
                onChange={handleChange}
                required
                disabled={loading}
                className={formData.confirmPassword && formData.newPassword !== formData.confirmPassword ? 'is-invalid' : ''}
              />
            </div>
            {formData.confirmPassword && formData.newPassword !== formData.confirmPassword && (
              <Form.Text className="text-danger">
                Passwords do not match
              </Form.Text>
            )}
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Check
              type="checkbox"
              id="showPasswords"
              label="Show passwords"
              checked={showPasswords}
              onChange={(e) => setShowPasswords(e.target.checked)}
              disabled={loading}
            />
          </Form.Group>

          <div className="d-grid">
            <Button
              variant="primary"
              type="submit"
              disabled={loading || passwordStrength < 30 || formData.newPassword !== formData.confirmPassword}
            >
              {loading ? (
                <>
                  <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                  Changing Password...
                </>
              ) : (
                <>
                  <i className="bi bi-check-circle me-2"></i>
                  Change Password
                </>
              )}
            </Button>
          </div>
        </Form>
      </Modal.Body>
    </Modal>
  );
}

export default PasswordChangeModal;
