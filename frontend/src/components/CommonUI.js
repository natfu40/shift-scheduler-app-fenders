import React from 'react';
import { Spinner } from 'react-bootstrap';

export const LoadingSpinner = ({ text = 'Loading...' }) => (
  <div className="text-center my-4">
    <Spinner animation="border" role="status" className="me-2" />
    <span>{text}</span>
  </div>
);

export const ErrorAlert = ({ error, onDismiss }) => {
  if (!error) return null;

  return (
    <div className="alert alert-danger alert-dismissible" role="alert">
      {error}
      {onDismiss && (
        <button type="button" className="btn-close" onClick={onDismiss}></button>
      )}
    </div>
  );
};

export const SuccessAlert = ({ message, onDismiss }) => {
  if (!message) return null;

  return (
    <div className="alert alert-success alert-dismissible" role="alert">
      {message}
      {onDismiss && (
        <button type="button" className="btn-close" onClick={onDismiss}></button>
      )}
    </div>
  );
};
