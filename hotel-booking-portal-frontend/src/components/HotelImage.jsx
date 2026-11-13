import { useState } from 'react';
import { Hotel } from 'lucide-react';

// Shows the hotel photo. If the file is missing, it shows a styled placeholder instead.
function HotelImage({ src, name }) {
  const [failed, setFailed] = useState(false);

  if (!src || failed) {
    return (
      <div className="card-image image-placeholder" role="img" aria-label={name}>
        <Hotel size={36} />
        <span>{name}</span>
      </div>
    );
  }

  return (
    <img
      className="card-image"
      src={src}
      alt={name}
      loading="lazy"
      onError={() => setFailed(true)}
    />
  );
}

export default HotelImage;