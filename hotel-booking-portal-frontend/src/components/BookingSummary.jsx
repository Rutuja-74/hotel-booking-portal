import { useState } from 'react';
import { Copy, Check } from 'lucide-react';
import { formatDate, formatPrice } from '../utils/format';
import '../styles/booking.css';

function Row({ label, value }) {
  return (
    <div className="summary-row">
      <span>{label}</span>
      <span>{value}</span>
    </div>
  );
}

export default function BookingSummary({ booking }) {
  const [copied, setCopied] = useState(false);
  const isCancelled = booking.status === 'CANCELLED';

  async function copyReference() {
    try {
      await navigator.clipboard.writeText(booking.bookingReference);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch {
      // Clipboard can be blocked by the browser; the reference is still visible to copy by hand
    }
  }

  return (
    <div className="summary-card">
      <div className="summary-head">
        <h2 style={{ margin: 0 }}>{booking.hotelName}</h2>
        <span className={`status-pill ${isCancelled ? 'status-cancelled' : 'status-confirmed'}`}>
          {booking.status}
        </span>
      </div>

      <div className="reference-box">
        <div>
          <div style={{ fontSize: '0.8rem', color: '#64748b' }}>Booking reference</div>
          <div className="reference-code">{booking.bookingReference}</div>
        </div>
        <button type="button" className="btn btn-outline" onClick={copyReference}>
          {copied ? <Check size={16} /> : <Copy size={16} />} {copied ? 'Copied' : 'Copy'}
        </button>
      </div>

      <Row label="Location" value={booking.city} />
      <Row label="Room" value={booking.roomType} />
      <Row label="Guest" value={booking.customerName} />
      <Row label="Email" value={booking.customerEmail} />
      <Row label="Check-in" value={formatDate(booking.checkIn)} />
      <Row label="Check-out" value={formatDate(booking.checkOut)} />
      <Row label="Nights" value={booking.nights} />
      <Row label="Price per night" value={formatPrice(booking.pricePerNight)} />
      <Row label="Total amount" value={formatPrice(booking.totalAmount)} />
      {isCancelled && booking.cancelledAt && (
        <Row label="Cancelled on" value={formatDate(booking.cancelledAt.slice(0, 10))} />
      )}
    </div>
  );
}