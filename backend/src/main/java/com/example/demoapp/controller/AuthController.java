// src/main/java/com/example/demoapp/controller/AuthController.java
package com.example.demoapp.controller;

import com.example.demoapp.dto.*;
import com.example.demoapp.model.User;
import com.example.demoapp.repository.UserRepository;
import com.example.demoapp.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        log.info("📝 Registration request for: {}", request.getUsername());

        // Validate input
        if (request.getUsername() == null || request.getPassword() == null ||
            request.getUsername().trim().isEmpty() || request.getPassword().isEmpty()) {
            log.warn("Invalid registration request: missing username or password");
            return ResponseEntity.badRequest().body("Username and password are required");
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            log.warn("Registration failed: username '{}' already exists", request.getUsername());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already taken");
        }

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Hash the password
        userRepository.save(user);

        log.info("✅ User registered successfully: {}", request.getUsername());
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        log.info("🔐 Login request received for: {}", request.getUsername());

        if (request.getUsername() == null || request.getPassword() == null) {
            log.warn("Login failed: missing username or password");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // Find user by username
        var userOpt = userRepository.findByUsername(request.getUsername().trim());
        if (userOpt.isEmpty()) {
            log.warn("Login failed: user not found - {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userOpt.get();

        // Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed: invalid password for user - {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getUsername());
        log.info("✅ Login successful for: {}", user.getUsername());

        return ResponseEntity.ok(new AuthResponse(token, user.getUsername()));
    }
}
