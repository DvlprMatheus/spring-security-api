package com.dvlprmatheus.security.utils;

import com.dvlprmatheus.security.config.jwt.JwtProperties;
import com.dvlprmatheus.security.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {
    
    @Mock
    private JwtProperties jwtProperties;
    
    private JwtUtil jwtUtil;
    
    private User user;
    
    @BeforeEach
    void setUp() {
        // Configurar JwtProperties mock
        when(jwtProperties.getSecret()).thenReturn("a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2");
        when(jwtProperties.getExpiration()).thenReturn(86400000L); // 24 horas
        
        jwtUtil = new JwtUtil(jwtProperties);
        
        user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .build();
    }
    
    @Test
    void generateToken_ShouldReturnValidToken() {
        // Act
        String token = jwtUtil.generateToken(user);
        
        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }
    
    @Test
    void extractUsername_ShouldReturnCorrectUsername_WhenTokenIsValid() {
        // Arrange
        String token = jwtUtil.generateToken(user);
        
        // Act
        String username = jwtUtil.extractUsername(token);
        
        // Assert
        assertEquals("testuser", username);
    }
    
    @Test
    void extractExpiration_ShouldReturnFutureDate_WhenTokenIsValid() {
        // Arrange
        String token = jwtUtil.generateToken(user);
        
        // Act
        Date expiration = jwtUtil.extractExpiration(token);
        
        // Assert
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }
    
    @Test
    void validateToken_ShouldReturnTrue_WhenTokenIsValid() {
        // Arrange
        String token = jwtUtil.generateToken(user);
        
        // Act
        Boolean isValid = jwtUtil.validateToken(token, user);
        
        // Assert
        assertTrue(isValid);
    }
    
    @Test
    void validateToken_ShouldReturnFalse_WhenUsernameDoesNotMatch() {
        // Arrange
        String token = jwtUtil.generateToken(user);
        User differentUser = User.builder()
                .username("differentuser")
                .email("different@example.com")
                .password("encodedPassword")
                .build();
        
        // Act
        Boolean isValid = jwtUtil.validateToken(token, differentUser);
        
        // Assert
        assertFalse(isValid);
    }
    
    @Test
    void isTokenExpired_ShouldReturnFalse_WhenTokenIsNotExpired() {
        // Arrange
        String token = jwtUtil.generateToken(user);
        
        // Act
        Boolean isExpired = jwtUtil.isTokenExpired(token);
        
        // Assert
        assertFalse(isExpired);
    }
    
    @Test
    void extractClaim_ShouldReturnCorrectClaim() {
        // Arrange
        String token = jwtUtil.generateToken(user);
        
        // Act
        String subject = jwtUtil.extractClaim(token, claims -> claims.getSubject());
        
        // Assert
        assertEquals("testuser", subject);
    }
    
    @Test
    void generateToken_ShouldGenerateDifferentTokens_WhenCalledMultipleTimes() {
        // Act
        String token1 = jwtUtil.generateToken(user);
        String token2 = jwtUtil.generateToken(user);
        
        // Assert
        assertNotEquals(token1, token2); // Tokens devem ser diferentes devido ao timestamp
    }
    
    @Test
    void extractUsername_ShouldThrowException_WhenTokenIsInvalid() {
        // Arrange
        String invalidToken = "invalid.token.here";
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtUtil.extractUsername(invalidToken);
        });
    }
}

