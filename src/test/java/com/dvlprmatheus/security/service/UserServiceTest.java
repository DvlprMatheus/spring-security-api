package com.dvlprmatheus.security.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dvlprmatheus.security.entity.User;
import com.dvlprmatheus.security.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  private static final String USERNAME = "testuser";

  @Mock private UserRepository userRepository;

  @InjectMocks private UserService userService;

  @Test
  @DisplayName("loadUserByUsername - should return user when username exists")
  void loadUserByUsername_ShouldReturnUserWhenUsernameExists() {
    User user = mock(User.class);
    when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

    UserDetails result = userService.loadUserByUsername(USERNAME);

    assertThat(result).isNotNull().isSameAs(user);

    verify(userRepository).findByUsername(USERNAME);
  }

  @Test
  @DisplayName("loadUserByUsername - should throw exception when username does not exist")
  void loadUserByUsername_ShouldThrowExceptionWhenUsernameDoesNotExist() {
    when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.loadUserByUsername(USERNAME))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessageContaining("User not found: " + USERNAME);

    verify(userRepository).findByUsername(USERNAME);
  }

  @Test
  @DisplayName("loadUserByUsername - should throw exception when database is unavailable")
  void loadUserByUsername_ShouldThrowExceptionWhenDatabaseIsUnavailable() {
    DataAccessResourceFailureException exception = mock(DataAccessResourceFailureException.class);

    when(userRepository.findByUsername(USERNAME)).thenThrow(exception);

    assertThatThrownBy(() -> userService.loadUserByUsername(USERNAME))
        .isSameAs(exception)
        .isNotInstanceOf(UsernameNotFoundException.class);

    verify(userRepository).findByUsername(USERNAME);
  }
}
