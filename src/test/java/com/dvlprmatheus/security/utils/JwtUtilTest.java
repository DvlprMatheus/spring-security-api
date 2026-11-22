package com.dvlprmatheus.security.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.dvlprmatheus.security.config.jwt.JwtProperties;
import com.dvlprmatheus.security.entity.User;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

  private static final String SECRET_KEY =
      "a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2";

  private static final long EXPIRATION_TIME = 86400000L;

  private static final String USERNAME = "testuser";

  private static final String EMAIL = "test@example.com";

  private static final String ENCODED_PASSWORD = "encodedPassword";

  @Mock private JwtProperties jwtProperties;

  private JwtUtil jwtUtil;

  private User user;

  @BeforeEach
  void setUp() {
    when(jwtProperties.getSecret()).thenReturn(SECRET_KEY);
    lenient().when(jwtProperties.getExpiration()).thenReturn(EXPIRATION_TIME);

    jwtUtil = new JwtUtil(jwtProperties);

    user = User.builder().username(USERNAME).email(EMAIL).password(ENCODED_PASSWORD).build();
  }

  @Test
  @DisplayName("generateToken - should return a valid token")
  void generateToken_ShouldReturnValidToken() {
    String token = jwtUtil.generateToken(user);

    assertThat(token).isNotNull().isNotEmpty();
  }

  @Test
  @DisplayName("extractUsername - should return correct username when token is valid")
  void extractUsername_ShouldReturnCorrectUsername_WhenTokenIsValid() {
    String token = jwtUtil.generateToken(user);
    String username = jwtUtil.extractUsername(token);

    assertThat(username).isEqualTo(USERNAME);
  }

  @Test
  @DisplayName("extractExpiration - should return a future date when token is valid")
  void extractExpiration_ShouldReturnFutureDate_WhenTokenIsValid() {
    String token = jwtUtil.generateToken(user);
    Date expiration = jwtUtil.extractExpiration(token);

    assertThat(expiration).isNotNull();
    assertThat(expiration).isAfter(new Date());
  }

  @Test
  @DisplayName("validateToken - should return true when token is valid")
  void validateToken_ShouldReturnTrue_WhenTokenIsValid() {
    String token = jwtUtil.generateToken(user);
    Boolean isValid = jwtUtil.validateToken(token, user);

    assertThat(isValid).isTrue();
  }

  @Test
  @DisplayName("validateToken - should return false when username does not match")
  void validateToken_ShouldReturnFalse_WhenUsernameDoesNotMatch() {
    String token = jwtUtil.generateToken(user);
    User differentUser =
        User.builder()
            .username("differentuser")
            .email("different@example.com")
            .password(ENCODED_PASSWORD)
            .build();
    Boolean isValid = jwtUtil.validateToken(token, differentUser);

    assertThat(isValid).isFalse();
  }

  @Test
  @DisplayName("isTokenExpired - should return false when token is not expired")
  void isTokenExpired_ShouldReturnFalse_WhenTokenIsNotExpired() {
    String token = jwtUtil.generateToken(user);
    Boolean isExpired = jwtUtil.isTokenExpired(token);

    assertThat(isExpired).isFalse();
  }

  @Test
  @DisplayName("extractClaim - should return correct claim")
  void extractClaim_ShouldReturnCorrectClaim() {
    String token = jwtUtil.generateToken(user);
    String subject = jwtUtil.extractClaim(token, claims -> claims.getSubject());

    assertThat(subject).isEqualTo(USERNAME);
  }

  @Test
  @DisplayName("generateToken - should generate different tokens when called multiple times")
  void generateToken_ShouldGenerateDifferentTokens_WhenCalledMultipleTimes() {
    String token1 = jwtUtil.generateToken(user);
    String token2 = jwtUtil.generateToken(user);

    assertThat(token1).isNotEqualTo(token2);
  }

  @Test
  @DisplayName("extractUsername - should throw exception when token is invalid")
  void extractUsername_ShouldThrowException_WhenTokenIsInvalid() {
    String invalidToken = "invalid.token.here";

    assertThatThrownBy(() -> jwtUtil.extractUsername(invalidToken)).isInstanceOf(Exception.class);
  }
}
