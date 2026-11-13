
import client from './client';

// GET /api/recommendations?city=Goa&checkIn=&checkOut=&budget=&hotelId=

export async function getRecommendations(
  { city, checkIn, checkOut, budget, hotelId },
  signal
) {
  const params = {
    city,
    checkIn,
    checkOut
  };

  if (budget) {
    params.budget = budget;
  }

  if (hotelId) {
    params.hotelId = hotelId;
  }

  const response = await client.get('/recommendations', {
    params,
    signal
  });

  return response.data;
}
