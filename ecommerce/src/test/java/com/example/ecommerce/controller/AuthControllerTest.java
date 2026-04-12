package com.example.ecommerce.controller;

import com.example.ecommerce.dto.AuthRequest;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.security.JwtUtil;
import com.example.ecommerce.security.TokenBlacklistService;
import com.example.ecommerce.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class AuthControllerTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtil jwtUtil;
    @Mock private TokenBlacklistService tokenBlacklistService;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;

    @InjectMocks
    private AuthController authController;

    @Test
    @DisplayName("Should register user successfully")
    void register_Success() {
        AuthRequest request = new AuthRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setEmail("new@test.com");

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPass");

        var result = authController.register(request);

        assertThat(result.getStatusCode().value()).isEqualTo(200);
        verify(userRepository).save(any());
    }

    @Test
    @DisplayName("Should reject duplicate username")
    void register_DuplicateUsername() {
        AuthRequest request = new AuthRequest();
        request.setUsername("existing");
        request.setPassword("password123");
        request.setEmail("dup@test.com");

        when(userRepository.findByUsername("existing")).thenReturn(Optional.of(new com.example.ecommerce.entity.User()));

        var result = authController.register(request);

        assertThat(result.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    @DisplayName("Should login successfully")
    void login_Success() {
        AuthRequest request = new AuthRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken("testuser", null));
        when(jwtUtil.generateToken("testuser")).thenReturn("fake-jwt-token");
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(new com.example.ecommerce.entity.User()));

        var result = authController.login(request);

        assertThat(result.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    @DisplayName("Should throw on invalid credentials")
    void login_InvalidCredentials() {
        AuthRequest request = new AuthRequest();
        request.setUsername("wrong");
        request.setPassword("wrong");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authController.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }
}