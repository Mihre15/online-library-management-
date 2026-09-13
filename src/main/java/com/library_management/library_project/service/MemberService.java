package com.library_management.library_project.service;

import com.library_management.library_project.entity.Member;
import com.library_management.library_project.entity.MembershipStatus;
import com.library_management.library_project.exception.NotFoundException;
import com.library_management.library_project.repository.MemberRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final Clock clock;

    public MemberService(MemberRepository memberRepository, Clock clock) {
        this.memberRepository = memberRepository;
        this.clock = clock;
    }

    @Transactional
    public Member register(String fullName, String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("A member with email " + email + " already exists");
        }
        Member member = Member.builder()
                .fullName(fullName)
                .email(email)
                .status(MembershipStatus.ACTIVE)
                .registeredAt(LocalDate.now(clock))
                .outstandingFines(java.math.BigDecimal.ZERO)
                .build();
        return memberRepository.save(member);
    }

    public Member getById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Member not found: " + id));
    }

    public List<Member> listAll() {
        return memberRepository.findAll();
    }

    @Transactional
    public Member suspend(Long id) {
        Member member = getById(id);
        member.setStatus(MembershipStatus.SUSPENDED);
        return memberRepository.save(member);
    }

    @Transactional
    public Member reactivate(Long id) {
        Member member = getById(id);
        member.setStatus(MembershipStatus.ACTIVE);
        return memberRepository.save(member);
    }
}
