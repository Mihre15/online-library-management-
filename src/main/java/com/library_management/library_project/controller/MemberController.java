package com.library_management.library_project.controller;

import com.library_management.library_project.dto.MemberResponse;
import com.library_management.library_project.entity.Member;
import com.library_management.library_project.service.MemberService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@CrossOrigin(origins = "http://localhost:3000")
@PreAuthorize("hasRole('ADMIN')")
public class MemberController {

  private final MemberService memberService;

  public MemberController(MemberService memberService) {
    this.memberService = memberService;
  }

  @GetMapping
  public List<MemberResponse> listMembers() {
    return memberService.listAll()
        .stream()
        .map(MemberResponse::from)
        .toList();
  }

  @GetMapping("/{id}")
  public MemberResponse getMember(@PathVariable Long id) {
    Member member = memberService.getById(id);
    return MemberResponse.from(member);
  }

  @PutMapping("/{id}/suspend")
  public MemberResponse suspend(@PathVariable Long id) {
    Member member = memberService.suspend(id);
    return MemberResponse.from(member);
  }

  @PutMapping("/{id}/reactivate")
  public MemberResponse reactivate(@PathVariable Long id) {
    Member member = memberService.reactivate(id);
    return MemberResponse.from(member);
  }
}
