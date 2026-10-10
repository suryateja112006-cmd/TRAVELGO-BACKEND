package com.travelbooking.controller;

import com.travelbooking.entity.User;
import com.travelbooking.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * POST /api/users/register
     * Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            User createdUser = userService.registerUser(user);
            return new ResponseEntity<>(userResponse(createdUser), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Registration error: " + e.getMessage());
        }
    }

    /**
     * POST /api/users/login
     * Authenticate user with email and password
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials, HttpServletRequest request) {
        return authenticate(credentials, request, "USER");
    }

    @PostMapping("/admin-login")
    public ResponseEntity<?> adminLogin(@RequestBody Map<String, String> credentials, HttpServletRequest request) {
        return authenticate(credentials, request, "ADMIN");
    }

    @PostMapping("/staff-login")
    public ResponseEntity<?> staffLogin(@RequestBody Map<String, String> credentials, HttpServletRequest request) {
        return authenticate(credentials, request, "STAFF");
    }

    private ResponseEntity<?> authenticate(Map<String, String> credentials, HttpServletRequest request,
            String requiredRole) {
        HttpSession existingSession = request.getSession(false);
        if (existingSession != null) {
            existingSession.invalidate();
        }

        String email = credentials.get("email");
        String password = credentials.get("password");

        if (email == null || password == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email and password are required.");
        }

        try {
            User user = userService.login(email, password);
            if (!requiredRole.equals(user.getRole())) {
                String message = "ADMIN".equals(requiredRole)
                        ? "This account is not an administrator. Use customer login."
                        : "Administrators must sign in through the admin portal.";
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(message);
            }

            HttpSession session = request.getSession(true);
            session.setAttribute("userId", user.getId());
            session.setAttribute("role", user.getRole());
            session.setMaxInactiveInterval(30 * 60);
            return ResponseEntity.ok(userResponse(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Login error: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/users
     * Retrieve all users
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Long userId = (Long) session.getAttribute("userId");
        try {
            return ResponseEntity.ok(userResponse(userService.getUserById(userId)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/staff")
    public ResponseEntity<List<Map<String, Object>>> getStaffUsers() {
        List<Map<String, Object>> staff = userService.getStaffUsers().stream()
                .map(this::userResponse)
                .toList();
        return ResponseEntity.ok(staff);
    }

    @PostMapping("/staff")
    public ResponseEntity<?> createStaff(@RequestBody User staff) {
        try {
            User createdStaff = userService.createStaff(staff);
            return new ResponseEntity<>(userResponse(createdStaff), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Staff creation error: " + e.getMessage());
        }
    }

    @PutMapping("/staff/{id}")
    public ResponseEntity<?> updateStaff(@PathVariable Long id, @RequestBody User updates) {
        try {
            return ResponseEntity.ok(userResponse(userService.updateStaff(id, updates)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/staff/{id}")
    public ResponseEntity<?> deleteStaff(@PathVariable Long id) {
        try {
            userService.deleteStaff(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private Map<String, Object> userResponse(User user) {
        return Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "phone", user.getPhone(),
                "role", user.getRole(),
                "active", user.isActive());
    }
}
