package com.dvlprmatheus.security.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

  private static final String USERNAME = "testuser";

  private static final String EMAIL = "test@example.com";

  private static final String PASSWORD = "password123";

  private static final String ENCODED_PASSWORD = "encodedPassword";

  private static final String JWT_TOKEN = "jwt-token";

  private static final String TOKEN_TYPE = "Bearer";

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private AuthenticationManager authenticationManager;

  @Mock private JwtUtil jwtUtil;

  @InjectMocks private AuthenticationService authenticationService;

  private RegisterRequest registerRequest;

  private LoginRequest loginRequest;

  private User user;

  @BeforeEach
  void setUp() {
    registerRequest =
        RegisterRequest.builder().username(USERNAME).email(EMAIL).password(PASSWORD).build();

    loginRequest = LoginRequest.builder().username(USERNAME).password(PASSWORD).build();

    user = User.builder().username(USERNAME).email(EMAIL).password(ENCODED_PASSWORD).build();
  }

  @Test
  @DisplayName("register - should return AuthResponse when user is created successfully")
  void register_ShouldReturnAuthResponse_WhenUserIsCreatedSuccessfully() {
    when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
    when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
    when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
    when(userRepository.save(any(User.class))).thenReturn(user);
    when(jwtUtil.generateToken(user)).thenReturn(JWT_TOKEN);

    AuthResponse response = authenticationService.register(registerRequest);

    assertThat(response)
        .isNotNull()
        .satisfies(
            authResponse -> {
              assertThat(authResponse.getToken()).isEqualTo(JWT_TOKEN);
              assertThat(authResponse.getType()).isEqualTo(TOKEN_TYPE);
              assertThat(authResponse.getUsername()).isEqualTo(USERNAME);
            });

    verify(userRepository).existsByUsername(USERNAME);
    verify(userRepository).existsByEmail(EMAIL);
    verify(passwordEncoder).encode(PASSWORD);

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());

    User savedUser = userCaptor.getValue();
    assertThat(savedUser.getUsername()).isEqualTo(USERNAME);
    assertThat(savedUser.getEmail()).isEqualTo(EMAIL);
    assertThat(savedUser.getPassword()).isEqualTo(ENCODED_PASSWORD);

    verify(jwtUtil).generateToken(user);
  }

  @Test
  @DisplayName("register - should throw UsernameAlreadyExistsException when username exists")
  void register_ShouldThrowUsernameAlreadyExistsException_WhenUsernameExists() {
    when(userRepository.existsByUsername(USERNAME)).thenReturn(true);

    assertThatThrownBy(() -> authenticationService.register(registerRequest))
        .isInstanceOf(UsernameAlreadyExistsException.class)
        .hasMessageContaining("Username already in use: " + USERNAME);

    verify(userRepository).existsByUsername(USERNAME);
    verify(userRepository, never()).existsByEmail(EMAIL);
    verify(passwordEncoder, never()).encode(anyString());
    verify(userRepository, never()).save(any(User.class));
    verify(jwtUtil, never()).generateToken(any(User.class));
  }

  @Test
  @DisplayName("register - should throw EmailAlreadyExistsException when email exists")
  void register_ShouldThrowEmailAlreadyExistsException_WhenEmailExists() {
    when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
    when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

    assertThatThrownBy(() -> authenticationService.register(registerRequest))
        .isInstanceOf(EmailAlreadyExistsException.class)
        .hasMessageContaining("Email already in use: " + EMAIL);

    verify(userRepository).existsByUsername(USERNAME);
    verify(userRepository).existsByEmail(EMAIL);
    verify(passwordEncoder, never()).encode(anyString());
    verify(userRepository, never()).save(any(User.class));
    verify(jwtUtil, never()).generateToken(any(User.class));
  }

  @Test
  @DisplayName("login - should return AuthResponse when credentials are valid")
  void login_ShouldReturnAuthResponse_WhenCredentialsAreValid() {
    Authentication authentication = mock(Authentication.class);

    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authentication);

    when(authentication.getPrincipal()).thenReturn(user);
    when(jwtUtil.generateToken(user)).thenReturn(JWT_TOKEN);

    AuthResponse response = authenticationService.login(loginRequest);

    assertThat(response)
        .isNotNull()
        .satisfies(
            authResponse -> {
              assertThat(authResponse.getToken()).isEqualTo(JWT_TOKEN);
              assertThat(authResponse.getType()).isEqualTo(TOKEN_TYPE);
              assertThat(authResponse.getUsername()).isEqualTo(USERNAME);
            });

    ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
        ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
    verify(authenticationManager).authenticate(captor.capture());

    UsernamePasswordAuthenticationToken capturedToken = captor.getValue();
    assertThat(capturedToken.getPrincipal()).isEqualTo(USERNAME);
    assertThat(capturedToken.getCredentials()).isEqualTo(PASSWORD);

    verify(jwtUtil).generateToken(user);
  }

  @Test
  @DisplayName("login - should throw AuthenticationFailedException when credentials are invalid")
  void login_ShouldThrowAuthenticationFailedException_WhenCredentialsAreInvalid() {
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(new BadCredentialsException("Bad credentials"));

    assertThatThrownBy(() -> authenticationService.login(loginRequest))
        .isInstanceOf(AuthenticationFailedException.class)
        .hasMessageContaining("Invalid credentials. Please check your username and password.");

    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    verify(jwtUtil, never()).generateToken(any(User.class));
  }

  @Test
  @DisplayName("login - should throw AuthenticationFailedException when authentication fails")
  void login_ShouldThrowAuthenticationFailedException_WhenAuthenticationFails() {
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(new AuthenticationException("Authentication failed") {});

    assertThatThrownBy(() -> authenticationService.login(loginRequest))
        .isInstanceOf(AuthenticationFailedException.class)
        .hasMessageContaining("Authentication failed");

    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    verify(jwtUtil, never()).generateToken(any(User.class));
  }
}
