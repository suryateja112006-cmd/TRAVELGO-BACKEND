package com.travelbooking.service;

import com.travelbooking.entity.Booking;
import com.travelbooking.entity.TravelPackage;
import com.travelbooking.entity.User;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BookingNotificationServiceTests {

    @Test
    void missingProviderConfigurationDoesNotPretendNotificationsWereSent() {
        BookingNotificationService service = new BookingNotificationService(Map.of(), HttpClient.newHttpClient());

        BookingNotificationService.DeliveryResult result = service.sendBookingConfirmation(booking());

        assertEquals("not_configured", result.email());
        assertEquals("not_configured", result.sms());
    }

    @Test
    void smsWithConfiguredProviderAndMissingCustomerPhoneReportsNoRecipient() {
        Map<String, String> environment = Map.of(
                "TRAVELGO_TWILIO_ACCOUNT_SID", "test-account",
                "TRAVELGO_TWILIO_AUTH_TOKEN", "test-token",
                "TRAVELGO_TWILIO_FROM_NUMBER", "+15551234567");
        Booking booking = booking();
        booking.getUser().setPhone("");
        BookingNotificationService service = new BookingNotificationService(environment, HttpClient.newHttpClient());

        BookingNotificationService.DeliveryResult result = service.sendBookingConfirmation(booking);

        assertEquals("not_configured", result.email());
        assertEquals("no_recipient", result.sms());
    }

    private Booking booking() {
        User user = new User();
        user.setName("Travel Customer");
        user.setEmail("customer@example.com");
        user.setPhone("9876543210");
        TravelPackage travelPackage = new TravelPackage();
        travelPackage.setName("Goa Escape");
        Booking booking = new Booking();
        booking.setId(42L);
        booking.setUser(user);
        booking.setTravelPackage(travelPackage);
        booking.setTravelDate("2026-12-01");
        booking.setNumberOfPersons(2);
        booking.setTotalAmount(19998.0);
        return booking;
    }
}
