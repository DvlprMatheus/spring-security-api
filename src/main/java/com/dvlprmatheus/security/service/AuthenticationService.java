package com.dvlprmatheus.security.service;

import com.dvlprmatheus.security.api.exception.AuthenticationFailedException;
import com.dvlprmatheus.security.api.exception.EmailAlreadyExistsException;
import com.dvlprmatheus.security.api.exception.UsernameAlreadyExistsException;
import com.dvlprmatheus.security.api.request.LoginRequest;
import com.dvlprmatheus.security.api.request.RegisterRequest;
import com.dvlprmatheus.security.api.response.AuthResponse;
import com.dvlprmatheus.security.entity.User;
import com.dvlprmatheus.security.repository.UserRepository;
import com.dvlprmatheus.security.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Attempting to register new user with username: {}", request.getUsername());
        
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed: username already exists - {}", request.getUsername());
            throw new UsernameAlreadyExistsException(request.getUsername());
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: email already exists - {}", request.getEmail());
            throw new EmailAlreadyExistsException(request.getEmail());
        }
        
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        
        user = userRepository.save(user);
        log.debug("User created successfully with ID: {}", user.getId());
        
        String token = jwtUtil.generateToken(user);
        log.info("User registered successfully: {}", request.getUsername());
        
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .username(user.getUsername())
                .build();
    }
    
    public AuthResponse login(LoginRequest request) {
        log.info("Attempting to login user: {}", request.getUsername());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
            
            User user = (User) authentication.getPrincipal();
            log.debug("User authenticated successfully: {}", user.getUsername());
            
            String token = jwtUtil.generateToken(user);
            log.info("Login successful for user: {}", request.getUsername());
            
            return AuthResponse.builder()
                    .token(token)
                    .type("Bearer")
                    .username(user.getUsername())
                    .build();
        } catch (BadCredentialsException e) {
            log.warn("Login failed: invalid credentials for user: {}", request.getUsername());
            throw new AuthenticationFailedException("Invalid credentials. Please check your username and password.", e);
        } catch (AuthenticationException e) {
            log.warn("Login failed: authentication exception for user: {} - {}", request.getUsername(), e.getMessage());
            throw new AuthenticationFailedException("Authentication failed: " + e.getMessage(), e);
        }
    }
}

