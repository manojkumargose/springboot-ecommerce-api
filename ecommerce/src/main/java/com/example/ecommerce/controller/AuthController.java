package com.example.ecommerce.controller;

import com.example.ecommerce.dto.ApiResponse;
import com.example.ecommerce.dto.AuthRequest;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.security.JwtUtil;
import com.example.ecommerce.security.TokenBlacklistService;
import com.example.ecommerce.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          TokenBlacklistService tokenBlacklistService,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, String>>> register(
            @Valid @RequestBody AuthRequest request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Username already exists"));
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("ROLE_USER");
        user.setEmail(request.getEmail());
        userRepository.save(user);

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            emailService.sendWelcomeEmail(request.getEmail(), request.getUsername());
        }

        String accessToken = jwtUtil.generateToken(request.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(request.getUsername());

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);

        return ResponseEntity.ok(ApiResponse.success("Registration successful", tokens));
    }

    @PostMapping("/register-admin")
    public ResponseEntity<ApiResponse<Map<String, String>>> registerAdminPublic(
            @Valid @RequestBody AuthRequest request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Username already exists"));
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("ROLE_ADMIN");
        user.setEmail(request.getEmail());
        userRepository.save(user);

        String accessToken = jwtUtil.generateToken(request.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(request.getUsername());

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);

        return ResponseEntity.ok(ApiResponse.success("Admin registered successfully", tokens));
    }

    @PostMapping("/register/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, String>>> registerAdmin(
            @Valid @RequestBody AuthRequest request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Username already exists"));
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("ROLE_ADMIN");
        user.setEmail(request.getEmail());
        userRepository.save(user);

        String accessToken = jwtUtil.generateToken(request.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(request.getUsername());

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);

        return ResponseEntity.ok(ApiResponse.success("Admin registered successfully", tokens));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> login(
            @Valid @RequestBody AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()));

        String accessToken = jwtUtil.generateToken(request.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(request.getUsername());

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        return ResponseEntity.ok(ApiResponse.success("Login successful", tokens));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, String>>> refresh(
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Missing or invalid Authorization header"));
        }

        String refreshToken = authHeader.substring(7);

        if (tokenBlacklistService.isBlacklisted(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Refresh token has been revoked"));
        }

        if (jwtUtil.isTokenExpired(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Refresh token has expired, please login again"));
        }

        if (!jwtUtil.isRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid token type"));
        }

        String username = jwtUtil.extractUsername(refreshToken);
        if (userRepository.findByUsername(username).isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("User not found"));
        }

        tokenBlacklistService.blacklistToken(refreshToken);

        String newAccessToken = jwtUtil.generateToken(username);
        String newRefreshToken = jwtUtil.generateRefreshToken(username);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", newAccessToken);
        tokens.put("refreshToken", newRefreshToken);

        return ResponseEntity.ok(ApiResponse.success("Token refreshed", tokens));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        tokenBlacklistService.blacklistToken(token);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }
}
