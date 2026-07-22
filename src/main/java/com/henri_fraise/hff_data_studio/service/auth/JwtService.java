package com.henri_fraise.hff_data_studio.service.auth;

import com.henri_fraise.hff_data_studio.exception.TokenExpiredException;
import com.henri_fraise.hff_data_studio.exception.TokenInvalidException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.expiration}")
  private Long accessTokenExpiration;

  @Value("${jwt.refresh-expiration}")
  private Long refreshTokenExpiration;

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public String extractUsername(String token) {
    try {
      return extractClaim(token, Claims::getSubject);
    } catch (ExpiredJwtException ex) {
      throw new TokenExpiredException("Token has expired");
    } catch (Exception ex) {
      throw new TokenInvalidException("Invalid token");
    }
  }

  public Date extractExpiration(String token) {
    try {
      return extractClaim(token, Claims::getExpiration);
    } catch (ExpiredJwtException ex) {
      throw new TokenExpiredException("Token has expired");
    } catch (Exception ex) {
      throw new TokenInvalidException("Invalid token");
    }
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    try {
      final Claims claims = extractAllClaims(token);
      return claimsResolver.apply(claims);
    } catch (ExpiredJwtException ex) {
      throw new TokenExpiredException("Token has expired");
    } catch (Exception ex) {
      throw new TokenInvalidException("Invalid token");
    }
  }

  private Claims extractAllClaims(String token) {
    try {
      return Jwts.parser().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    } catch (ExpiredJwtException ex) {
      throw new TokenExpiredException("Token has expired");
    } catch (Exception ex) {
      throw new TokenInvalidException("Invalid token");
    }
  }

  public String generateToken(UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("authorities", userDetails.getAuthorities());
    return createToken(claims, userDetails.getUsername(), accessTokenExpiration);
  }

  public String generateRefreshToken(UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    return createToken(claims, userDetails.getUsername(), refreshTokenExpiration);
  }

  private String createToken(Map<String, Object> claims, String subject, Long expiration) {
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(subject)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(getSigningKey())
        .compact();
  }

  public Boolean isTokenValid(String token) {
    try {
      return !isTokenExpired(token);
    } catch (TokenExpiredException | TokenInvalidException ex) {
      return false;
    }
  }

  private Boolean isTokenExpired(String token) {
    try {
      return extractExpiration(token).before(new Date());
    } catch (TokenExpiredException ex) {
      return true;
    }
  }

  public Long getAccessTokenExpiration() {
    return accessTokenExpiration / 1000;
  }
}
