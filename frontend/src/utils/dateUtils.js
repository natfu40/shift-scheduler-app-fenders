import { format } from 'date-fns';

// Helper function to properly handle UTC timestamps from backend
const createDateFromString = (dateString) => {
  if (!dateString) return null;

  // If it's in format "2026-01-14T15:30:45" (no timezone info), treat as UTC
  if (dateString.includes('T') && !dateString.endsWith('Z') && !dateString.includes('+') && !dateString.includes('-', 19)) {
    return new Date(dateString + 'Z');
  }

  // If it already has timezone info or is in other format
  return new Date(dateString);
};

export const formatShiftTime = (startTime, endTime) => {
  try {
    const startDate = createDateFromString(startTime);
    const endDate = createDateFromString(endTime);

    if (!startDate || !endDate || isNaN(startDate.getTime()) || isNaN(endDate.getTime())) {
      return 'Invalid date';
    }

    const start = format(startDate, 'MMM dd, yyyy h:mm a');
    const end = format(endDate, 'h:mm a');
    return `${start} - ${end}`;
  } catch (error) {
    console.error('Error formatting shift time:', error);
    return 'Invalid date';
  }
};

export const formatDateTime = (dateTime) => {
  try {
    const date = createDateFromString(dateTime);

    if (!date || isNaN(date.getTime())) {
      return 'Invalid date';
    }

    return format(date, 'MMM dd, yyyy h:mm a');
  } catch (error) {
    console.error('Error formatting date time:', error);
    return 'Invalid date';
  }
};

export const formatDate = (date) => {
  try {
    const dateObj = createDateFromString(date);

    if (!dateObj || isNaN(dateObj.getTime())) {
      return 'Invalid date';
    }

    return format(dateObj, 'MMM dd, yyyy');
  } catch (error) {
    console.error('Error formatting date:', error);
    return 'Invalid date';
  }
};

export const isUpcoming = (dateTime) => {
  const shiftDate = new Date(dateTime);
  const now = new Date();
  return shiftDate > now;
};

export const isTodayOrLater = (dateTime) => {
  const shiftDate = new Date(dateTime);
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  shiftDate.setHours(0, 0, 0, 0);
  return shiftDate >= today;
};
