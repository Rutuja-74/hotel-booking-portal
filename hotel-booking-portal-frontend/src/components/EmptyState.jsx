import { SearchX } from 'lucide-react';

function EmptyState({ title, text, actionLabel, onAction }) {
  return (
    <div className="state-box">
      <SearchX size={36} />
      <h3>{title}</h3>
      <p>{text}</p>
      {onAction && (
        <button type="button" className="btn btn-outline" onClick={onAction}>
          {actionLabel}
        </button>
      )}
    </div>
  );
}

export default EmptyState;