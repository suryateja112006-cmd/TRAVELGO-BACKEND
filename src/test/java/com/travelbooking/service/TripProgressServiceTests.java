package com.travelbooking.service;

import com.travelbooking.entity.Booking;
import com.travelbooking.entity.TravelPackage;
import com.travelbooking.entity.TripProgressUpdate;
import com.travelbooking.entity.User;
import com.travelbooking.repository.BookingRepository;
import com.travelbooking.repository.TravelPackageRepository;
import com.travelbooking.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TripProgressServiceTests {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TravelPackageRepository packageRepository;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void staffReportIsSavedAgainstTheirNextUnreachedCheckpoint() {
        User staff = new User();
        staff.setId(7L);
        staff.setRole("STAFF");
        TravelPackage travelPackage = new TravelPackage();
        travelPackage.setPlaces("Central Station\nHill Viewpoint");
        Booking booking = new Booking();
        booking.setId(12L);
        booking.setStatus("CONFIRMED");
        booking.setTravelPackage(travelPackage);
        booking.setStaffInCharge(staff);
        TripProgressUpdate firstStop = new TripProgressUpdate();
        firstStop.setCheckpoint("Central Station");
        firstStop.setReached(true);
        booking.setProgressUpdates(new java.util.ArrayList<>(List.of(firstStop)));
        when(bookingRepository.findById(12L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);

        TripProgressUpdate update = bookingService.addTripProgressUpdate(12L, 7L, "North gate", 17.4, 78.5, false);

        assertEquals("Hill Viewpoint", update.getCheckpoint());
        assertEquals("North gate", update.getLocation());
        assertEquals(17.4, update.getLatitude());
        assertEquals(78.5, update.getLongitude());
        assertEquals(false, update.isReached());
        assertEquals(2, booking.getProgressUpdates().size());
        verify(bookingRepository).save(booking);
    }

    @Test
    void staffCannotReportProgressForAnotherStaffMembersBooking() {
        User assignedStaff = new User();
        assignedStaff.setId(8L);
        Booking booking = new Booking();
        booking.setStaffInCharge(assignedStaff);
        when(bookingRepository.findById(12L)).thenReturn(Optional.of(booking));

        assertThrows(SecurityException.class,
                () -> bookingService.addTripProgressUpdate(12L, 7L, "North gate", null, null, false));
    }
}
