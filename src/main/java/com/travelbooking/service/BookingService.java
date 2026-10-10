package com.travelbooking.service;

import com.travelbooking.entity.Booking;
import com.travelbooking.entity.TravelPackage;
import com.travelbooking.entity.TravelerDetails;
import com.travelbooking.entity.TripProgressUpdate;
import com.travelbooking.entity.User;
import com.travelbooking.repository.BookingRepository;
import com.travelbooking.repository.TravelPackageRepository;
import com.travelbooking.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final TravelPackageRepository packageRepository;

    public BookingService(BookingRepository bookingRepository,
            UserRepository userRepository,
            TravelPackageRepository packageRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.packageRepository = packageRepository;
    }

    public Booking createBooking(Booking booking) {
        if (booking.getUser() == null || booking.getUser().getId() == null) {
            throw new IllegalArgumentException("User ID is required for booking.");
        }
        if (booking.getTravelPackage() == null || booking.getTravelPackage().getId() == null) {
            throw new IllegalArgumentException("Package ID is required for booking.");
        }
        if (booking.getNumberOfPersons() == null || booking.getNumberOfPersons() < 1) {
            throw new IllegalArgumentException("Number of persons must be at least 1.");
        }
        if (booking.getNumberOfPersons() > 50) {
            throw new IllegalArgumentException("A booking can include at most 50 travelers.");
        }
        if (booking.getTravelers() == null || booking.getTravelers().size() != booking.getNumberOfPersons()) {
            throw new IllegalArgumentException("Traveler details must be provided for every person.");
        }
        for (int i = 0; i < booking.getTravelers().size(); i++) {
            TravelerDetails traveler = booking.getTravelers().get(i);
            if (traveler.getName() == null || traveler.getName().isBlank()
                    || traveler.getEmail() == null || traveler.getEmail().isBlank()
                    || traveler.getPhone() == null || traveler.getPhone().isBlank()) {
                throw new IllegalArgumentException("Name, email, and phone are required for traveler " + (i + 1) + ".");
            }
        }

        User user = userRepository.findById(booking.getUser().getId())
                .orElseThrow(
                        () -> new IllegalArgumentException("User not found with id: " + booking.getUser().getId()));

        TravelPackage travelPackage = packageRepository.findById(booking.getTravelPackage().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Travel package not found with id: " + booking.getTravelPackage().getId()));
        if (!travelPackage.isActive()) {
            throw new IllegalArgumentException("This travel package is no longer available for booking.");
        }

        // Calculate Total Amount = Package Price * Number of Persons
        double discountPercent = travelPackage.getDiscountPercent() == null ? 0.0 : travelPackage.getDiscountPercent();
        double pricePerPerson = travelPackage.getPrice() * (1 - discountPercent / 100.0);
        double totalAmount = pricePerPerson * booking.getNumberOfPersons();

        booking.setUser(user);
        booking.setTravelPackage(travelPackage);
        booking.setTotalAmount(totalAmount);
        booking.setStatus("CONFIRMED");
        booking.setPaymentStatus("UNPAID");

        return bookingRepository.save(booking);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public List<Booking> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserIdOrderByIdDesc(userId);
    }

    public List<Booking> getBookingsByStaffId(Long staffId) {
        return bookingRepository.findByStaffInCharge_IdOrderByTravelDateAsc(staffId);
    }

    @Transactional
    public TripProgressUpdate addTripProgressUpdate(Long bookingId, Long staffId, String location,
            Double latitude, Double longitude, boolean reached) {
        Booking booking = getBookingById(bookingId);
        if (booking.getStaffInCharge() == null || !staffId.equals(booking.getStaffInCharge().getId())) {
            throw new SecurityException("You are not assigned to this trip.");
        }
        if ("CANCELLED".equals(booking.getStatus())) {
            throw new IllegalArgumentException("Progress cannot be reported for a cancelled trip.");
        }
        if ((latitude == null) != (longitude == null)
                || (latitude != null && (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180))) {
            throw new IllegalArgumentException("Provide valid latitude and longitude coordinates together.");
        }
        String normalizedLocation = location == null ? "" : location.trim();
        if (normalizedLocation.isEmpty() && latitude == null) {
            throw new IllegalArgumentException("Enter a location or share your current GPS location.");
        }

        String places = booking.getTravelPackage().getPlaces();
        List<String> itinerary = places == null ? List.of()
                : Arrays.stream(places.split("\\R"))
                        .map(String::trim)
                        .filter(place -> !place.isEmpty())
                        .toList();
        Set<String> reachedPoints = new HashSet<>();
        if (booking.getProgressUpdates() != null) {
            booking.getProgressUpdates().stream()
                    .filter(TripProgressUpdate::isReached)
                    .map(TripProgressUpdate::getCheckpoint)
                    .forEach(reachedPoints::add);
        }
        String checkpoint = itinerary.stream()
                .filter(place -> !reachedPoints.contains(place))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("All itinerary points are already marked reached."));

        TripProgressUpdate update = new TripProgressUpdate();
        update.setBooking(booking);
        update.setStaff(booking.getStaffInCharge());
        update.setCheckpoint(checkpoint);
        update.setLocation(normalizedLocation.isEmpty() ? null : normalizedLocation);
        update.setLatitude(latitude);
        update.setLongitude(longitude);
        update.setReached(reached);
        update.setReportedAt(LocalDateTime.now(ZoneOffset.UTC));
        booking.getProgressUpdates().add(update);
        bookingRepository.save(booking);
        return update;
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with id: " + id));
    }

    public Booking cancelBooking(Long id) {
        Booking booking = getBookingById(id);
        booking.setStatus("CANCELLED");
        return bookingRepository.save(booking);
    }

    public Booking updatePaymentStatus(Long id, String paymentStatus) {
        if (!"PAID".equals(paymentStatus) && !"UNPAID".equals(paymentStatus)) {
            throw new IllegalArgumentException("Payment status must be PAID or UNPAID.");
        }
        Booking booking = getBookingById(id);
        booking.setPaymentStatus(paymentStatus);
        return bookingRepository.save(booking);
    }

    public Booking updateStaffInCharge(Long bookingId, Long staffId) {
        Booking booking = getBookingById(bookingId);
        if ("CANCELLED".equals(booking.getStatus())) {
            throw new IllegalArgumentException("A cancelled trip cannot be assigned to staff.");
        }

        if (staffId == null) {
            booking.setStaffInCharge(null);
        } else {
            User staff = userRepository.findById(staffId)
                    .orElseThrow(() -> new IllegalArgumentException("Staff member not found with id: " + staffId));
            if (!"STAFF".equals(staff.getRole())) {
                throw new IllegalArgumentException("The selected user is not a staff member.");
            }
            if (!staff.isActive()) {
                throw new IllegalArgumentException("Deleted staff members cannot be assigned to trips.");
            }
            booking.setStaffInCharge(staff);
        }
        return bookingRepository.save(booking);
    }

    public void deleteBooking(Long id) {
        if (!bookingRepository.existsById(id)) {
            throw new IllegalArgumentException("Booking not found with id: " + id);
        }
        bookingRepository.deleteById(id);
    }
}
