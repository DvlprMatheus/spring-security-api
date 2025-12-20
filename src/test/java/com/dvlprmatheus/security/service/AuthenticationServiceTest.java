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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @InjectMocks
    private AuthenticationService authenticationService;
    
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;
    
    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();
        
        loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();
        
        user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .build();
    }
    
    @Test
    void register_ShouldReturnAuthResponse_WhenUserIsCreatedSuccessfully() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtil.generateToken(any(User.class))).thenReturn("jwt-token");
        
        // Act
        AuthResponse response = authenticationService.register(registerRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getType());
        assertEquals("testuser", response.getUsername());
        
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).generateToken(any(User.class));
    }
    
    @Test
    void register_ShouldThrowUsernameAlreadyExistsException_WhenUsernameExists() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(true);
        
        // Act & Assert
        assertThrows(UsernameAlreadyExistsException.class, () -> {
            authenticationService.register(registerRequest);
        });
        
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void register_ShouldThrowEmailAlreadyExistsException_WhenEmailExists() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        
        // Act & Assert
        assertThrows(EmailAlreadyExistsException.class, () -> {
            authenticationService.register(registerRequest);
        });
        
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void login_ShouldReturnAuthResponse_WhenCredentialsAreValid() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateToken(any(User.class))).thenReturn("jwt-token");
        
        // Act
        AuthResponse response = authenticationService.login(loginRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getType());
        assertEquals("testuser", response.getUsername());
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken(any(User.class));
    }
    
    @Test
    void login_ShouldThrowAuthenticationFailedException_WhenCredentialsAreInvalid() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));
        
        // Act & Assert
        assertThrows(AuthenticationFailedException.class, () -> {
            authenticationService.login(loginRequest);
        });
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, never()).generateToken(any(User.class));
    }
    
    @Test
    void login_ShouldThrowAuthenticationFailedException_WhenAuthenticationFails() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new AuthenticationException("Authentication failed") {});
        
        // Act & Assert
        assertThrows(AuthenticationFailedException.class, () -> {
            authenticationService.login(loginRequest);
        });
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, never()).generateToken(any(User.class));
    }
}

