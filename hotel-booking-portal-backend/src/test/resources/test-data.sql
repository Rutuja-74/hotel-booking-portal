INSERT INTO hotels (id, name, city, state, description, amenities, category, star_rating, image_url) VALUES
(1, 'Test Hotel One', 'Goa', 'Goa', 'Test hotel with a single room.', 'Free WiFi', 'STANDARD', 3, '/images/test-one.jpg'),
(2, 'Test Hotel Three', 'Jaipur', 'Rajasthan', 'Test hotel with three rooms.', 'Free WiFi', 'STANDARD', 3, '/images/test-three.jpg');

INSERT INTO rooms (id, hotel_id, room_type, price_per_night, total_rooms, max_guests) VALUES
(1, 1, 'Standard Room', 2000.00, 1, 2),
(2, 2, 'Standard Room', 3000.00, 3, 2);