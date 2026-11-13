import { useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createBooking } from '../api/bookingApi';
import ErrorMessage from './ErrorMessage';

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export default function BookingForm({ hotel, checkIn, checkOut, onSoldOut }) {
  const navigate = useNavigate();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [errors, setErrors] = useState({});
  const [formError, setFormError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  // A ref updates instantly, so a fast double click cannot send two requests
  const submittingRef = useRef(false);

  function validate() {
    const found = {};
    if (!name.trim()) found.customerName = 'Please enter your name.';
    else if (name.trim().length > 100) found.customerName = 'Name must be 100 characters or less.';

    if (!email.trim()) found.customerEmail = 'Please enter your email.';
    else if (!EMAIL_PATTERN.test(email.trim())) found.customerEmail = 'Please enter a valid email address.';
    else if (email.trim().length > 150) found.customerEmail = 'Email must be 150 characters or less.';
    return found;
  }

  async function handleSubmit(event) {
    event.preventDefault();
    if (submittingRef.current) return;

    const found = validate();
    setErrors(found);
    setFormError('');
    if (Object.keys(found).length > 0) return;

    submittingRef.current = true;
    setSubmitting(true);
    try {
      const booking = await createBooking({
        roomId: hotel.roomId,
        customerName: name.trim(),
        customerEmail: email.trim(),
        checkIn,
        checkOut,
      });
      // Reference + email in the URL make the confirmation page refresh-safe
      navigate(
        `/booking/${booking.bookingReference}?email=${encodeURIComponent(booking.customerEmail)}`,
        { state: { booking } }
      );
    } catch (err) {
      if (err.fieldErrors) setErrors(err.fieldErrors);
      setFormError(err.message || 'Booking failed. Please try again.');
      // 409 = room was taken meanwhile; refresh the availability shown on the page
      if (err.status === 409 && onSoldOut) onSoldOut();
    } finally {
      submittingRef.current = false;
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} noValidate>
      {formError && <ErrorMessage message={formError} />}

      <div className="field">
        <label htmlFor="customerName">Full name</label>
        <input
          id="customerName"
          type="text"
          value={name}
          maxLength={100}
          onChange={(e) => setName(e.target.value)}
          placeholder="e.g. Rutuja Patil"
        />
        {errors.customerName && <span className="form-error">{errors.customerName}</span>}
      </div>

      <div className="field">
        <label htmlFor="customerEmail">Email</label>
        <input
          id="customerEmail"
          type="email"
          value={email}
          maxLength={150}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="you@example.com"
        />
        {errors.customerEmail && <span className="form-error">{errors.customerEmail}</span>}
      </div>

      <button type="submit" className="btn" disabled={submitting} style={{ width: '100%' }}>
        {submitting ? 'Booking...' : 'Confirm booking'}
      </button>
    </form>
  );
}