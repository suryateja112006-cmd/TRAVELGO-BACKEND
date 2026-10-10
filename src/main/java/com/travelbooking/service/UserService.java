package com.travelbooking.service;

import com.travelbooking.entity.User;
import com.travelbooking.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("An account with this email already exists!");
        }
        user.setRole("USER");
        return userRepository.save(user);
    }

    public User login(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(u -> u.getPassword().equals(password) && ("STAFF".equals(u.getRole()) ? u.isActive() : true))
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password!"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getStaffUsers() {
        return userRepository.findByRoleOrderByNameAsc("STAFF");
    }

    public User createStaff(User staff) {
        if (staff.getName() == null || staff.getName().isBlank()
                || staff.getEmail() == null || staff.getEmail().isBlank()
                || staff.getPhone() == null || staff.getPhone().isBlank()
                || staff.getPassword() == null || staff.getPassword().isBlank()) {
            throw new IllegalArgumentException("Name, email, phone, and password are required.");
        }
        if (staff.getPassword().length() < 6) {
            throw new IllegalArgumentException("Staff passwords must be at least 6 characters.");
        }

        staff.setName(staff.getName().trim());
        staff.setEmail(staff.getEmail().trim());
        staff.setPhone(staff.getPhone().trim());
        staff.setId(null);
        if (userRepository.existsByEmail(staff.getEmail())) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        staff.setRole("STAFF");
        return userRepository.save(staff);
    }

    public User updateStaff(Long staffId, User updates) {
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff member not found."));
        if (!"STAFF".equals(staff.getRole())) {
            throw new IllegalArgumentException("The selected account is not a staff member.");
        }
        if (!staff.isActive()) {
            throw new IllegalArgumentException("Deleted staff accounts cannot be edited.");
        }
        if (updates.getName() == null || updates.getName().isBlank()
                || updates.getEmail() == null || updates.getEmail().isBlank()
                || updates.getPhone() == null || updates.getPhone().isBlank()) {
            throw new IllegalArgumentException("Name, email, and phone are required.");
        }

        String email = updates.getEmail().trim();
        if (!email.equalsIgnoreCase(staff.getEmail()) && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        String password = updates.getPassword();
        if (password != null && !password.isBlank() && password.length() < 6) {
            throw new IllegalArgumentException("Staff passwords must be at least 6 characters.");
        }

        staff.setName(updates.getName().trim());
        staff.setEmail(email);
        staff.setPhone(updates.getPhone().trim());
        if (password != null && !password.isBlank()) {
            staff.setPassword(password);
        }
        return userRepository.save(staff);
    }

    @Transactional
    public void deleteStaff(Long staffId) {
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff member not found."));
        if (!"STAFF".equals(staff.getRole())) {
            throw new IllegalArgumentException("The selected account is not a staff member.");
        }
        if (!staff.isActive()) {
            throw new IllegalArgumentException("Staff member has already been deleted.");
        }
        staff.setActive(false);
        userRepository.save(staff);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }
}
