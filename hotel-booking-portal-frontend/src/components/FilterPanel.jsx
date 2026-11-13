import { useState } from 'react';

const CATEGORIES = [
  { value: '', label: 'All categories' },
  { value: 'BUDGET', label: 'Budget' },
  { value: 'STANDARD', label: 'Standard' },
  { value: 'PREMIUM', label: 'Premium' },
  { value: 'LUXURY', label: 'Luxury' },
];

// The parent gives this panel a "key", so it resets when the URL filters change.
function FilterPanel({ values, onApply, onClear }) {
  const [category, setCategory] = useState(values.category || '');
  const [minPrice, setMinPrice] = useState(values.minPrice || '');
  const [maxPrice, setMaxPrice] = useState(values.maxPrice || '');
  const [error, setError] = useState('');

  function handleSubmit(event) {
    event.preventDefault();
    if (minPrice && maxPrice && Number(minPrice) > Number(maxPrice)) {
      setError('Minimum price cannot be more than maximum price.');
      return;
    }
    setError('');
    onApply({ category, minPrice, maxPrice });
  }

  return (
    <form className="filter-panel" onSubmit={handleSubmit} noValidate>
      <div className="filter-grid">
        <div className="field">
          <label htmlFor="category">Category</label>
          <select id="category" value={category} onChange={(e) => setCategory(e.target.value)}>
            {CATEGORIES.map((item) => (
              <option key={item.value} value={item.value}>{item.label}</option>
            ))}
          </select>
        </div>

        <div className="field">
          <label htmlFor="minPrice">Min price per night (₹)</label>
          <input
            id="minPrice"
            type="number"
            min="0"
            placeholder="0"
            value={minPrice}
            onChange={(e) => setMinPrice(e.target.value)}
          />
        </div>

        <div className="field">
          <label htmlFor="maxPrice">Max price per night (₹)</label>
          <input
            id="maxPrice"
            type="number"
            min="0"
            placeholder="10000"
            value={maxPrice}
            onChange={(e) => setMaxPrice(e.target.value)}
          />
        </div>

        <div className="filter-actions">
          <button type="submit" className="btn">Apply filters</button>
          <button type="button" className="btn btn-outline" onClick={onClear}>Clear</button>
        </div>
      </div>
      {error && <p className="form-error" role="alert">{error}</p>}
    </form>
  );
}

export default FilterPanel;