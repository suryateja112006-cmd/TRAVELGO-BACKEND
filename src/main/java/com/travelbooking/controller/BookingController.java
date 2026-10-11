
package com.travelbooking.controller;

import com.travelbooking.entity.Booking;
import com.travelbooking.service.BookingService;
import com.travelbooking.service.BookingNotificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final BookingNotificationService bookingNotificationService;

    public BookingController(
            BookingService bookingService,
            BookingNotificationService bookingNotificationService) {
        this.bookingService = bookingService;
        this.bookingNotificationService = bookingNotificationService;
    }

    private HttpSession getSession(HttpServletRequest request) {
        return request.getSession(false);
    }

    private boolean hasRole(HttpSession session, String role) {
        return session != null && role.equals(session.getAttribute("role"));
    }

    private Long getSessionUserId(HttpSession session) {
        if (session == null)
            return null;
        Object id = session.getAttribute("userId");
        return id instanceof Long ? (Long) id : null;
    }

    // POST /api/bookings - Create booking
    @PostMapping
    public ResponseEntity<?> createBooking(
            @RequestBody Booking booking,
            HttpServletRequest request) {

        HttpSession session = getSession(request);
        Long sessionUserId = getSessionUserId(session);

        if (sessionUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Please log in before booking a travel package.");
        }

        if (!hasRole(session, "USER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only customer accounts can create bookings.");
        }

        if (booking.getUser() == null
                || booking.getUser().getId() == null
                || !sessionUserId.equals(booking.getUser().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You can only create bookings for your own account.");
        }

        try {
            Booking savedBooking = bookingService.createBooking(booking);

            BookingNotificationService.DeliveryResult delivery = bookingNotificationService
                    .sendBookingConfirmation(savedBooking);

            savedBooking.setNotificationEmailStatus(delivery.email());
            savedBooking.setNotificationSmsStatus(delivery.sms());

            return ResponseEntity.status(HttpStatus.CREATED).body(savedBooking);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Booking creation failed. Please try again.");
        }
    }

    // GET /api/bookings - Admin only
    @GetMapping
    public ResponseEntity<?> getAllBookings(HttpServletRequest request) {
        if (!hasRole(getSession(request), "ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Administrator access is required.");
        }

        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    // GET /api/bookings/user/{userId}
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getBookingsByUserId(
            @PathVariable Long userId,
            HttpServletRequest request) {

        HttpSession session = getSession(request);
        Long currentUserId = getSessionUserId(session);

        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Please log in to view bookings.");
        }

        if (!hasRole(session, "ADMIN")
                && !currentUserId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You can only view your own bookings.");
        }

        try {
            return ResponseEntity.ok(
                    bookingService.getBookingsByUserId(userId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving bookings.");
        }
    }

    // GET /api/bookings/staff/{staffId}
    @GetMapping("/staff/{staffId}")
    public ResponseEntity<?> getBookingsByStaffId(
            @PathVariable Long staffId,
            HttpServletRequest request) {

        HttpSession session = getSession(request);
        Long currentUserId = getSessionUserId(session);

        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Please log in.");
        }

        if (!hasRole(session, "ADMIN")
                && !(hasRole(session, "STAFF")
                        && currentUserId.equals(staffId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You are not authorized to view these trips.");
        }

        return ResponseEntity.ok(
                bookingService.getBookingsByStaffId(staffId));
    }

    // POST /api/bookings/{id}/progress
    @PostMapping("/{id}/progress")
    public ResponseEntity<?> addTripProgressUpdate(
            @PathVariable Long id,
            @RequestBody TripProgressRequest update,
            HttpServletRequest request) {

        HttpSession session = getSession(request);
        Long staffId = getSessionUserId(session);

        if (staffId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Please log in.");
        }

        if (!hasRole(session, "STAFF")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only staff can submit trip progress.");
        }

        try {
            return ResponseEntity.ok(
                    bookingService.addTripProgressUpdate(
                            id,
                            staffId,
                            update.location(),
                            update.latitude(),
                            update.longitude(),
                            update.reached()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // DELETE /api/bookings/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelBooking(
            @PathVariable Long id,
            HttpServletRequest request) {

        HttpSession session = getSession(request);
        Long currentUserId = getSessionUserId(session);

        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Please log in to cancel a booking.");
        }

        if (!hasRole(session, "USER")
                && !hasRole(session, "ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You are not authorized to cancel bookings.");
        }

        try {
            Booking existing = bookingService.getBookingById(id);

            if (hasRole(session, "USER")
                    && !currentUserId.equals(existing.getUser().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You can only cancel your own bookings.");
            }

            return ResponseEntity.ok(bookingService.cancelBooking(id));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error cancelling booking.");
        }
    }

    // PUT /api/bookings/{id}/payment-status - Admin only
    @PutMapping("/{id}/payment-status")
    public ResponseEntity<?> updatePaymentStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {

        if (!hasRole(getSession(request), "ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Administrator access is required.");
        }

        try {
            Booking updated = bookingService.updatePaymentStatus(
                    id, body.get("paymentStatus"));
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PUT /api/bookings/{id}/staff - Admin only
    @PutMapping("/{id}/staff")
    public ResponseEntity<?> updateStaffInCharge(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body,
            HttpServletRequest request) {

        if (!hasRole(getSession(request), "ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Administrator access is required.");
        }

        if (!body.containsKey("staffId")) {
            return ResponseEntity.badRequest()
                    .body("Provide staffId to assign a staff member.");
        }

        try {
            Booking updated = bookingService.updateStaffInCharge(
                    id, body.get("staffId"));
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}