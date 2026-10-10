
package com.travelbooking.controller;

import com.travelbooking.entity.User;
import com.travelbooking.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            // Public registration must never create privileged accounts.
            user.setRole("USER");
            User createdUser = userService.registerUser(user);
            return new ResponseEntity<>(userResponse(createdUser), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Registration failed.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> credentials,
            HttpServletRequest request) {
        return authenticate(credentials, request, "USER");
    }

    @PostMapping("/admin-login")
    public ResponseEntity<?> adminLogin(
            @RequestBody Map<String, String> credentials,
            HttpServletRequest request) {
        return authenticate(credentials, request, "ADMIN");
    }

    @PostMapping("/staff-login")
    public ResponseEntity<?> staffLogin(
            @RequestBody Map<String, String> credentials,
            HttpServletRequest request) {
        return authenticate(credentials, request, "STAFF");
    }

    private ResponseEntity<?> authenticate(
            Map<String, String> credentials,
            HttpServletRequest request,
            String requiredRole) {

        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        String email = credentials.get("email");
        String password = credentials.get("password");

        if (email == null || email.isBlank()
                || password == null || password.isBlank()) {
            return ResponseEntity.badRequest()
                    .body("Email and password are required.");
        }

        try {
            User user = userService.login(email.trim(), password);

            if (!requiredRole.equalsIgnoreCase(user.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("This account cannot use this login portal.");
            }

            HttpSession session = request.getSession(true);
            session.setAttribute("userId", user.getId());
            session.setAttribute("role", user.getRole().toUpperCase());
            session.setMaxInactiveInterval(30 * 60);

            return ResponseEntity.ok(userResponse(user));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Login failed. Please try again.");
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

    @GetMapping
    public ResponseEntity<?> getAllUsers(HttpServletRequest request) {
        if (!hasRole(request, "ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Administrator access required.");
        }
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Please sign in.");
        }

        try {
            Long userId = (Long) session.getAttribute("userId");
            return ResponseEntity.ok(userResponse(userService.getUserById(userId)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found.");
        }
    }

    @GetMapping("/staff")
    public ResponseEntity<?> getStaffUsers(HttpServletRequest request) {
        if (!hasRole(request, "ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Administrator access required.");
        }

        List<Map<String, Object>> staff = userService.getStaffUsers()
                .stream()
                .map(this::userResponse)
                .toList();

        return ResponseEntity.ok(staff);
    }

    @PostMapping("/staff")
    public ResponseEntity<?> createStaff(
            @RequestBody User staff,
            HttpServletRequest request) {
        if (!hasRole(request, "ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Administrator access required.");
        }

        try {
            User createdStaff = userService.createStaff(staff);
            return new ResponseEntity<>(userResponse(createdStaff), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Staff creation failed.");
        }
    }

    @PutMapping("/staff/{id}")
    public ResponseEntity<?> updateStaff(
            @PathVariable Long id,
            @RequestBody User updates,
            HttpServletRequest request) {
        if (!hasRole(request, "ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Administrator access required.");
        }

        try {
            return ResponseEntity.ok(
                    userResponse(userService.updateStaff(id, updates)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/staff/{id}")
    public ResponseEntity<?> deleteStaff(
            @PathVariable Long id,
            HttpServletRequest request) {
        if (!hasRole(request, "ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Administrator access required.");
        }

        try {
            userService.deleteStaff(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private boolean hasRole(HttpServletRequest request, String role) {
        HttpSession session = request.getSession(false);
        return session != null
                && role.equals(session.getAttribute("role"));
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
