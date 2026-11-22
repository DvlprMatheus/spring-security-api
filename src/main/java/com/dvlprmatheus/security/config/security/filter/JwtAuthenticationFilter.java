package com.dvlprmatheus.security.config.security.filter;

import com.dvlprmatheus.security.utils.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtUtil jwtUtil;
  private final UserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      validateAndSetAuthentication(request);
    } catch (RuntimeException e) {
      log.error("Unexpected error occurred during JWT authentication", e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }
    filterChain.doFilter(request, response);
  }

  private void validateAndSetAuthentication(HttpServletRequest request) {
    if (isUserAlreadyAuthenticated()) {
      log.debug("User is already authenticated in the security context");
      return;
    }
    String jwt = extractJwtToken(request);
    if (jwt == null) {
      return;
    }
    String username = extractUsernameFromJwtToken(jwt);
    if (username == null || username.isBlank()) {
      log.warn("JWT token does not contain a valid username");
      return;
    }
    UserDetails userDetails = loadUserDetails(username);
    if (userDetails == null) {
      return;
    }
    if (!isUserAccountValid(userDetails)) {
      log.warn("User account is not valid for authentication: {}", username);
      return;
    }
    if (!jwtUtil.validateToken(jwt, userDetails)) {
      log.warn("JWT token validation failed for user: {}", username);
      return;
    }
    setAuthentication(userDetails, request);
  }

  private String extractJwtToken(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
      log.debug("Authorization header is missing or invalid");
      return null;
    }
    String jwt = authHeader.substring(BEARER_PREFIX.length()).trim();
    return jwt.isEmpty() ? null : jwt;
  }

  private String extractUsernameFromJwtToken(String jwt) {
    try {
      String username = jwtUtil.extractUsername(jwt);
      log.debug("Extracted username from JWT token: {}", username);
      return username;
    } catch (JwtException | IllegalArgumentException e) {
      log.warn("Invalid JWT token: {}", e.getMessage());
      return null;
    }
  }

  private boolean isUserAlreadyAuthenticated() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication != null
        && authentication.isAuthenticated()
        && !(authentication instanceof AnonymousAuthenticationToken);
  }

  private UserDetails loadUserDetails(String username) {
    try {
      UserDetails userDetails = userDetailsService.loadUserByUsername(username);
      log.debug("Loaded user details for username: {}", username);
      return userDetails;
    } catch (UsernameNotFoundException e) {
      log.warn("User not found for username: {}", username);
      return null;
    }
  }

  private boolean isUserAccountValid(UserDetails userDetails) {
    return userDetails.isEnabled()
        && userDetails.isAccountNonExpired()
        && userDetails.isAccountNonLocked()
        && userDetails.isCredentialsNonExpired();
  }

  private void setAuthentication(UserDetails userDetails, HttpServletRequest request) {
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    log.debug("Authentication set in security context for user: {}", userDetails.getUsername());
  }
}
