import React, { useState } from 'react';
import { Modal, Form, Button, Alert } from 'react-bootstrap';
import { userAPI } from '../api/client';
import { useAuthStore } from '../store/authStore';

function UserProfileModal({ show, onHide, onProfileUpdated }) {
  const { user, login, token, isAdmin, firstTimeLogin } = useAuthStore();
  const [formData, setFormData] = useState({
    firstName: user?.firstName || '',
    lastName: user?.lastName || '',
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    setError('');
    setSuccess('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    // Validation
    if (!formData.firstName.trim()) {
      setError('First name is required');
      return;
    }

    if (!formData.lastName.trim()) {
      setError('Last name is required');
      return;
    }

    setLoading(true);

    try {
      // Update user profile via API
      await userAPI.updateProfile(user.userId, {
        firstName: formData.firstName.trim(),
        lastName: formData.lastName.trim(),
      });

      // Update the auth store with new user data
      const updatedUser = {
        ...user,
        firstName: formData.firstName.trim(),
        lastName: formData.lastName.trim(),
      };

      // Re-login with updated user data to refresh the store
      login(updatedUser, token, isAdmin, firstTimeLogin);

      setSuccess('Profile updated successfully!');

      // Call the callback to notify parent component
      if (onProfileUpdated) {
        onProfileUpdated(updatedUser);
      }

      // Auto-close modal after 1.5 seconds
      setTimeout(() => {
        setSuccess('');
        onHide();
      }, 1500);

    } catch (err) {
      console.error('Profile update error:', err);
      setError(err.response?.data?.message || 'Failed to update profile. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    if (!loading) {
      setError('');
      setSuccess('');
      setFormData({
        firstName: user?.firstName || '',
        lastName: user?.lastName || '',
      });
      onHide();
    }
  };

  return (
    <Modal show={show} onHide={handleClose} centered>
      <Modal.Header closeButton>
        <Modal.Title>
          <i className="bi bi-person-gear me-2"></i>
          Update Profile
        </Modal.Title>
      </Modal.Header>

      <Form onSubmit={handleSubmit}>
        <Modal.Body>
          {error && <Alert variant="danger">{error}</Alert>}
          {success && <Alert variant="success">{success}</Alert>}

          <Form.Group className="mb-3">
            <Form.Label>
              First Name <span className="text-danger">*</span>
            </Form.Label>
            <Form.Control
              type="text"
              name="firstName"
              value={formData.firstName}
              onChange={handleChange}
              placeholder="Enter your first name"
              disabled={loading}
              required
            />
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Label>
              Last Name <span className="text-danger">*</span>
            </Form.Label>
            <Form.Control
              type="text"
              name="lastName"
              value={formData.lastName}
              onChange={handleChange}
              placeholder="Enter your last name"
              disabled={loading}
              required
            />
          </Form.Group>

          <div className="text-muted small">
            <i className="bi bi-info-circle me-1"></i>
            Changes will be reflected across the application immediately.
          </div>
        </Modal.Body>

        <Modal.Footer>
          <Button
            variant="secondary"
            onClick={handleClose}
            disabled={loading}
          >
            Cancel
          </Button>
          <Button
            variant="primary"
            type="submit"
            disabled={loading}
          >
            {loading ? (
              <>
                <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                Updating...
              </>
            ) : (
              <>
                <i className="bi bi-check-lg me-2"></i>
                Update Profile
              </>
            )}
          </Button>
        </Modal.Footer>
      </Form>
    </Modal>
  );
}

export default UserProfileModal;
