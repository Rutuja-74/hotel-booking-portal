
import { Link, NavLink } from 'react-router-dom';
import { Hotel } from 'lucide-react';

function Navbar() {
  return (
    <header className="navbar">
      <div className="container navbar-inner">
        <Link to="/" className="logo">
          <Hotel size={24} />
          <span>Hotel Booking Portal</span>
        </Link>

        <nav className="nav-links">
          <NavLink to="/" end>Home</NavLink>
          <NavLink to="/hotels">Hotels</NavLink>
          <NavLink to="/smart-picks">Smart Picks</NavLink>
          <NavLink to="/manage-booking">Manage Booking</NavLink>
        </nav>
      </div>
    </header>
  );
}

export default Navbar;
