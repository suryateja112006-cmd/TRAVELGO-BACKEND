package com.travelbooking.service;

import com.travelbooking.entity.User;
import com.travelbooking.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void createStaffForcesStaffRoleAndCreatesNewUser() {
        User staff = new User(42L, "  Asha Kumar  ", " asha@example.com ", " 9999999999 ", "staff123", "ADMIN");
        when(userRepository.existsByEmail("asha@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = userService.createStaff(staff);

        assertNull(created.getId());
        assertEquals("Asha Kumar", created.getName());
        assertEquals("asha@example.com", created.getEmail());
        assertEquals("9999999999", created.getPhone());
        assertEquals("STAFF", created.getRole());
        verify(userRepository).save(staff);
    }

    @Test
    void createStaffRejectsShortPassword() {
        User staff = new User(null, "Asha Kumar", "asha@example.com", "9999999999", "short", "STAFF");

        assertThrows(IllegalArgumentException.class, () -> userService.createStaff(staff));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deletedStaffCannotSignIn() {
        User staff = new User(42L, "Asha Kumar", "asha@example.com", "9999999999", "staff123", "STAFF");
        staff.setActive(false);
        when(userRepository.findByEmail("asha@example.com")).thenReturn(java.util.Optional.of(staff));

        assertThrows(IllegalArgumentException.class, () -> userService.login("asha@example.com", "staff123"));
    }

    @Test
    void updateStaffChangesProfileAndKeepsPasswordWhenBlank() {
        User staff = new User(42L, "Old Name", "old@example.com", "1111111111", "original123", "STAFF");
        User updates = new User(null, " New Name ", " new@example.com ", " 9999999999 ", " ", null);
        when(userRepository.findById(42L)).thenReturn(Optional.of(staff));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(staff)).thenReturn(staff);

        User updated = userService.updateStaff(42L, updates);

        assertEquals("New Name", updated.getName());
        assertEquals("new@example.com", updated.getEmail());
        assertEquals("9999999999", updated.getPhone());
        assertEquals("original123", updated.getPassword());
        assertEquals("STAFF", updated.getRole());
        verify(userRepository).save(staff);
    }

    @Test
    void updateStaffCanResetPassword() {
        User staff = new User(42L, "Staff Name", "staff@example.com", "9999999999", "old-pass", "STAFF");
        User updates = new User(null, "Staff Name", "staff@example.com", "9999999999", "new-pass-123", null);
        when(userRepository.findById(42L)).thenReturn(Optional.of(staff));
        when(userRepository.save(staff)).thenReturn(staff);

        User updated = userService.updateStaff(42L, updates);

        assertEquals("new-pass-123", updated.getPassword());
        verify(userRepository).save(staff);
    }

    @Test
    void updateStaffRejectsDuplicateEmail() {
        User staff = new User(42L, "Staff Name", "old@example.com", "9999999999", "staff-pass", "STAFF");
        User updates = new User(null, "Staff Name", "taken@example.com", "9999999999", "", null);
        when(userRepository.findById(42L)).thenReturn(Optional.of(staff));
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.updateStaff(42L, updates));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteStaffKeepsAssignedBookingReferences() {
        User staff = new User(42L, "Staff Name", "staff@example.com", "9999999999", "staff-pass", "STAFF");
        when(userRepository.findById(42L)).thenReturn(java.util.Optional.of(staff));
        when(userRepository.save(staff)).thenReturn(staff);

        userService.deleteStaff(42L);

        org.junit.jupiter.api.Assertions.assertFalse(staff.isActive());
        verify(userRepository).save(staff);
    }

    @Test
    void deleteStaffKeepsAccountForHistoricalReferences() {
        User staff = new User(42L, "Staff Name", "staff@example.com", "9999999999", "staff-pass", "STAFF");
        when(userRepository.findById(42L)).thenReturn(java.util.Optional.of(staff));
        when(userRepository.save(staff)).thenReturn(staff);

        userService.deleteStaff(42L);

        assertEquals("Staff Name", staff.getName());
        org.junit.jupiter.api.Assertions.assertFalse(staff.isActive());
        verify(userRepository).save(staff);
        verify(userRepository, never()).delete(staff);
    }
}
