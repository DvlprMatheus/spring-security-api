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
        when(jwtProperties.getSecret()).thenReturn("a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2");
        when(jwtProperties.getExpiration()).thenReturn(86400000L);
        
        jwtUtil = new JwtUtil(jwtProperties);
        
        user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .build();
    }
    
    @Test
    void generateToken_ShouldReturnValidToken() {
        String token = jwtUtil.generateToken(user);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }
    
    @Test
    void extractUsername_ShouldReturnCorrectUsername_WhenTokenIsValid() {
        String token = jwtUtil.generateToken(user);
        
        String username = jwtUtil.extractUsername(token);
        
        assertEquals("testuser", username);
    }
    
    @Test
    void extractExpiration_ShouldReturnFutureDate_WhenTokenIsValid() {
        String token = jwtUtil.generateToken(user);
        
        Date expiration = jwtUtil.extractExpiration(token);
        
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }
    
    @Test
    void validateToken_ShouldReturnTrue_WhenTokenIsValid() {
        String token = jwtUtil.generateToken(user);
        
        Boolean isValid = jwtUtil.validateToken(token, user);
        
        assertTrue(isValid);
    }
    
    @Test
    void validateToken_ShouldReturnFalse_WhenUsernameDoesNotMatch() {
        String token = jwtUtil.generateToken(user);
        User differentUser = User.builder()
                .username("differentuser")
                .email("different@example.com")
                .password("encodedPassword")
                .build();
        
        Boolean isValid = jwtUtil.validateToken(token, differentUser);
        
        assertFalse(isValid);
    }
    
    @Test
    void isTokenExpired_ShouldReturnFalse_WhenTokenIsNotExpired() {
        String token = jwtUtil.generateToken(user);
        
        Boolean isExpired = jwtUtil.isTokenExpired(token);
        
        assertFalse(isExpired);
    }
    
    @Test
    void extractClaim_ShouldReturnCorrectClaim() {
        String token = jwtUtil.generateToken(user);
        
        String subject = jwtUtil.extractClaim(token, claims -> claims.getSubject());
        
        assertEquals("testuser", subject);
    }
    
    @Test
    void generateToken_ShouldGenerateDifferentTokens_WhenCalledMultipleTimes() {
        String token1 = jwtUtil.generateToken(user);
        String token2 = jwtUtil.generateToken(user);
        
        assertNotEquals(token1, token2);
    }
    
    @Test
    void extractUsername_ShouldThrowException_WhenTokenIsInvalid() {
        String invalidToken = "invalid.token.here";
        
        assertThrows(Exception.class, () -> {
            jwtUtil.extractUsername(invalidToken);
        });
    }
}

