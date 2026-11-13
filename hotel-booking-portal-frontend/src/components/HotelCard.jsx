import { Link } from 'react-router-dom';
import { MapPin, Star } from 'lucide-react';
import HotelImage from './HotelImage';
import { formatPrice } from '../utils/format';

const MAX_AMENITIES = 4;

function HotelCard({ hotel, dates }) {
  // Carry the selected dates to the details page.
  const query = dates.checkIn && dates.checkOut
    ? `?checkIn=${dates.checkIn}&checkOut=${dates.checkOut}`
    : '';

  // availableRooms is null when no dates were selected.
  const hasAvailability = hotel.availableRooms !== null && hotel.availableRooms !== undefined;
  const soldOut = hasAvailability && hotel.availableRooms === 0;
  const fewLeft = hasAvailability && hotel.availableRooms > 0 && hotel.availableRooms <= 3;

  const shownAmenities = hotel.amenities.slice(0, MAX_AMENITIES);
  const extraAmenities = hotel.amenities.length - shownAmenities.length;

  return (
    <article className="hotel-card">
      <div className="card-image-wrap">
        <HotelImage src={hotel.imageUrl} name={hotel.name} />
        <span className="badge badge-category">{hotel.category}</span>
        {soldOut && <span className="badge badge-soldout">Sold out</span>}
        {fewLeft && <span className="badge badge-few">Only {hotel.availableRooms} left</span>}
      </div>

      <div className="card-body">
        <div className="stars" aria-label={`${hotel.starRating} star hotel`}>
          {Array.from({ length: hotel.starRating }).map((_, index) => (
            <Star key={index} size={14} fill="currentColor" />
          ))}
        </div>
        <h3>{hotel.name}</h3>
        <p className="muted location">
          <MapPin size={14} /> {hotel.city}, {hotel.state}
        </p>
        <p className="description">{hotel.description}</p>

        <ul className="amenities">
          {shownAmenities.map((amenity) => (
            <li key={amenity}>{amenity}</li>
          ))}
          {extraAmenities > 0 && <li className="more">+{extraAmenities} more</li>}
        </ul>

        <div className="card-footer">
          <div>
            <div className="price">
              <strong>{formatPrice(hotel.pricePerNight)}</strong>
              <span className="muted"> / night</span>
            </div>
            {hotel.totalAmount !== null && hotel.totalAmount !== undefined && (
              <div className="price-total">
                {formatPrice(hotel.totalAmount)} total for {hotel.nights}{' '}
                {hotel.nights === 1 ? 'night' : 'nights'}
              </div>
            )}
          </div>
          <Link to={`/hotels/${hotel.id}${query}`} className="btn">
            View details
          </Link>
        </div>
      </div>
    </article>
  );
}

export default HotelCard;