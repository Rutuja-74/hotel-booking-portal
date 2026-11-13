import client from './client';

// Remove empty values so we never send "location=" to the backend.
function clean(params) {
  const result = {};
  Object.entries(params).forEach(([key, value]) => {
    if (value !== '' && value !== null && value !== undefined) {
      result[key] = value;
    }
  });
  return result;
}

// GET /api/hotels/search?location=&checkIn=&checkOut=&category=&minPrice=&maxPrice=
export async function searchHotels(params, signal) {
  const response = await client.get('/hotels/search', { params: clean(params), signal });
  return response.data;
}