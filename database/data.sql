-- =====================================================
-- Hotel Booking Portal - Sample data
-- Run AFTER schema.sql
-- Hotel names are fictional. Prices are in INR per night.
-- =====================================================

USE hotel_booking_db;

INSERT INTO hotels (id, name, city, state, description, amenities, category, star_rating, image_url) VALUES
(1, 'Azure Sands Beach Resort', 'Goa', 'Goa',
 'A beachfront resort a short walk from the shore, with spacious sea-view rooms, a lagoon pool and a seafood restaurant serving fresh coastal dishes.',
 'Free WiFi,Swimming Pool,Beach Access,Spa,Breakfast Included,Restaurant',
 'LUXURY', 5, '/images/hotels/azure-sands-goa.jpg'),

(2, 'Palm Grove Stay', 'Goa', 'Goa',
 'A friendly, budget-conscious stay near the beaches with clean air-conditioned rooms, a leafy courtyard and easy access to local cafes and markets.',
 'Free WiFi,Air Conditioning,Parking,Breakfast Included',
 'BUDGET', 3, '/images/hotels/palm-grove-goa.jpg'),

(3, 'Rajmahal Heritage Haveli', 'Jaipur', 'Rajasthan',
 'A restored royal haveli with carved sandstone arches, a courtyard pool and evening folk performances, minutes from the Pink City attractions.',
 'Free WiFi,Swimming Pool,Restaurant,Cultural Evenings,Airport Shuttle,Spa',
 'LUXURY', 5, '/images/hotels/rajmahal-haveli-jaipur.jpg'),

(4, 'Marine Drive Business Hotel', 'Mumbai', 'Maharashtra',
 'A modern business hotel near the waterfront with executive rooms, a rooftop restaurant, a fitness centre and quick access to the business districts.',
 'Free WiFi,Gym,Restaurant,Airport Shuttle,Breakfast Included,Air Conditioning',
 'PREMIUM', 4, '/images/hotels/marine-drive-mumbai.jpg'),

(5, 'Himalayan Pine Retreat', 'Manali', 'Himachal Pradesh',
 'A cosy pine-wood retreat with mountain-view rooms, a warm bonfire lounge and a restaurant serving Himachali and North Indian cuisine.',
 'Free WiFi,Room Heater,Bonfire,Restaurant,Parking,Mountain View',
 'STANDARD', 3, '/images/hotels/himalayan-pine-manali.jpg'),

(6, 'Backwater Bliss Resort', 'Kochi', 'Kerala',
 'A tranquil lakeside resort with lake-view rooms, an Ayurvedic spa and backwater cruises, set among coconut palms.',
 'Free WiFi,Swimming Pool,Ayurvedic Spa,Breakfast Included,Backwater Cruise,Restaurant',
 'PREMIUM', 4, '/images/hotels/backwater-bliss-kochi.jpg'),

(7, 'Lakeview Budget Inn', 'Udaipur', 'Rajasthan',
 'A simple, welcoming inn close to the old city with a rooftop cafe offering sunset views over the lake and spotless standard rooms.',
 'Free WiFi,Rooftop Cafe,Breakfast Included,Parking',
 'BUDGET', 3, '/images/hotels/lakeview-inn-udaipur.jpg');

INSERT INTO rooms (id, hotel_id, room_type, price_per_night, total_rooms, max_guests) VALUES
(1, 1, 'Deluxe Sea View Room',   7500.00,  8, 3),
(2, 2, 'Standard AC Room',       2400.00,  9, 2),
(3, 3, 'Heritage Suite',         8500.00,  6, 3),
(4, 4, 'Executive Room',         6800.00, 10, 2),
(5, 5, 'Mountain View Room',     3200.00,  7, 3),
(6, 6, 'Lake View Room',         4800.00,  5, 3),
(7, 7, 'Standard Room',          1800.00, 12, 2);