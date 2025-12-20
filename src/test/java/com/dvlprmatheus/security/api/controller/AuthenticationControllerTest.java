package com.dvlprmatheus.security.api.controller;

import com.dvlprmatheus.security.api.exception.EmailAlreadyExistsException;
import com.dvlprmatheus.security.api.exception.UsernameAlreadyExistsException;
import com.dvlprmatheus.security.api.request.LoginRequest;
import com.dvlprmatheus.security.api.request.RegisterRequest;
import com.dvlprmatheus.security.api.response.AuthResponse;
import com.dvlprmatheus.security.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class AuthenticationControllerTest {
    
    @Autowired
    private WebApplicationContext webApplicationContext;
    
    private MockMvc mockMvc;
    
    @Autowired
    private AuthenticationService authenticationService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public AuthenticationService authenticationService() {
            return mock(AuthenticationService.class);
        }
        
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }
    
    @Test
    void register_ShouldReturnCreated_WhenRequestIsValid() throws Exception {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();
        
        AuthResponse response = AuthResponse.builder()
                .token("jwt-token")
                .type("Bearer")
                .username("testuser")
                .build();
        
        when(authenticationService.register(any(RegisterRequest.class))).thenReturn(response);
        
        // Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }
    
    @Test
    void register_ShouldReturnBadRequest_WhenValidationFails() throws Exception {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .username("") // Inválido
                .email("invalid-email") // Inválido
                .password("123") // Muito curto
                .build();
        
        // Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void register_ShouldReturnConflict_WhenUsernameExists() throws Exception {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .username("existinguser")
                .email("test@example.com")
                .password("password123")
                .build();
        
        when(authenticationService.register(any(RegisterRequest.class)))
                .thenThrow(new UsernameAlreadyExistsException("existinguser"));
        
        // Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }
    
    @Test
    void register_ShouldReturnConflict_WhenEmailExists() throws Exception {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser")
                .email("existing@example.com")
                .password("password123")
                .build();
        
        when(authenticationService.register(any(RegisterRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("existing@example.com"));
        
        // Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }
    
    @Test
    void login_ShouldReturnOk_WhenCredentialsAreValid() throws Exception {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();
        
        AuthResponse response = AuthResponse.builder()
                .token("jwt-token")
                .type("Bearer")
                .username("testuser")
                .build();
        
        when(authenticationService.login(any(LoginRequest.class))).thenReturn(response);
        
        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }
    
    @Test
    void login_ShouldReturnBadRequest_WhenValidationFails() throws Exception {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .username("") // Inválido
                .password("") // Inválido
                .build();
        
        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void login_ShouldReturnUnauthorized_WhenCredentialsAreInvalid() throws Exception {
        // Arrange
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("wrongpassword")
                .build();
        
        when(authenticationService.login(any(LoginRequest.class)))
                .thenThrow(new com.dvlprmatheus.security.api.exception.AuthenticationFailedException("Invalid credentials"));
        
        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }
}
