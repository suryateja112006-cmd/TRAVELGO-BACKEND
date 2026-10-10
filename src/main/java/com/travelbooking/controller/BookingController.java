package com.travelbooking.controller;

import com.travelbooking.entity.Booking;
import com.travelbooking.service.BookingService;
import com.travelbooking.service.BookingNotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final BookingNotificationService bookingNotificationService;

    public BookingController(BookingService bookingService, BookingNotificationService bookingNotificationService) {
        this.bookingService = bookingService;
        this.bookingNotificationService = bookingNotificationService;
    }

    /**
     * POST /api/bookings
     * Create a new booking with automatic total amount calculation
     */
    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody Booking booking, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            Long sessionUserId = (Long) session.getAttribute("userId");
            if (booking.getUser() == null || !sessionUserId.equals(booking.getUser().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You can only create bookings for your own account.");
            }
            Booking savedBooking = bookingService.createBooking(booking);
            BookingNotificationService.DeliveryResult delivery = bookingNotificationService
                    .sendBookingConfirmation(savedBooking);
            savedBooking.setNotificationEmailStatus(delivery.email());
            savedBooking.setNotificationSmsStatus(delivery.sms());
            return new ResponseEntity<>(savedBooking, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Booking creation failed: " + e.getMessage());
        }
    }

    /**
     * GET /api/bookings
     * Retrieve all customer bookings (Admin)
     */
    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    /**
     * GET /api/bookings/user/{userId}
     * Retrieve all bookings for a specific user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getBookingsByUserId(@PathVariable Long userId) {
        try {
            List<Booking> bookings = bookingService.getBookingsByUserId(userId);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving bookings: " + e.getMessage());
        }
    }

    @GetMapping("/staff/{staffId}")
    public ResponseEntity<List<Booking>> getBookingsByStaffId(@PathVariable Long staffId) {
        return ResponseEntity.ok(bookingService.getBookingsByStaffId(staffId));
    }

    @PostMapping("/{id}/progress")
    public ResponseEntity<?> addTripProgressUpdate(@PathVariable Long id,
            @RequestBody TripProgressRequest update, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Long staffId = (Long) session.getAttribute("userId");
        try {
            return ResponseEntity.ok(bookingService.addTripProgressUpdate(id, staffId, update.location(),
                    update.latitude(), update.longitude(), update.reached()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * DELETE /api/bookings/{id}
     * Cancel an existing booking
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id, HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            String role = (String) session.getAttribute("role");
            if ("USER".equals(role)) {
                Booking existing = bookingService.getBookingById(id);
                Long sessionUserId = (Long) session.getAttribute("userId");
                if (!sessionUserId.equals(existing.getUser().getId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only cancel your own bookings.");
                }
            }
            Booking cancelled = bookingService.cancelBooking(id);
            return ResponseEntity.ok(cancelled);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error cancelling booking: " + e.getMessage());
        }
    }

    /**
     * PUT /api/bookings/{id}/payment-status
     * Update the manually tracked payment status.
     */
    @PutMapping("/{id}/payment-status")
    public ResponseEntity<?> updatePaymentStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            Booking updated = bookingService.updatePaymentStatus(id, body.get("paymentStatus"));
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}/staff")
    public ResponseEntity<?> updateStaffInCharge(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        if (!body.containsKey("staffId")) {
            return ResponseEntity.badRequest().body("Provide staffId, or null to clear the current assignment.");
        }
        try {
            Booking updated = bookingService.updateStaffInCharge(id, body.get("staffId"));
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
