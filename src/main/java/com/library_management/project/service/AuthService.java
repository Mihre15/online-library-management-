package com.library_management.project.service;

import com.library_management.project.dto.LoginResponse;
import com.library_management.project.entity.Member;
import com.library_management.project.entity.MemberRole;
import com.library_management.project.exception.UnauthorizedException;
import com.library_management.project.repository.MemberRepository;
import com.library_management.project.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            MemberRepository memberRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(String email, String password) {
        Member member = memberRepository
                .findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        if (member.getPasswordHash() == null
                || !passwordEncoder.matches(password, member.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }
        MemberRole role = member.getRole() == null ? MemberRole.STUDENT : member.getRole();
        String token = jwtService.createToken(member.getId(), member.getEmail(), role);
        return new LoginResponse(token, member.getId());
    }
}
