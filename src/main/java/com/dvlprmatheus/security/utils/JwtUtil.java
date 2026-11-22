package com.dvlprmatheus.security.utils;

import com.dvlprmatheus.security.config.jwt.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtUtil {

  private final JwtProperties jwtProperties;
  private final SecretKey secretKey;

  public JwtUtil(JwtProperties jwtProperties) {
    this.jwtProperties = jwtProperties;
    this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
  }

  public String generateToken(UserDetails userDetails) {
    log.debug("Generating JWT token for user: {}", userDetails.getUsername());
    Map<String, Object> claims = new HashMap<>();
    String token = createToken(claims, userDetails.getUsername());
    log.debug("JWT token generated successfully for user: {}", userDetails.getUsername());
    return token;
  }

  private String createToken(Map<String, Object> claims, String subject) {
    Date now = new Date();
    Date expirationDate = new Date(now.getTime() + jwtProperties.getExpiration());
    return Jwts.builder()
        .id(UUID.randomUUID().toString())
        .claims(claims)
        .subject(subject)
        .issuedAt(now)
        .expiration(expirationDate)
        .signWith(secretKey)
        .compact();
  }

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
  }

  public boolean isTokenExpired(String token) {
    Date expiration = extractExpiration(token);
    return expiration == null || !expiration.after(new Date());
  }

  public boolean validateToken(String token, UserDetails userDetails) {
    try {
      Claims claims = extractAllClaims(token);
      String username = claims.getSubject();
      Date expiration = claims.getExpiration();
      boolean isValid =
          username != null
              && username.equals(userDetails.getUsername())
              && expiration != null
              && expiration.after(new Date());
      log.debug("Token validation result for user {}: {}", userDetails.getUsername(), isValid);
      return isValid;
    } catch (JwtException | IllegalArgumentException e) {
      log.warn("JWT validation failed: {}", e.getMessage());
      return false;
    }
  }
}
