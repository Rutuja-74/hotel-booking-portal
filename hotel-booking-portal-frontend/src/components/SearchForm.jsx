import { useState } from 'react';
import { Search, MapPin, CalendarDays } from 'lucide-react';
import { addDays, todayString } from '../utils/format';

// The parent decides what happens on search (onSearch), so the form works on any page.
function SearchForm({ initialValues = {}, onSearch }) {
  const [location, setLocation] = useState(initialValues.location || '');
  const [checkIn, setCheckIn] = useState(initialValues.checkIn || '');
  const [checkOut, setCheckOut] = useState(initialValues.checkOut || '');
  const [error, setError] = useState('');
  const today = todayString();

  function handleCheckInChange(value) {
    setCheckIn(value);
    // If the old check-out is no longer after the new check-in, clear it.
    if (value && checkOut && checkOut <= value) {
      setCheckOut('');
    }
  }

  function handleSubmit(event) {
    event.preventDefault();

    if ((checkIn && !checkOut) || (!checkIn && checkOut)) {
      setError('Please select both check-in and check-out dates.');
      return;
    }
    if (checkIn && checkIn < today) {
      setError('Check-in cannot be in the past.');
      return;
    }
    if (checkIn && checkOut <= checkIn) {
      setError('Check-out must be after check-in.');
      return;
    }

    setError('');
    onSearch({ location: location.trim(), checkIn, checkOut });
  }

  return (
    <form className="search-form" onSubmit={handleSubmit} noValidate>
      <div className="search-grid">
        <div className="field">
          <label htmlFor="location"><MapPin size={14} /> Location</label>
          <input
            id="location"
            type="text"
            placeholder="City or state, e.g. Goa"
            value={location}
            onChange={(e) => setLocation(e.target.value)}
          />
        </div>

        <div className="field">
          <label htmlFor="checkIn"><CalendarDays size={14} /> Check-in</label>
          <input
            id="checkIn"
            type="date"
            min={today}
            value={checkIn}
            onChange={(e) => handleCheckInChange(e.target.value)}
          />
        </div>

        <div className="field">
          <label htmlFor="checkOut"><CalendarDays size={14} /> Check-out</label>
          <input
            id="checkOut"
            type="date"
            min={checkIn ? addDays(checkIn, 1) : addDays(today, 1)}
            value={checkOut}
            onChange={(e) => setCheckOut(e.target.value)}
          />
        </div>

        <button type="submit" className="btn search-btn">
          <Search size={18} /> Search
        </button>
      </div>
      {error && <p className="form-error" role="alert">{error}</p>}
    </form>
  );
}

export default SearchForm;