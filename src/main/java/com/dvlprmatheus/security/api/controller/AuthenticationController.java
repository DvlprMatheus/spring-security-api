package com.dvlprmatheus.security.api.controller;

import com.dvlprmatheus.security.api.request.LoginRequest;
import com.dvlprmatheus.security.api.request.RegisterRequest;
import com.dvlprmatheus.security.api.response.AuthResponse;
import com.dvlprmatheus.security.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    
    private final AuthenticationService authenticationService;
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Received registration request for username: {}", request.getUsername());
        AuthResponse response = authenticationService.register(request);
        log.debug("Registration completed successfully for username: {}", request.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Received login request for username: {}", request.getUsername());
        AuthResponse response = authenticationService.login(request);
        log.debug("Login request processed for username: {}", request.getUsername());
        return ResponseEntity.ok(response);
    }
}

