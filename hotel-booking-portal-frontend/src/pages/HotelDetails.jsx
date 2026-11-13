import { useEffect, useState } from 'react';
import { Link, useParams, useSearchParams } from 'react-router-dom';
import { MapPin, Star, Users, BedDouble, Check } from 'lucide-react';
import { getHotel } from '../api/hotelApi';
import { isCancel } from '../api/client';
import { addDays, todayString, formatPrice } from '../utils/format';
import Loader from '../components/Loader';
import ErrorMessage from '../components/ErrorMessage';
import BookingForm from '../components/BookingForm';
import '../styles/booking.css';

// Hotel photo with a styled fallback when the image file does not exist
function DetailsImage({ src, alt }) {
  const [failed, setFailed] = useState(false);
  if (!src || failed) {
    return (
      <div className="details-image placeholder">
        <BedDouble size={56} />
      </div>
    );
  }
  return <img className="details-image" src={src} alt={alt} onError={() => setFailed(true)} />;
}

// Small date picker used when dates are missing or the user wants to change them
function DateForm({ initialCheckIn, initialCheckOut, onApply }) {
  const [checkIn, setCheckIn] = useState(initialCheckIn);
  const [checkOut, setCheckOut] = useState(initialCheckOut);
  const [error, setError] = useState('');

  function handleSubmit(event) {
    event.preventDefault();
    if (!checkIn || !checkOut) return setError('Please select both check-in and check-out dates.');
    if (checkIn < todayString()) return setError('Check-in cannot be in the past.');
    if (checkOut <= checkIn) return setError('Check-out must be after check-in.');
    setError('');
    onApply(checkIn, checkOut);
  }

  return (
    <form className="date-form" onSubmit={handleSubmit} noValidate>
      <div className="field">
        <label htmlFor="d-checkIn">Check-in</label>
        <input
          id="d-checkIn"
          type="date"
          min={todayString()}
          value={checkIn}
          onChange={(e) => setCheckIn(e.target.value)}
        />
      </div>
      <div className="field">
        <label htmlFor="d-checkOut">Check-out</label>
        <input
          id="d-checkOut"
          type="date"
          min={checkIn ? addDays(checkIn, 1) : addDays(todayString(), 1)}
          value={checkOut}
          onChange={(e) => setCheckOut(e.target.value)}
        />
      </div>
      {error && <span className="form-error">{error}</span>}
      <button type="submit" className="btn btn-outline">
        Check availability
      </button>
    </form>
  );
}

export default function HotelDetails() {
  const { id } = useParams();
  const [searchParams, setSearchParams] = useSearchParams();
  const checkIn = searchParams.get('checkIn') || '';
  const checkOut = searchParams.get('checkOut') || '';
  const hasDates = Boolean(checkIn && checkOut);

  const [hotel, setHotel] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  // Changing this number re-runs the fetch (used by "Try again" and after a sold-out error)
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    const controller = new AbortController();
    setLoading(true);
    setError(null);

    getHotel(id, { checkIn, checkOut }, controller.signal)
      .then((data) => setHotel(data))
      .catch((err) => {
        if (!isCancel(err)) setError(err);
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false);
      });

    return () => controller.abort();
  }, [id, checkIn, checkOut, reloadKey]);

  function applyDates(newCheckIn, newCheckOut) {
    setSearchParams({ checkIn: newCheckIn, checkOut: newCheckOut });
  }

  const reload = () => setReloadKey((n) => n + 1);

  return (
    <div className="container page">
      <Link to="/hotels" className="back-link">
        &larr; Back to hotels
      </Link>

      {loading && <Loader />}
      {!loading && error && <ErrorMessage message={error.message} onRetry={reload} />}

      {!loading && !error && hotel && (
        <div className="details-layout">
          {/* Left column: hotel information */}
          <div>
            <DetailsImage src={hotel.imageUrl} alt={hotel.name} />
            <h1 className="details-title">{hotel.name}</h1>
            <div className="details-meta">
              <span>
                <MapPin size={16} /> {hotel.city}, {hotel.state}
              </span>
              <span className="stars" aria-label={`${hotel.starRating} star hotel`}>
                {Array.from({ length: hotel.starRating }).map((_, i) => (
                  <Star key={i} size={16} fill="currentColor" />
                ))}
              </span>
              <span>
                <BedDouble size={16} /> {hotel.roomType}
              </span>
              <span>
                <Users size={16} /> Up to {hotel.maxGuests} guests
              </span>
            </div>

            <div className="details-section">
              <h2>About this hotel</h2>
              <p>{hotel.description}</p>
            </div>

            <div className="details-section">
              <h2>Amenities</h2>
              <ul className="amenity-list">
                {hotel.amenities.map((amenity) => (
                  <li key={amenity}>
                    <Check size={14} /> {amenity}
                  </li>
                ))}
              </ul>
            </div>
          </div>

          {/* Right column: dates, price, availability and booking form */}
          <aside className="booking-panel">
            <h2>{formatPrice(hotel.pricePerNight)} / night</h2>

            <DateForm
              key={`${checkIn}-${checkOut}`}
              initialCheckIn={checkIn}
              initialCheckOut={checkOut}
              onApply={applyDates}
            />

            {!hasDates && (
              <p>Select your check-in and check-out dates to see availability and the total price.</p>
            )}

            {hasDates && hotel.availableRooms === 0 && (
              <p className="availability-note none">
                Sold out for these dates. Please try different dates.
              </p>
            )}

            {hasDates && hotel.availableRooms > 0 && (
              <>
                <p className={`availability-note ${hotel.availableRooms <= 3 ? 'low' : ''}`}>
                  {hotel.availableRooms <= 3
                    ? `Hurry! Only ${hotel.availableRooms} room${hotel.availableRooms > 1 ? 's' : ''} left`
                    : `${hotel.availableRooms} rooms available`}
                </p>

                <div className="price-row">
                  <span>
                    {formatPrice(hotel.pricePerNight)} x {hotel.nights} night
                    {hotel.nights > 1 ? 's' : ''}
                  </span>
                  <span>{formatPrice(hotel.totalAmount)}</span>
                </div>
                <div className="price-row total">
                  <span>Total amount</span>
                  <span>{formatPrice(hotel.totalAmount)}</span>
                </div>

                <h2 style={{ marginTop: 20 }}>Your details</h2>
                <BookingForm
                  hotel={hotel}
                  checkIn={checkIn}
                  checkOut={checkOut}
                  onSoldOut={reload}
                />
              </>
            )}
          </aside>
        </div>
      )}
    </div>
  );
}