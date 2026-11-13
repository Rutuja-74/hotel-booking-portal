# Design Document

## 1. Architecture
```
React (Vite)  --HTTP/JSON-->  Spring Boot REST API  --JPA/Hibernate-->  MySQL
 localhost:5173  (/api proxy)      localhost:8080                       hotel_booking_db
```
- **Frontend:** React pages call the API through one Axios client (`src/api`), which converts failures into friendly errors. React Router handles pages: Home, Hotels, Hotel Details, Confirmation, Manage Booking.
- **Backend:** layered: `controller` (HTTP) → `service` (business rules) → `repository` (Spring Data JPA) → MySQL. DTOs (Java records) separate the API from entities. A global exception handler returns a consistent error JSON.
- **Database:** MySQL is the single source of truth for hotels, rooms and bookings.
- In development Vite proxies `/api` to port 8080, so no CORS configuration is needed.

## 2. Database design
- **hotels**: id, name, city, state, description, amenities (comma-separated text), category (BUDGET/STANDARD/PREMIUM/LUXURY), star_rating, image_url
- **rooms**: id, hotel_id (FK → hotels), room_type, price_per_night, total_rooms, max_guests
- **bookings**: id, booking_reference (unique), room_id (FK → rooms), customer_name, customer_email, check_in, check_out, nights, total_amount, status (CONFIRMED/CANCELLED), created_at, cancelled_at

Relationships: Hotel 1—N Room, Room 1—N Booking.
Constraints (in `schema.sql`): unique booking reference, foreign keys, unique (hotel, room type), CHECK constraints (star rating 1–5, valid category, price > 0, `check_out > check_in`, nights > 0, status IN CONFIRMED/CANCELLED), and an index on `(room_id, status, check_in, check_out)` for fast availability queries. Hibernate runs with `ddl-auto=validate`: it only checks the entities match the tables and never changes the schema.
Availability is **not stored as a counter**. It is computed from CONFIRMED bookings, so cancelling just changes the status and availability is automatically correct (nothing to forget to update).
The booking stores `nights` and `total_amount` at booking time, so later price changes do not alter old bookings.

## 3. API design
| Endpoint | Purpose |
|---|---|
| `GET /api/hotels` | list all hotels |
| `GET /api/hotels/search?location&category&minPrice&maxPrice&checkIn&checkOut` | filtered search; with dates also returns availableRooms, nights, totalAmount |
| `GET /api/hotels/{id}?checkIn&checkOut` | hotel details (+ availability for dates) |
| `GET /api/rooms/availability?roomId&checkIn&checkOut` | availability and total for a stay |
| `POST /api/bookings` | create a booking (201) |
| `GET /api/bookings/{reference}?email=` | retrieve a booking |
| `POST /api/bookings/{reference}/cancel` | cancel a booking (body: email) |
| Recommendation endpoint | Smart Stay Recommendation (see section 7) |

Errors share one shape: `{ timestamp, status, error, message, path, fieldErrors }`. Validation → 400 with `fieldErrors`; not found / wrong email → 404; sold out → 409.

## 4. Booking logic
- **Validation:** name (required, ≤100), email (valid, ≤150), check-out after check-in, check-in not in the past. Validated on the frontend for quick feedback and again on the backend (the backend is the authority).
- **Pricing:** `nights = checkOut − checkIn` (days); `total = nights × pricePerNight`. Always calculated on the backend; the browser only displays it.
- **Availability:** the database query finds CONFIRMED bookings that overlap the stay (`existingIn < newOut AND existingOut > newIn`). Then, for each night of the requested stay, the code counts how many of those bookings use that night; the highest count is the rooms in use. `available = totalRooms − thatMax` (never below 0). If `available <= 0`, `RoomNotAvailableException` (409). This is more accurate than a plain overlap count, because two short stays that do not overlap each other can share one room. Search with dates only returns hotels that still have a free room.
- **Cancellation:** requires reference + matching email; the booking row is locked, then status becomes CANCELLED and `cancelledAt` is set. Cancelled bookings are ignored by availability, so the room is released. Cancelling twice, or after the check-in date, returns a clear error (409). A stay is limited to 30 nights.

