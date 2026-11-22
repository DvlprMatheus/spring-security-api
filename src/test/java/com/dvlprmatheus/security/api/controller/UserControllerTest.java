package com.dvlprmatheus.security.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dvlprmatheus.security.entity.User;
import com.dvlprmatheus.security.utils.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
class UserControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private JwtUtil jwtUtil;

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("test - should return ok with user info when user is authenticated")
  void shouldReturnOkWithUserInfoWhenUserIsAuthenticated() throws Exception {
    User user =
        User.builder()
            .username("testuser")
            .email("test@example.com")
            .password("encodedPassword")
            .build();

    Authentication authentication =
        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);

    mockMvc
        .perform(get("/v1/user/test"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Authentication working correctly!"))
        .andExpect(jsonPath("$.username").value("testuser"));
  }

  @Test
  @DisplayName(
      "test - should return ok with unauthenticated message when user is not authenticated")
  void shouldReturnOkWithUnauthenticatedMessageWhenUserIsNotAuthenticated() throws Exception {

    mockMvc.perform(get("/v1/user/test")).andExpect(status().isUnauthorized());
  }
}
