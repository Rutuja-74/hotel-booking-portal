import { Link } from 'react-router-dom';

function NotFound() {
  return (
    <div className="container page">
      <div className="state-box">
        <h2>Page not found</h2>
        <p>The page you are looking for does not exist yet.</p>
        <Link to="/" className="btn">Go to home</Link>
      </div>
    </div>
  );
}

export default NotFound;