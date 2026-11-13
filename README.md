# Hotel Booking Portal

A full-stack hotel booking application: search hotels, view details and availability for chosen dates, book a room, retrieve a booking with reference + email, and cancel it.

**Stack:** React (Vite, JavaScript) · Spring Boot 4.1.2 (Java 21) · MySQL 8 · Maven

## Features
- Search hotels by location, dates, category and price range; sort results
- Hotel details with room type, amenities, price per night and available rooms for the selected dates
- Book one room with name and email; total amount is calculated by the backend
- Booking confirmation with a unique reference (e.g. `HB-20261113-K7M2QX`)
- Retrieve a booking by reference + email, and cancel it (cancelling releases availability)
- Safe concurrent booking of the last room (pessimistic locking)
- **Surprise feature: Smart Stay Picks** (`/smart-picks`): ranked hotel suggestions for a city, dates and optional budget, with reasons (see DESIGN.md)

## Prerequisites
- Java 21
- Node.js 20+ and npm (developed on Node 24, npm 11)
- MySQL 8.0 (and MySQL Workbench or the `mysql` CLI)
- Git

## 1. Database setup
1. Open MySQL Workbench (or CLI) as an admin user and create the database and an application user (choose your own password):
   ```sql
   CREATE DATABASE hotel_booking_db;
   CREATE USER 'hotel_app'@'localhost' IDENTIFIED BY '<your-password>';
   GRANT ALL PRIVILEGES ON hotel_booking_db.* TO 'hotel_app'@'localhost';
   FLUSH PRIVILEGES;
   ```
2. Run `database/schema.sql`, then `database/data.sql` (7 sample hotels) against `hotel_booking_db`.
3. (Optional, for integration tests) create a separate `hotel_booking_test_db` and grant the same user access to it.

## 2. Environment variables (no secrets are stored in the repo)
| Variable | Meaning |
|---|---|
| `DB_USERNAME` | MySQL user, e.g. `hotel_app` |
| `DB_PASSWORD` | MySQL password |
| `DB_URL` | optional, defaults to the local `hotel_booking_db` |

PowerShell (current terminal only):
```powershell
$env:DB_USERNAME="hotel_app"
$env:DB_PASSWORD="<your-password>"
```

## 3. Run the backend
```powershell
cd hotel-booking-portal-backend
.\mvnw.cmd spring-boot:run
```
API runs at http://localhost:8080 (try http://localhost:8080/api/hotels).

## 4. Run the frontend
```powershell
cd hotel-booking-portal-frontend
npm install
npm run dev
```
Open http://localhost:5173. Vite proxies `/api` to `http://localhost:8080`, so no CORS setup is needed.

## 5. Run the tests
```powershell
cd hotel-booking-portal-backend
.\mvnw.cmd test
```
Unit tests cover the night/price calculation, date validation, availability (overlapping and back-to-back stays, never below zero) and booking/cancel rules.
Integration tests (including two users booking the last room at the same time) run against the separate `hotel_booking_test_db` and are **skipped automatically if `DB_PASSWORD` is not set**. Set the environment variables from step 2 first so they run.

## Main flow to try
Home → search "Goa" with dates → open a hotel → enter name + email → Confirm booking → note the reference → **Manage Booking** → enter reference + email → Cancel → search again to see availability restored.

## Screenshots
See `docs/screenshots/` (home, search results, hotel details, confirmation, manage booking, recommendation).

## Assumptions
- Each hotel has one room type with a fixed number of rooms (`totalRooms`).
- A booking is for one room. Check-out day is not counted as a night (nights = check-out − check-in).
- Check-in cannot be in the past; check-out must be after check-in; a stay can be at most 30 nights.
- A booking occupies the nights from check-in up to (not including) check-out. Availability = total rooms − the **highest number of rooms in use on any single night** of the requested stay. Only `CONFIRMED` bookings count, so a guest checking out on the 10th does not block another guest checking in on the 10th.
- A booking can be cancelled only until its check-in date.
- Wrong reference and wrong email give the same "Booking not found" message so nothing is revealed.
- No login: a booking is retrieved/cancelled using reference + email together.
- Prices are in INR; no payment integration.

## Approximate time spent
About 10 hours, including learning Spring Boot and React along the way.

## Unfinished / known limitations
- No real hotel photos (a styled placeholder is shown when an image is missing)
- The hotels page sorts results in the browser (the backend also supports `sort`)
- One room type per hotel; no authentication or payments
- Frontend has no automated tests (backend does)

## AI assistance disclosure
I used an AI assistant throughout this project: for planning, step-by-step guidance, and generating much of the backend and frontend code, tests and these documents. I worked through it phase by phase, ran and tested everything locally, and I can explain the code and design decisions.
