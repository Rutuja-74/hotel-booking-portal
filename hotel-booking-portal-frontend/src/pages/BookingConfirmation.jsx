import { useEffect, useState } from 'react';
import { Link, useLocation, useParams, useSearchParams } from 'react-router-dom';
import { CheckCircle } from 'lucide-react';
import { getBooking } from '../api/bookingApi';
import { isCancel } from '../api/client';
import Loader from '../components/Loader';
import ErrorMessage from '../components/ErrorMessage';
import BookingSummary from '../components/BookingSummary';
import '../styles/booking.css';

export default function BookingConfirmation() {
  const { reference } = useParams();
  const [searchParams] = useSearchParams();
  const email = searchParams.get('email') || '';
  const location = useLocation();

  // Right after booking we already have the data from the router state.
  // After a page refresh the state is gone, so we fetch it again by reference + email.
  const [booking, setBooking] = useState(location.state?.booking || null);
  const [loading, setLoading] = useState(!location.state?.booking);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (booking) return undefined;
    if (!email) {
      setLoading(false);
      return undefined;
    }

    const controller = new AbortController();
    getBooking(reference, email, controller.signal)
      .then((data) => setBooking(data))
      .catch((err) => {
        if (!isCancel(err)) setError(err);
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false);
      });
    return () => controller.abort();
  }, [reference, email, booking]);

  return (
    <div className="container page">
      {loading && <Loader />}

      {!loading && error && <ErrorMessage message={error.message} />}

      {!loading && !booking && !error && (
        <div className="state-box">
          <p>We could not load this booking because the email is missing from the link.</p>
          <Link to="/manage-booking" className="btn">
            Find my booking
          </Link>
        </div>
      )}

      {booking && (
        <>
          <div style={{ textAlign: 'center', marginBottom: 24 }}>
            <CheckCircle size={48} color="#16a34a" />
            <h1>{booking.status === 'CANCELLED' ? 'Booking cancelled' : 'Booking confirmed!'}</h1>
            <p>Keep your booking reference and email. You need both to view or cancel this booking.</p>
          </div>

          <BookingSummary booking={booking} />

          <div className="confirm-actions" style={{ maxWidth: 640, margin: '20px auto 0' }}>
            <Link to="/manage-booking" className="btn btn-outline">
              Manage booking
            </Link>
            <Link to="/hotels" className="btn">
              Browse more hotels
            </Link>
          </div>
        </>
      )}
    </div>
  );
}