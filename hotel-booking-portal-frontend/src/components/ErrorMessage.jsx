import { TriangleAlert } from 'lucide-react';

function ErrorMessage({ message, onRetry }) {
  return (
    <div className="state-box error-box" role="alert">
      <TriangleAlert size={36} />
      <h3>Something went wrong</h3>
      <p>{message}</p>
      {onRetry && (
        <button type="button" className="btn" onClick={onRetry}>
          Try again
        </button>
      )}
    </div>
  );
}

export default ErrorMessage;