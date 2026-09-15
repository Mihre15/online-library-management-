package com.library_management.library_project.service;

import com.library_management.library_project.dto.LoginResponse;
import com.library_management.library_project.entity.Member;
import com.library_management.library_project.entity.MembershipStatus;
import com.library_management.library_project.exception.AccountSuspendedException;
import com.library_management.library_project.exception.AuthenticationException;
import com.library_management.library_project.repository.MemberRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Date;

@Service
public class AuthService {

  private final MemberRepository memberRepository;
  private final Clock clock;
  private final String jwtSecret;
  private final long jwtExpirationMs;
  private final BCryptPasswordEncoder passwordEncoder;

  public AuthService(
      MemberRepository memberRepository,
      Clock clock,
      @Value("${app.jwt.secret}") String jwtSecret,
      @Value("${app.jwt.expiration-ms:86400000}") long jwtExpirationMs) {
    this.memberRepository = memberRepository;
    this.clock = clock;
    this.jwtSecret = jwtSecret;
    this.jwtExpirationMs = jwtExpirationMs;
    this.passwordEncoder = new BCryptPasswordEncoder();
  }

  @Transactional
  public LoginResponse register(String fullName, String email, String password) {
    if (memberRepository.existsByEmail(email)) {
      throw new IllegalArgumentException("Email already registered");
    }

    Member member = Member.builder()
        .fullName(fullName)
        .email(email)
        .passwordHash(passwordEncoder.encode(password))
        .status(MembershipStatus.ACTIVE)
        .registeredAt(LocalDate.now(clock))
        .outstandingFines(java.math.BigDecimal.ZERO)
        .build();

    member = memberRepository.save(member);
    String token = generateToken(member);
    return new LoginResponse(token, member.getId());
  }

  public LoginResponse login(String email, String password) {
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

    if (!passwordEncoder.matches(password, member.getPasswordHash())) {
      throw new AuthenticationException("Invalid email or password");
    }

    if (member.getStatus() == MembershipStatus.SUSPENDED) {
      throw new AccountSuspendedException("Account is suspended");
    }

    String token = generateToken(member);
    return new LoginResponse(token, member.getId());
  }

  private String generateToken(Member member) {
    SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    return Jwts.builder()
        .subject(String.valueOf(member.getId()))
        .claim("email", member.getEmail())
        .claim("role", member.getRole().toString())
        .issuedAt(new Date(clock.millis()))
        .expiration(new Date(clock.millis() + jwtExpirationMs))
        .signWith(key)
        .compact();
  }
}
