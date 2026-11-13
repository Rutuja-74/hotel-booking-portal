import client from './client';

// Remove empty values so we never send ?location=&category= to the backend
function removeEmpty(params) {
  const cleaned = {};
  Object.entries(params || {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      cleaned[key] = value;
    }
  });
  return cleaned;
}

// GET /api/hotels/search
export async function searchHotels(params, signal) {
  const response = await client.get('/hotels/search', {
    params: removeEmpty(params),
    signal,
  });
  return response.data;
}

// GET /api/hotels/{id}?checkIn=&checkOut=  (dates optional)
export async function getHotel(id, params, signal) {
  const response = await client.get(`/hotels/${id}`, {
    params: removeEmpty(params),
    signal,
  });
  return response.data;
}