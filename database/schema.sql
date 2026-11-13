-- =====================================================
-- Hotel Booking Portal - Database schema (MySQL 8)
-- WARNING: this script DROPS and recreates the tables.
-- =====================================================

USE hotel_booking_db;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS rooms;
DROP TABLE IF EXISTS hotels;
SET FOREIGN_KEY_CHECKS = 1;

-- ---------- hotels ----------
CREATE TABLE hotels (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    name         VARCHAR(150)  NOT NULL,
    city         VARCHAR(100)  NOT NULL,
    state        VARCHAR(100)  NOT NULL,
    description  VARCHAR(1000) NOT NULL,
    amenities    VARCHAR(500)  NOT NULL,
    category     VARCHAR(30)   NOT NULL,
    star_rating  INT           NOT NULL,
    image_url    VARCHAR(500)  NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_hotels_city (city),
    CONSTRAINT chk_hotel_star CHECK (star_rating BETWEEN 1 AND 5),
    CONSTRAINT chk_hotel_category CHECK (category IN ('BUDGET', 'STANDARD', 'PREMIUM', 'LUXURY'))
) ENGINE=InnoDB;

-- ---------- rooms ----------
CREATE TABLE rooms (
    id               BIGINT         NOT NULL AUTO_INCREMENT,
    hotel_id         BIGINT         NOT NULL,
    room_type        VARCHAR(50)    NOT NULL,
    price_per_night  DECIMAL(10,2)  NOT NULL,
    total_rooms      INT            NOT NULL,
    max_guests       INT            NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_room_hotel_type (hotel_id, room_type),
    CONSTRAINT fk_rooms_hotel FOREIGN KEY (hotel_id) REFERENCES hotels (id),
    CONSTRAINT chk_room_price CHECK (price_per_night > 0),
    CONSTRAINT chk_room_total CHECK (total_rooms >= 0),
    CONSTRAINT chk_room_guests CHECK (max_guests > 0)
) ENGINE=InnoDB;

-- ---------- bookings ----------
CREATE TABLE bookings (
    id                 BIGINT         NOT NULL AUTO_INCREMENT,
    booking_reference  VARCHAR(20)    NOT NULL,
    room_id            BIGINT         NOT NULL,
    customer_name      VARCHAR(100)   NOT NULL,
    customer_email     VARCHAR(150)   NOT NULL,
    check_in           DATE           NOT NULL,
    check_out          DATE           NOT NULL,
    nights             INT            NOT NULL,
    total_amount       DECIMAL(10,2)  NOT NULL,
    status             VARCHAR(20)    NOT NULL,
    created_at         DATETIME       NOT NULL,
    cancelled_at       DATETIME       NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_booking_reference (booking_reference),
    INDEX idx_booking_availability (room_id, status, check_in, check_out),
    INDEX idx_booking_email (customer_email),
    CONSTRAINT fk_bookings_room FOREIGN KEY (room_id) REFERENCES rooms (id),
    CONSTRAINT chk_booking_dates CHECK (check_out > check_in),
    CONSTRAINT chk_booking_nights CHECK (nights > 0),
    CONSTRAINT chk_booking_amount CHECK (total_amount > 0),
    CONSTRAINT chk_booking_status CHECK (status IN ('CONFIRMED', 'CANCELLED'))
) ENGINE=InnoDB;