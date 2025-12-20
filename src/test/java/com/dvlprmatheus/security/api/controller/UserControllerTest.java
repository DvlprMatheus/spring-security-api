package com.dvlprmatheus.security.api.controller;

import com.dvlprmatheus.security.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class UserControllerTest {
    
    @Autowired
    private WebApplicationContext webApplicationContext;
    
    private MockMvc mockMvc;
    
    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }
    
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
    
    @Test
    void test_ShouldReturnOkWithUserInfo_WhenUserIsAuthenticated() throws Exception {
        // Arrange
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .build();
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        
        // Act & Assert
        mockMvc.perform(get("/v1/user/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Authentication working correctly!"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }
    
    @Test
    void test_ShouldReturnOkWithUnauthenticatedMessage_WhenUserIsNotAuthenticated() throws Exception {
        // Arrange - SecurityContext já está limpo pelo @BeforeEach
        
        // Act & Assert
        mockMvc.perform(get("/v1/user/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User not authenticated"))
                .andExpect(jsonPath("$.username").isEmpty());
    }
}

