
import { useState } from 'react';
import { addDays, todayString } from '../utils/format';
import RecommendationList from '../components/RecommendationList';
import '../styles/recommend.css';

export default function SmartPicks() {
  const [city, setCity] = useState('');
  const [checkIn, setCheckIn] = useState('');
  const [checkOut, setCheckOut] = useState('');
  const [budget, setBudget] = useState('');
  const [error, setError] = useState('');
  const [query, setQuery] = useState(null);

  function handleSubmit(event) {
    event.preventDefault();

    if (!city.trim()) {
      return setError('Please enter a city.');
    }

    if (!checkIn || !checkOut) {
      return setError('Please select both check-in and check-out dates.');
    }

    if (checkIn < todayString()) {
      return setError('Check-in cannot be in the past.');
    }

    if (checkOut <= checkIn) {
      return setError('Check-out must be after check-in.');
    }

    if (budget && Number(budget) <= 0) {
      return setError('Budget must be greater than zero.');
    }

    setError('');

    setQuery({
      city: city.trim(),
      checkIn,
      checkOut,
      budget
    });
  }

  return (
    <div style={{ background: '#f1f5f9', minHeight: '100vh' }}>

      {/* Green Gradient Hero Section */}
      <section
        style={{
          background: 'linear-gradient(110deg, #0f766e, #0e7490)',
          padding: '32px 0 40px',
          color: '#ffffff'
        }}
      >
        <div className="container">

          <h1 style={{
            textAlign: 'center',
            color: '#ffffff',
            marginBottom: '10px'
          }}>
            Smart Stay Picks
          </h1>

          <p style={{
            textAlign: 'center',
            color: '#ffffff',
            marginBottom: '24px'
          }}>
            Tell us your destination, dates and budget.
            We pick the best available hotels for you and explain why.
          </p>

          {/* Search Form */}
          <form
            className="smart-form"
            onSubmit={handleSubmit}
            noValidate
            style={{
              maxWidth: 'none',
              background: '#ffffff',
              padding: '20px',
              borderRadius: '14px'
            }}
          >

            <div className="field">
              <label htmlFor="sp-city">City</label>
              <input
                id="sp-city"
                type="text"
                value={city}
                onChange={(e) => setCity(e.target.value)}
                placeholder="e.g. Goa, Mumbai, Jaipur"
              />
            </div>

            <div className="field">
              <label htmlFor="sp-checkIn">Check-in</label>
              <input
                id="sp-checkIn"
                type="date"
                min={todayString()}
                value={checkIn}
                onChange={(e) => setCheckIn(e.target.value)}
              />
            </div>

            <div className="field">
              <label htmlFor="sp-checkOut">Check-out</label>
              <input
                id="sp-checkOut"
                type="date"
                min={
                  checkIn
                    ? addDays(checkIn, 1)
                    : addDays(todayString(), 1)
                }
                value={checkOut}
                onChange={(e) => setCheckOut(e.target.value)}
              />
            </div>

            <div className="field">
              <label htmlFor="sp-budget">
                Budget per night (optional)
              </label>
              <input
                id="sp-budget"
                type="text"
                inputMode="numeric"
                value={budget}
                onChange={(e) =>
                  setBudget(e.target.value.replace(/\D/g, ''))
                }
                placeholder="e.g. 5000"
              />
            </div>

            <button type="submit" className="btn">
              Find my stay
            </button>

            {error && (
              <span className="form-error">
                {error}
              </span>
            )}

          </form>
        </div>
      </section>

      {/* Hotel Recommendations */}
      <div className="container page">
        {query && (
          <RecommendationList
            city={query.city}
            checkIn={query.checkIn}
            checkOut={query.checkOut}
            budget={query.budget}
            title="Our top picks for you"
          />
        )}
      </div>

    </div>
  );
}
