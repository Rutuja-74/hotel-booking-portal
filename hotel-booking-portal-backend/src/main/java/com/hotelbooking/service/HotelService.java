package com.hotelbooking.service;

import com.hotelbooking.dto.HotelResponse;
import com.hotelbooking.entity.Hotel;
import com.hotelbooking.entity.Room;
import com.hotelbooking.exception.BadRequestException;
import com.hotelbooking.exception.ResourceNotFoundException;
import com.hotelbooking.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
public class HotelService {

    private static final Set<String> VALID_CATEGORIES =
            Set.of("BUDGET", "STANDARD", "PREMIUM", "LUXURY");

    private final RoomRepository roomRepository;

    // Constructor injection: Spring passes in the repository for us.
    public HotelService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Transactional(readOnly = true)
    public List<HotelResponse> getAllHotels() {
        return roomRepository.findAllWithHotel().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public HotelResponse getHotelById(Long id) {
        List<Room> rooms = roomRepository.findByHotelIdWithHotel(id);
        if (rooms.isEmpty()) {
            throw new ResourceNotFoundException("Hotel not found with id " + id);
        }
        // Each hotel has one room category in this project, so we use the first.
        return toResponse(rooms.get(0));
    }

    @Transactional(readOnly = true)
    public List<HotelResponse> searchHotels(String location, String category,
                                            BigDecimal minPrice, BigDecimal maxPrice,
                                            String sort) {
        String normalizedCategory = normalizeCategory(category);
        validatePriceRange(minPrice, maxPrice);
        Comparator<Room> comparator = buildComparator(sort);
        String searchText = (location == null) ? "" : location.trim().toLowerCase();

        return roomRepository.findAllWithHotel().stream()
                .filter(room -> matchesLocation(room.getHotel(), searchText))
                .filter(room -> normalizedCategory == null
                        || room.getHotel().getCategory().equals(normalizedCategory))
                .filter(room -> minPrice == null
                        || room.getPricePerNight().compareTo(minPrice) >= 0)
                .filter(room -> maxPrice == null
                        || room.getPricePerNight().compareTo(maxPrice) <= 0)
                .sorted(comparator)
                .map(this::toResponse)
                .toList();
    }

    // ---------- helper methods ----------

    private boolean matchesLocation(Hotel hotel, String searchText) {
        if (searchText.isEmpty()) {
            return true;
        }
        return hotel.getCity().toLowerCase().contains(searchText)
                || hotel.getState().toLowerCase().contains(searchText)
                || hotel.getName().toLowerCase().contains(searchText);
    }

    private String normalizeCategory(String category) {
        if (category == null || category.isBlank()) {
            return null;
        }
        String upper = category.trim().toUpperCase();
        if (!VALID_CATEGORIES.contains(upper)) {
            throw new BadRequestException(
                    "Invalid category '" + category + "'. Allowed values: BUDGET, STANDARD, PREMIUM, LUXURY.");
        }
        return upper;
    }

    private void validatePriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice != null && minPrice.signum() < 0) {
            throw new BadRequestException("minPrice cannot be negative.");
        }
        if (maxPrice != null && maxPrice.signum() < 0) {
            throw new BadRequestException("maxPrice cannot be negative.");
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new BadRequestException("minPrice cannot be greater than maxPrice.");
        }
    }

    private Comparator<Room> buildComparator(String sort) {
        if (sort == null || sort.isBlank()) {
            return Comparator.comparing(Room::getId);
        }
        return switch (sort.trim().toLowerCase()) {
            case "price_asc" -> Comparator.comparing(Room::getPricePerNight);
            case "price_desc" -> Comparator.comparing(Room::getPricePerNight).reversed();
            case "rating_desc" -> Comparator
                    .comparing((Room r) -> r.getHotel().getStarRating()).reversed();
            case "name" -> Comparator
                    .comparing((Room r) -> r.getHotel().getName(), String.CASE_INSENSITIVE_ORDER);
            default -> throw new BadRequestException(
                    "Invalid sort '" + sort + "'. Allowed values: price_asc, price_desc, rating_desc, name.");
        };
    }

    private HotelResponse toResponse(Room room) {
        Hotel hotel = room.getHotel();
        List<String> amenityList = Arrays.stream(hotel.getAmenities().split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .toList();

        return new HotelResponse(
                hotel.getId(),
                hotel.getName(),
                hotel.getCity(),
                hotel.getState(),
                hotel.getDescription(),
                amenityList,
                hotel.getCategory(),
                hotel.getStarRating(),
                hotel.getImageUrl(),
                room.getId(),
                room.getRoomType(),
                room.getPricePerNight(),
                room.getTotalRooms(),
                room.getMaxGuests()
        );
    }
}