## 5. Design decisions, alternatives and trade-offs
- **Computed availability vs. a counter per day:** a counter table is faster for huge data but easy to get out of sync. Computing from bookings is simpler and always correct at this scale. Trade-off: slower with millions of bookings (there is already an index on room, status and dates).
- **Reference + email instead of login:** meets the requirement without authentication; email acts as a lightweight proof of ownership. Less secure than real accounts.
- **DTO records, no Lombok:** clear and explicit; a bit more typing.
- **Filtering/sorting in the service layer:** the backend loads rooms with their hotel and filters in memory (fine for a small catalogue). With large data this should move into SQL with pagination. The hotels page also sorts in the browser for instant feedback.
- **One room type per hotel:** keeps the model small; supporting several types only needs more rows in `rooms`.

## 6. Simultaneous bookings (two users, last room)
Booking runs in one transaction. The service first loads the room row with a **pessimistic write lock** (`SELECT ... FOR UPDATE` via `RoomRepository.findByIdForUpdate`); this is the first database statement. The second request waits until the first commits; it then recalculates availability, sees no room left, and gets a 409 "not available" response. So the last room can never be sold twice. An automated integration test (`simultaneousBookings_neverExceedRoomCapacity`) runs several bookings at once and checks capacity is never exceeded. Cancellation also locks the booking row, so two cancel requests are handled one after the other.
Alternatives: optimistic locking with a version column and retry (better when conflicts are rare), or a unique constraint per room-night. Pessimistic locking was chosen because it is simple to reason about for this scale.

## 7. Surprise feature: Smart Stay Picks
- **What:** a page (`/smart-picks`) where the guest enters a city, dates and an optional budget per night. It shows up to **3 hotels** that are available for those dates, best first, with a "Best match" badge and short reasons (for example "Available for your dates", "Within your budget", "4-star rated", "Plenty of rooms left"). Each card shows the price per night and the total for the stay, and links to the hotel details page with the dates already filled in. If a `hotelId` is passed, the same endpoint works as "similar hotels": it excludes that hotel and prefers ones in the same city and category.
- **Why:** choosing among many hotels is the hardest part of booking, and a plain list does not say which one fits. Ranked picks with reasons save time and make the choice easier to trust. If nothing fits the budget, the page says so and tells the user what to change.
- **How it works (backend):** `GET /api/recommendations?city&checkIn&checkOut&budget&hotelId` → `RecommendationController` → `RecommendationService`.
  1. Validate input (city and both dates required, budget > 0) → 400 otherwise.
  2. Reuse `HotelService.searchHotels` for the city and dates, so only hotels with a free room for the whole stay are considered (no duplicated availability logic).
  3. Remove the reference hotel (if any) and any hotel whose price per night is above the budget.
  4. Score each hotel with simple, explainable rules: +40 within budget, +30 same city as the reference hotel, +20 same category as the reference hotel, +4 points per star, +5 if 3 or more rooms are left.
  5. Sort by score (highest first), then cheaper price, then id so the order is always the same; return the top 3 as `{ hotel, score, reasons[] }`.
- **Frontend:** `SmartPicks` page validates the form and renders `RecommendationList`, which calls the API through `RecommendationApi.js` and handles loading, error and empty states.
- **Trade-offs:** rule-based scoring was chosen over machine learning because it is simple, predictable and easy to explain; the weights are constants that are easy to tune. It is covered by unit tests (`RecommendationServiceTest`). With more data, scoring could also use guest preferences, reviews and distance.

## 8. Limitations and improvements
- No authentication/payments (not required); booking lookup relies on reference + email
- Placeholder images instead of real photos
- Only one room type per hotel; one room per booking
- Sorting/filtering of amenities in the browser; no pagination
- With more time, I would add frontend tests, database migrations with Flyway, optimistic-lock retries, pagination for hotel search, email confirmations, multi-room bookings and a Docker Compose setup for one-command startup.