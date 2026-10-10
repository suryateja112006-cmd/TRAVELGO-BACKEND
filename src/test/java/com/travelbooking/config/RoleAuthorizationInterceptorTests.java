package com.travelbooking.config;

import com.travelbooking.repository.BookingRepository;
import com.travelbooking.entity.User;
import com.travelbooking.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.Optional;

class RoleAuthorizationInterceptorTests {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final RoleAuthorizationInterceptor interceptor = new RoleAuthorizationInterceptor(
            mock(BookingRepository.class), userRepository);

    @Test
    void staffCanReadTheirOwnAssignedBookings() throws Exception {
        MockHttpServletRequest request = staffRequest(7L, 7L);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, new Object()));
    }

    @Test
    void signedInStaffCanReadTheirOwnProfile() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/users/me");
        request.setMethod("GET");
        request.getSession().setAttribute("userId", 7L);
        request.getSession().setAttribute("role", "STAFF");
        User staff = new User();
        staff.setActive(true);
        when(userRepository.findById(7L)).thenReturn(Optional.of(staff));
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, new Object()));
    }

    @Test
    void staffCannotReadAnotherStaffMembersBookings() throws Exception {
        MockHttpServletRequest request = staffRequest(7L, 8L);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertTrue(response.getStatus() == MockHttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    void staffLoginEndpointDoesNotRequireAnExistingSession() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/users/staff-login");
        request.setMethod("POST");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, new Object()));
    }

    @Test
    void onlyAdminsCanReadDeletedPackagesForManagement() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/packages/admin/all");
        request.setMethod("GET");
        request.getSession().setAttribute("userId", 1L);
        request.getSession().setAttribute("role", "ADMIN");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, new Object()));
    }

    @Test
    void staffCanSubmitProgressForAnAssignedBooking() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/bookings/12/progress");
        request.setMethod("POST");
        request.getSession().setAttribute("userId", 7L);
        request.getSession().setAttribute("role", "STAFF");
        User staff = new User();
        staff.setActive(true);
        when(userRepository.findById(7L)).thenReturn(Optional.of(staff));
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, new Object()));
    }

    @Test
    void adminCannotSubmitStaffProgressUpdates() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/bookings/12/progress");
        request.setMethod("POST");
        request.getSession().setAttribute("userId", 1L);
        request.getSession().setAttribute("role", "ADMIN");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertTrue(response.getStatus() == MockHttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    void adminCanUpdateStaffProfiles() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/users/staff/7");
        request.setMethod("PUT");
        request.getSession().setAttribute("userId", 1L);
        request.getSession().setAttribute("role", "ADMIN");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, new Object()));
    }

    @Test
    void staffCannotUpdateTheirOwnProfileThroughAdminEndpoint() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/users/staff/7");
        request.setMethod("PUT");
        request.getSession().setAttribute("userId", 7L);
        request.getSession().setAttribute("role", "STAFF");
        User staff = new User();
        staff.setActive(true);
        when(userRepository.findById(7L)).thenReturn(Optional.of(staff));
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertTrue(response.getStatus() == MockHttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    void adminCanDeleteStaffAccounts() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/users/staff/7");
        request.setMethod("DELETE");
        request.getSession().setAttribute("userId", 1L);
        request.getSession().setAttribute("role", "ADMIN");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, new Object()));
    }

    @Test
    void deletedStaffCannotUseAnExistingSession() throws Exception {
        MockHttpServletRequest request = staffRequest(7L, 7L);
        MockHttpServletResponse response = new MockHttpServletResponse();
        User deletedStaff = new User();
        deletedStaff.setActive(false);
        when(userRepository.findById(7L)).thenReturn(Optional.of(deletedStaff));

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertTrue(response.getStatus() == MockHttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void staffCannotDeleteStaffAccounts() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/users/staff/7");
        request.setMethod("DELETE");
        request.getSession().setAttribute("userId", 7L);
        request.getSession().setAttribute("role", "STAFF");
        User staff = new User();
        staff.setActive(true);
        when(userRepository.findById(7L)).thenReturn(Optional.of(staff));
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertTrue(response.getStatus() == MockHttpServletResponse.SC_FORBIDDEN);
    }

    private MockHttpServletRequest staffRequest(Long requestedStaffId, Long sessionUserId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/bookings/staff/" + requestedStaffId);
        request.setMethod("GET");
        request.getSession().setAttribute("userId", sessionUserId);
        request.getSession().setAttribute("role", "STAFF");
        User staff = new User();
        staff.setActive(true);
        when(userRepository.findById(sessionUserId)).thenReturn(Optional.of(staff));
        return request;
    }
}
