package com.dvlprmatheus.security.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dvlprmatheus.security.api.exception.AuthenticationFailedException;
import com.dvlprmatheus.security.api.exception.EmailAlreadyExistsException;
import com.dvlprmatheus.security.api.exception.UsernameAlreadyExistsException;
import com.dvlprmatheus.security.api.request.LoginRequest;
import com.dvlprmatheus.security.api.request.RegisterRequest;
import com.dvlprmatheus.security.api.response.AuthResponse;
import com.dvlprmatheus.security.service.AuthenticationService;
import com.dvlprmatheus.security.utils.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthenticationControllerTest {

  private static final String REGISTER_URL = "/auth/register";
  private static final String LOGIN_URL = "/auth/login";

  private static final String USERNAME = "testuser";
  private static final String EMAIL = "test@example.com";
  private static final String PASSWORD = "password123";

  private static final String JWT_TOKEN = "jwt-token";
  private static final String TOKEN_TYPE = "Bearer";

  @Autowired private MockMvc mockMvc;

  @Autowired private JsonMapper jsonMapper;

  @MockitoBean private AuthenticationService authenticationService;

  @MockitoBean private JwtUtil jwtUtil;

  @Test
  @DisplayName("register - should return created when registration is successful")
  void register_ShouldReturnCreated_WhenRequestIsValid() throws Exception {
    RegisterRequest request = createRegisterRequest();

    AuthResponse response = createAuthResponse();

    when(authenticationService.register(any(RegisterRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.token").value(JWT_TOKEN))
        .andExpect(jsonPath("$.type").value(TOKEN_TYPE))
        .andExpect(jsonPath("$.username").value(USERNAME));

    ArgumentCaptor<RegisterRequest> captor = ArgumentCaptor.forClass(RegisterRequest.class);

    verify(authenticationService).register(captor.capture());

    assertThat(captor.getValue())
        .satisfies(
            capturedRequest -> {
              assertThat(capturedRequest.getUsername()).isEqualTo(USERNAME);
              assertThat(capturedRequest.getEmail()).isEqualTo(EMAIL);
              assertThat(capturedRequest.getPassword()).isEqualTo(PASSWORD);
            });
  }

  @Test
  @DisplayName("register - should return bad request when registration validation fails")
  void register_ShouldReturnBadRequest_WhenValidationFails() throws Exception {
    RegisterRequest request =
        RegisterRequest.builder().username("").email("invalid-email").password("123").build();

    mockMvc
        .perform(
            post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());

    verify(authenticationService, never()).register(any(RegisterRequest.class));
  }

  @Test
  @DisplayName("register - should return conflict when username already exists")
  void register_ShouldReturnConflict_WhenUsernameExists() throws Exception {
    RegisterRequest request =
        RegisterRequest.builder().username(USERNAME).email(EMAIL).password(PASSWORD).build();

    when(authenticationService.register(any(RegisterRequest.class)))
        .thenThrow(new UsernameAlreadyExistsException(USERNAME));

    mockMvc
        .perform(
            post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").exists());

    verify(authenticationService).register(any(RegisterRequest.class));
  }

  @Test
  @DisplayName("register - should return conflict when email already exists")
  void register_ShouldReturnConflict_WhenEmailExists() throws Exception {
    RegisterRequest request =
        RegisterRequest.builder().username(USERNAME).email(EMAIL).password(PASSWORD).build();

    when(authenticationService.register(any(RegisterRequest.class)))
        .thenThrow(new EmailAlreadyExistsException(EMAIL));

    mockMvc
        .perform(
            post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").exists());

    verify(authenticationService).register(any(RegisterRequest.class));
  }

  @Test
  @DisplayName("login - should return ok when login is successful")
  void login_ShouldReturnOk_WhenCredentialsAreValid() throws Exception {
    LoginRequest request = createLoginRequest();

    AuthResponse response = createAuthResponse();

    when(authenticationService.login(any(LoginRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value(JWT_TOKEN))
        .andExpect(jsonPath("$.type").value(TOKEN_TYPE))
        .andExpect(jsonPath("$.username").value(USERNAME));

    ArgumentCaptor<LoginRequest> captor = ArgumentCaptor.forClass(LoginRequest.class);

    verify(authenticationService).login(captor.capture());

    assertThat(captor.getValue())
        .satisfies(
            capturedRequest -> {
              assertThat(capturedRequest.getUsername()).isEqualTo(USERNAME);
              assertThat(capturedRequest.getPassword()).isEqualTo(PASSWORD);
            });
  }

  @Test
  @DisplayName("login - should return bad request when login validation fails")
  void login_ShouldReturnBadRequest_WhenValidationFails() throws Exception {
    LoginRequest request = LoginRequest.builder().username("").password("").build();

    mockMvc
        .perform(
            post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());

    verify(authenticationService, never()).login(any(LoginRequest.class));
  }

  @Test
  @DisplayName("login - should return unauthorized when credentials are invalid")
  void login_ShouldReturnUnauthorized_WhenCredentialsAreInvalid() throws Exception {
    LoginRequest request =
        LoginRequest.builder().username(USERNAME).password("wrongpassword").build();

    when(authenticationService.login(any(LoginRequest.class)))
        .thenThrow(new AuthenticationFailedException("Invalid credentials"));

    mockMvc
        .perform(
            post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").exists());

    verify(authenticationService).login(any(LoginRequest.class));
  }

  private RegisterRequest createRegisterRequest() {
    return RegisterRequest.builder().username(USERNAME).email(EMAIL).password(PASSWORD).build();
  }

  private LoginRequest createLoginRequest() {
    return LoginRequest.builder().username(USERNAME).password(PASSWORD).build();
  }

  private AuthResponse createAuthResponse() {
    return AuthResponse.builder().token(JWT_TOKEN).type(TOKEN_TYPE).username(USERNAME).build();
  }
}
