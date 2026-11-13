import { Link, useNavigate } from 'react-router-dom';
import SearchForm from '../components/SearchForm';

const DESTINATIONS = ['Goa', 'Jaipur', 'Mumbai', 'Manali', 'Kochi', 'Udaipur'];

function Home() {
  const navigate = useNavigate();

  function handleSearch(values) {
    const params = new URLSearchParams();
    Object.entries(values).forEach(([key, value]) => {
      if (value) params.set(key, value);
    });
    const query = params.toString();
    navigate(query ? `/hotels?${query}` : '/hotels');
  }

  return (
    <>
      <section className="hero">
        <div className="container">
          <h1>Find your perfect stay</h1>
          <p>Search hotels by location and dates, see the total price upfront, and book in minutes.</p>
          <SearchForm onSearch={handleSearch} />
        </div>
      </section>

      <section className="container page">
        <h2>Popular destinations</h2>
        <div className="chips">
          {DESTINATIONS.map((city) => (
            <Link key={city} to={`/hotels?location=${city}`} className="chip">
              {city}
            </Link>
          ))}
        </div>

        <h2 className="section-gap">How it works</h2>
        <div className="steps">
          <div className="step"><strong>1. Search</strong><p>Choose a location and your check-in and check-out dates.</p></div>
          <div className="step"><strong>2. Book</strong><p>Pick a hotel, enter your name and email and see the total amount.</p></div>
          <div className="step"><strong>3. Manage</strong><p>Use your booking reference and email to view or cancel any time.</p></div>
        </div>
      </section>
    </>
  );
}

export default Home;