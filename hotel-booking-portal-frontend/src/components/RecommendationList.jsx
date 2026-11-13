
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getRecommendations } from '../api/RecommendationApi';
import { isCancel } from '../api/client';
import { formatPrice } from '../utils/format';
import Loader from './Loader';
import ErrorMessage from './ErrorMessage';
import '../styles/Recommend.css';

// Shows recommended hotels for the selected city, dates and budget.
export default function RecommendationList({
  city,
  checkIn,
  checkOut,
  budget,
  hotelId,
  title
}) {
  const [picks, setPicks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const controller = new AbortController();

    setLoading(true);
    setError(null);

    getRecommendations(
      { city, checkIn, checkOut, budget, hotelId },
      controller.signal
    )
      .then((data) => setPicks(data))
      .catch((err) => {
        if (!isCancel(err)) {
          setError(err);
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      });

    return () => controller.abort();

  }, [city, checkIn, checkOut, budget, hotelId]);

  return (
    <div>
      {title && <h2>{title}</h2>}

      {loading && <Loader />}

      {!loading && error && (
        <ErrorMessage message={error.message} />
      )}

      {!loading && !error && picks.length === 0 && (
        <div className="state-box">
          <p>
            {budget
              ? `No hotels found in ${city} within your budget. Please increase your budget or try different dates.`
              : `No hotels are available in ${city} for these dates. Please try another city or different dates.`}
          </p>
        </div>
      )}

      {!loading && !error && picks.length > 0 && (
        <div className="picks-grid">
          {picks.map((pick, index) => {
            const hotel = pick.hotel;

            return (
              <Link
                key={hotel.id}
                to={`/hotels/${hotel.id}?checkIn=${checkIn}&checkOut=${checkOut}`}
                className={`pick-card ${index === 0 ? 'best' : ''}`}
              >
                {index === 0 && (
                  <span className="pick-badge">Best match</span>
                )}

                <div className="pick-top">
                  <strong>{hotel.name}</strong>
                  <span className="pick-stars">
                    {'★'.repeat(hotel.starRating)}
                  </span>
                </div>

                <div className="pick-sub">
                  {hotel.city}, {hotel.state} &middot; {hotel.category}
                </div>

                <div className="pick-price">
                  {formatPrice(hotel.pricePerNight)} / night
                  {' · '}
                  {formatPrice(hotel.totalAmount)}
                  {' for '}
                  {hotel.nights} night{hotel.nights > 1 ? 's' : ''}
                </div>

                <ul className="pick-reasons">
                  {pick.reasons.map((reason) => (
                    <li key={reason}>{reason}</li>
                  ))}
                </ul>
              </Link>
            );
          })}
        </div>
      )}
    </div>
  );
}
