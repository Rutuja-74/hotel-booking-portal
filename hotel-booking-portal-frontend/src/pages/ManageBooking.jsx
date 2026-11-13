import { useState } from 'react';
import { getBooking, cancelBooking } from '../api/bookingApi';
import BookingSummary from '../components/BookingSummary';
import ErrorMessage from '../components/ErrorMessage';

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export default function ManageBooking() {
  const [reference, setReference] = useState('');
  const [email, setEmail] = useState('');
  const [errors, setErrors] = useState({});

  const [booking, setBooking] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const [confirming, setConfirming] = useState(false);
  const [cancelling, setCancelling] = useState(false);
  const [cancelError, setCancelError] = useState('');

  async function handleFind(event) {
    event.preventDefault();

    const found = {};
    if (!reference.trim()) found.reference = 'Please enter your booking reference.';
    if (!email.trim()) found.email = 'Please enter your email.';
    else if (!EMAIL_PATTERN.test(email.trim())) found.email = 'Please enter a valid email address.';
    setErrors(found);
    if (Object.keys(found).length > 0) return;

    setLoading(true);
    setError('');
    setCancelError('');
    setConfirming(false);
    setBooking(null);
    try {
      const data = await getBooking(reference.trim().toUpperCase(), email.trim());
      setBooking(data);
    } catch (err) {
      setError(err.message || 'Could not find this booking.');
    } finally {
      setLoading(false);
    }
  }

  async function handleCancel() {
    if (cancelling) return;
    setCancelling(true);
    setCancelError('');
    try {
      // The email was already verified when we retrieved the booking
      const updated = await cancelBooking(booking.bookingReference, booking.customerEmail);
      setBooking(updated);
      setConfirming(false);
    } catch (err) {
      setCancelError(err.message || 'Could not cancel this booking.');
    } finally {
      setCancelling(false);
    }
  }

  const isCancelled = booking && booking.status === 'CANCELLED';

  return (
    <div className="container page">
      <h1 style={{ textAlign: 'center' }}>Manage your booking</h1>
      <p style={{ textAlign: 'center' }}>
        Enter the booking reference and the email you used when booking.
      </p>

      <form
        className="summary-card"
        style={{ marginBottom: 24 }}
        onSubmit={handleFind}
        noValidate
      >
        <div className="field">
          <label htmlFor="reference">Booking reference</label>
          <input
            id="reference"
            type="text"
            value={reference}
            onChange={(e) => setReference(e.target.value)}
            placeholder="e.g. HB-20261113-K7M2QX"
          />
          {errors.reference && <span className="form-error">{errors.reference}</span>}
        </div>

        <div className="field">
          <label htmlFor="manage-email">Email</label>
          <input
            id="manage-email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="you@example.com"
          />
          {errors.email && <span className="form-error">{errors.email}</span>}
        </div>

        <button type="submit" className="btn" disabled={loading} style={{ width: '100%' }}>
          {loading ? 'Searching...' : 'Find booking'}
        </button>
      </form>

      {error && (
        <div style={{ maxWidth: 640, margin: '0 auto 24px' }}>
          <ErrorMessage message={error} />
        </div>
      )}

      {booking && (
        <>
          <BookingSummary booking={booking} />

          <div style={{ maxWidth: 640, margin: '20px auto 0' }}>
            {cancelError && <ErrorMessage message={cancelError} />}

            {isCancelled && (
              <p className="availability-note none">
                This booking is cancelled. The room has been released.
              </p>
            )}

            {!isCancelled && !confirming && (
              <button
                type="button"
                className="btn btn-outline"
                onClick={() => setConfirming(true)}
              >
                Cancel booking
              </button>
            )}

            {!isCancelled && confirming && (
              <div className="state-box">
                <p>
                  <strong>Are you sure you want to cancel this booking?</strong> This cannot be
                  undone.
                </p>
                <div className="confirm-actions">
                  <button type="button" className="btn" onClick={handleCancel} disabled={cancelling}>
                    {cancelling ? 'Cancelling...' : 'Yes, cancel booking'}
                  </button>
                  <button
                    type="button"
                    className="btn btn-outline"
                    onClick={() => setConfirming(false)}
                    disabled={cancelling}
                  >
                    Keep booking
                  </button>
                </div>
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
}