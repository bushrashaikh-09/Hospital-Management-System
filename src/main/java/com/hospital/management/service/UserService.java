package com.hospital.management.service;

import com.hospital.management.entity.User;
import com.hospital.management.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> findRecentUsers() {
        return userRepository.findTop10ByOrderByCreatedAtDesc();
    }

    public List<User> findPatients() {
        return userRepository.findByRole("USER");
    }

    public long countByRole(String role) {
        return userRepository.countByRole(role);
    }

    public User registerUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User data is required");
        }

        String fullName = user.getFullName() == null ? "" : user.getFullName().trim();
        String email = user.getEmail() == null ? "" : user.getEmail().trim().toLowerCase();
        String password = user.getPassword() == null ? "" : user.getPassword().trim();

        if (fullName.isEmpty()) {
            throw new IllegalArgumentException("Full name is required");
        }
        if (email.isEmpty() || !email.contains("@")) {
            throw new IllegalArgumentException("Please enter a valid email");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole("USER");
        return userRepository.save(user);
    }

    public User createAdminIfNotExists() {
        Optional<User> existingAdmin = userRepository.findByEmail("admin@hospital.com");
        if (existingAdmin.isPresent()) {
            return existingAdmin.get();
        }

        User admin = new User();
        admin.setFullName("System Administrator");
        admin.setEmail("admin@hospital.com");
        admin.setPassword("admin123");
        admin.setRole("ADMIN");
        return userRepository.save(admin);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User save(User user) {
        return userRepository.save(user);
    }
}
