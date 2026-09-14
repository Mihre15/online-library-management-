package com.library_management.project.controller;

import com.library_management.project.dto.MemberResponse;
import com.library_management.project.dto.RegisterMemberRequest;
import com.library_management.project.service.MemberService;
import com.library_management.project.security.RequestAuth;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<MemberResponse> register(
            @Valid @RequestBody RegisterMemberRequest request, HttpServletRequest httpRequest) {
        RequestAuth.requireAdmin(httpRequest);
        var member = memberService.register(request.fullName(), request.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(MemberResponse.from(member));
    }

    @GetMapping("/{id}")
    public MemberResponse getById(@PathVariable Long id, HttpServletRequest httpRequest) {
        RequestAuth.requireAdmin(httpRequest);
        return MemberResponse.from(memberService.getById(id));
    }

    @GetMapping
    public List<MemberResponse> listAll(HttpServletRequest httpRequest) {
        RequestAuth.requireAdmin(httpRequest);
        return memberService.listAll().stream().map(MemberResponse::from).toList();
    }

    @PutMapping("/{id}/suspend")
    public MemberResponse suspend(@PathVariable Long id, HttpServletRequest httpRequest) {
        RequestAuth.requireAdmin(httpRequest);
        return MemberResponse.from(memberService.suspend(id));
    }

    @PutMapping("/{id}/reactivate")
    public MemberResponse reactivate(@PathVariable Long id, HttpServletRequest httpRequest) {
        RequestAuth.requireAdmin(httpRequest);
        return MemberResponse.from(memberService.reactivate(id));
    }
}
