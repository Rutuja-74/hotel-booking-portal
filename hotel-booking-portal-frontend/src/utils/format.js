// Date helpers use LOCAL date parts on purpose.
// toISOString() uses UTC and can give "yesterday" in India around midnight.

function pad(number) {
  return String(number).padStart(2, '0');
}

export function toDateString(date) {
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

export function todayString() {
  return toDateString(new Date());
}

function parseDate(dateString) {
  const [year, month, day] = dateString.split('-').map(Number);
  return new Date(year, month - 1, day);
}

export function addDays(dateString, days) {
  const date = parseDate(dateString);
  date.setDate(date.getDate() + days);
  return toDateString(date);
}

export function nightsBetween(checkIn, checkOut) {
  const millisecondsPerDay = 24 * 60 * 60 * 1000;
  return Math.round((parseDate(checkOut) - parseDate(checkIn)) / millisecondsPerDay);
}

export function formatDate(dateString) {
  return parseDate(dateString).toLocaleDateString('en-IN', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  });
}

const priceFormatter = new Intl.NumberFormat('en-IN', {
  style: 'currency',
  currency: 'INR',
  maximumFractionDigits: 0,
});

export function formatPrice(amount) {
  return priceFormatter.format(amount);
}