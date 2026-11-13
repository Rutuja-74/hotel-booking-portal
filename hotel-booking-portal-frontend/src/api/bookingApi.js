import client from './client';

// POST /api/bookings -> returns the created booking
export async function createBooking(payload) {
  const response = await client.post('/bookings', payload);
  return response.data;
}

// GET /api/bookings/{reference}?email=...
export async function getBooking(reference, email, signal) {
  const response = await client.get(`/bookings/${encodeURIComponent(reference)}`, {
    params: { email },
    signal,
  });
  return response.data;
}

// POST /api/bookings/{reference}/cancel with { email }
export async function cancelBooking(reference, email) {
  const response = await client.post(
    `/bookings/${encodeURIComponent(reference)}/cancel`,
    { email }
  );
  return response.data;
}