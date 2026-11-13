import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import SearchForm from '../components/SearchForm';
import FilterPanel from '../components/FilterPanel';
import HotelCard from '../components/HotelCard';
import Loader from '../components/Loader';
import ErrorMessage from '../components/ErrorMessage';
import EmptyState from '../components/EmptyState';
import { searchHotels } from '../api/hotelApi';
import { isCancel } from '../api/client';
import { formatDate, nightsBetween } from '../utils/format';

// Sorting is done in the browser on the list we already have.
const SORTERS = {
  price_asc: (a, b) => a.pricePerNight - b.pricePerNight,
  price_desc: (a, b) => b.pricePerNight - a.pricePerNight,
  rating_desc: (a, b) => b.starRating - a.starRating,
};

function setParam(params, key, value) {
  if (value) {
    params.set(key, value);
  } else {
    params.delete(key);
  }
}

function Hotels() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [hotels, setHotels] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [sort, setSort] = useState('default');
  const [reloadKey, setReloadKey] = useState(0);

  const location = searchParams.get('location') || '';
  const checkIn = searchParams.get('checkIn') || '';
  const checkOut = searchParams.get('checkOut') || '';
  const category = searchParams.get('category') || '';
  const minPrice = searchParams.get('minPrice') || '';
  const maxPrice = searchParams.get('maxPrice') || '';
  const queryString = searchParams.toString();

  // Load hotels whenever the URL query (search or filters) changes.
  useEffect(() => {
    const controller = new AbortController();
    const params = Object.fromEntries(new URLSearchParams(queryString));

    setLoading(true);
    setError('');

    searchHotels(params, controller.signal)
      .then((data) => {
        setHotels(data);
        setLoading(false);
      })
      .catch((err) => {
        if (isCancel(err)) return; // a newer request replaced this one
        setError(err.message);
        setLoading(false);
      });

    // Cancel the old request if the query changes or the page closes.
    return () => controller.abort();
  }, [queryString, reloadKey]);

  function handleSearch(values) {
    const next = new URLSearchParams(searchParams); // keeps existing filters
    setParam(next, 'location', values.location);
    setParam(next, 'checkIn', values.checkIn);
    setParam(next, 'checkOut', values.checkOut);
    setSearchParams(next);
  }

  function handleApplyFilters(values) {
    const next = new URLSearchParams(searchParams); // keeps the search
    setParam(next, 'category', values.category);
    setParam(next, 'minPrice', values.minPrice);
    setParam(next, 'maxPrice', values.maxPrice);
    setSearchParams(next);
  }

  function handleClearFilters() {
    const next = new URLSearchParams(searchParams);
    next.delete('category');
    next.delete('minPrice');
    next.delete('maxPrice');
    setSearchParams(next);
  }

  function handleClearAll() {
    setSearchParams(new URLSearchParams());
  }

  const visibleHotels = sort === 'default' ? hotels : [...hotels].sort(SORTERS[sort]);
  const hasDates = checkIn && checkOut;
  const nights = hasDates ? nightsBetween(checkIn, checkOut) : 0;

  return (
    <>
      <section className="search-strip">
        <div className="container">
          <SearchForm
            key={`${location}|${checkIn}|${checkOut}`}
            initialValues={{ location, checkIn, checkOut }}
            onSearch={handleSearch}
          />
        </div>
      </section>

      <div className="container page">
        <FilterPanel
          key={`${category}|${minPrice}|${maxPrice}`}
          values={{ category, minPrice, maxPrice }}
          onApply={handleApplyFilters}
          onClear={handleClearFilters}
        />

        {loading && <Loader text="Finding hotels..." />}

        {!loading && error && (
          <ErrorMessage message={error} onRetry={() => setReloadKey((key) => key + 1)} />
        )}

        {!loading && !error && hotels.length === 0 && (
          <EmptyState
            title="No hotels found"
            text="Try a different location or dates, or clear your filters."
            actionLabel="Clear search and filters"
            onAction={handleClearAll}
          />
        )}

        {!loading && !error && hotels.length > 0 && (
          <>
            <div className="results-header">
              <div>
                <strong>
                  {hotels.length} {hotels.length === 1 ? 'hotel' : 'hotels'} found
                </strong>
                {location && <span className="muted"> in {location}</span>}
                {hasDates && (
                  <p className="muted small">
                    {formatDate(checkIn)} to {formatDate(checkOut)} ({nights}{' '}
                    {nights === 1 ? 'night' : 'nights'})
                  </p>
                )}
              </div>
              <div className="field sort-field">
                <label htmlFor="sort">Sort by</label>
                <select id="sort" value={sort} onChange={(e) => setSort(e.target.value)}>
                  <option value="default">Recommended</option>
                  <option value="price_asc">Price: low to high</option>
                  <option value="price_desc">Price: high to low</option>
                  <option value="rating_desc">Star rating</option>
                </select>
              </div>
            </div>

            <div className="hotel-grid">
              {visibleHotels.map((hotel) => (
                <HotelCard key={hotel.id} hotel={hotel} dates={{ checkIn, checkOut }} />
              ))}
            </div>
          </>
        )}
      </div>
    </>
  );
}

export default Hotels;