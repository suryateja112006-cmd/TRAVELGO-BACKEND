package com.travelbooking.service;

import com.travelbooking.entity.Booking;
import com.travelbooking.entity.TravelPackage;
import com.travelbooking.entity.TravelerDetails;
import com.travelbooking.entity.User;
import com.travelbooking.repository.BookingRepository;
import com.travelbooking.repository.TravelPackageRepository;
import com.travelbooking.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTests {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TravelPackageRepository packageRepository;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void updateStaffInChargeAssignsStaffMember() {
        Booking booking = new Booking();
        booking.setStatus("CONFIRMED");
        User staff = new User();
        staff.setId(7L);
        staff.setRole("STAFF");
        when(bookingRepository.findById(12L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(7L)).thenReturn(Optional.of(staff));
        when(bookingRepository.save(booking)).thenReturn(booking);

        Booking updated = bookingService.updateStaffInCharge(12L, 7L);

        assertSame(staff, updated.getStaffInCharge());
        verify(bookingRepository).save(booking);
    }

    @Test
    void updateStaffInChargeRejectsNonStaffUsers() {
        Booking booking = new Booking();
        booking.setStatus("CONFIRMED");
        User customer = new User();
        customer.setId(7L);
        customer.setRole("USER");
        when(bookingRepository.findById(12L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(7L)).thenReturn(Optional.of(customer));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.updateStaffInCharge(12L, 7L)
        );

        assertEquals("The selected user is not a staff member.", exception.getMessage());
        verify(bookingRepository, never()).save(booking);
    }

    @Test
    void updateStaffInChargeRejectsDeletedStaff() {
        Booking booking = new Booking();
        booking.setStatus("CONFIRMED");
        User staff = new User();
        staff.setId(7L);
        staff.setRole("STAFF");
        staff.setActive(false);
        when(bookingRepository.findById(12L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(7L)).thenReturn(Optional.of(staff));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.updateStaffInCharge(12L, 7L)
        );

        assertEquals("Deleted staff members cannot be assigned to trips.", exception.getMessage());
        verify(bookingRepository, never()).save(booking);
    }

    @Test
    void updateStaffInChargeRejectsCancelledTrips() {
        Booking booking = new Booking();
        booking.setStatus("CANCELLED");
        when(bookingRepository.findById(12L)).thenReturn(Optional.of(booking));

        assertThrows(IllegalArgumentException.class, () -> bookingService.updateStaffInCharge(12L, 7L));

        verify(userRepository, never()).findById(7L);
        verify(bookingRepository, never()).save(booking);
    }

    @Test
    void createBookingRejectsDeletedPackage() {
        Booking booking = new Booking();
        User userReference = new User();
        userReference.setId(5L);
        TravelPackage packageReference = new TravelPackage();
        packageReference.setId(8L);
        booking.setUser(userReference);
        booking.setTravelPackage(packageReference);
        booking.setNumberOfPersons(1);
        TravelerDetails traveler = new TravelerDetails();
        traveler.setName("Asha Kumar");
        traveler.setEmail("asha@example.com");
        traveler.setPhone("9999999999");
        booking.setTravelers(java.util.List.of(traveler));

        User user = new User();
        user.setId(5L);
        TravelPackage deletedPackage = new TravelPackage();
        deletedPackage.setActive(false);
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(packageRepository.findById(8L)).thenReturn(Optional.of(deletedPackage));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(booking)
        );

        assertEquals("This travel package is no longer available for booking.", exception.getMessage());
        verify(bookingRepository, never()).save(booking);
    }
}
