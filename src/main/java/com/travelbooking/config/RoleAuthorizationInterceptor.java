package com.travelbooking.config;

import com.travelbooking.entity.Booking;
import com.travelbooking.repository.BookingRepository;
import com.travelbooking.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RoleAuthorizationInterceptor implements HandlerInterceptor {

    private static final Pattern USER_BOOKINGS_PATH = Pattern.compile("/api/bookings/user/(\\d+)");
    private static final Pattern STAFF_BOOKINGS_PATH = Pattern.compile("/api/bookings/staff/(\\d+)");
    private static final Pattern STAFF_USER_PATH = Pattern.compile("/api/users/staff/(\\d+)");
    private static final Pattern BOOKING_PAYMENT_PATH = Pattern.compile("/api/bookings/(\\d+)/payment-status");
    private static final Pattern BOOKING_STAFF_PATH = Pattern.compile("/api/bookings/(\\d+)/staff");
    private static final Pattern BOOKING_PROGRESS_PATH = Pattern.compile("/api/bookings/(\\d+)/progress");
    private static final Pattern BOOKING_PATH = Pattern.compile("/api/bookings/(\\d+)");
    private static final Pattern PACKAGE_ID_PATH = Pattern.compile("/api/packages/(\\d+)");

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public RoleAuthorizationInterceptor(BookingRepository bookingRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI().substring(request.getContextPath().length());
        String method = request.getMethod();
        if (isPublicRequest(path, method)) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null || session.getAttribute("role") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        Long userId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        if ("STAFF".equals(role)
                && userRepository.findById(userId).filter(user -> user.isActive()).isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        boolean allowed = isAllowed(path, method, userId, role);
        if (!allowed) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
        return allowed;
    }

    private boolean isPublicRequest(String path, String method) {
        return ("POST".equals(method) && ("/api/users/register".equals(path)
                || "/api/users/login".equals(path)
                || "/api/users/admin-login".equals(path)
                || "/api/users/staff-login".equals(path)
                || "/api/users/logout".equals(path)))
                || ("GET".equals(method) && ("/api/packages".equals(path) || PACKAGE_ID_PATH.matcher(path).matches()));
    }

    private boolean isAllowed(String path, String method, Long userId, String role) {
        if ("/api/users".equals(path)) {
            return "GET".equals(method) && "ADMIN".equals(role);
        }
        if ("/api/users/me".equals(path)) {
            return "GET".equals(method);
        }
        if ("/api/bookings".equals(path)) {
            return ("GET".equals(method) && "ADMIN".equals(role))
                    || ("POST".equals(method) && "USER".equals(role));
        }

        Matcher userBookings = USER_BOOKINGS_PATH.matcher(path);
        if (userBookings.matches() && "GET".equals(method)) {
            return "ADMIN".equals(role) || ("USER".equals(role) && userId.equals(Long.valueOf(userBookings.group(1))));
        }

        Matcher staffBookings = STAFF_BOOKINGS_PATH.matcher(path);
        if (staffBookings.matches() && "GET".equals(method)) {
            return "ADMIN".equals(role)
                    || ("STAFF".equals(role) && userId.equals(Long.valueOf(staffBookings.group(1))));
        }

        Matcher progressPath = BOOKING_PROGRESS_PATH.matcher(path);
        if (progressPath.matches() && "POST".equals(method)) {
            return "STAFF".equals(role);
        }

        Matcher paymentPath = BOOKING_PAYMENT_PATH.matcher(path);
        if (paymentPath.matches() && "PUT".equals(method)) {
            return "ADMIN".equals(role);
        }

        Matcher staffPath = BOOKING_STAFF_PATH.matcher(path);
        if (staffPath.matches() && "PUT".equals(method)) {
            return "ADMIN".equals(role);
        }

        if ("/api/users/staff".equals(path)) {
            return "ADMIN".equals(role) && ("GET".equals(method) || "POST".equals(method));
        }
        if ("/api/packages/admin/all".equals(path)) {
            return "GET".equals(method) && "ADMIN".equals(role);
        }

        Matcher staffUser = STAFF_USER_PATH.matcher(path);
        if (staffUser.matches() && ("PUT".equals(method) || "DELETE".equals(method))) {
            return "ADMIN".equals(role);
        }

        Matcher bookingPath = BOOKING_PATH.matcher(path);
        if (bookingPath.matches() && "DELETE".equals(method)) {
            if ("ADMIN".equals(role)) {
                return true;
            }
            if (!"USER".equals(role)) {
                return false;
            }
            Optional<Booking> booking = bookingRepository.findById(Long.valueOf(bookingPath.group(1)));
            return booking.isEmpty() || userId.equals(booking.get().getUser().getId());
        }

        if (path.startsWith("/api/packages")) {
            return "ADMIN".equals(role)
                    && ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method));
        }
        return false;
    }
}
