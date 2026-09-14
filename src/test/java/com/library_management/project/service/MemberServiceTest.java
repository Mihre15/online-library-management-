package com.library_management.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.library_management.project.entity.Member;
import com.library_management.project.entity.MembershipStatus;
import com.library_management.project.exception.NotFoundException;
import com.library_management.project.repository.MemberRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 6, 15);

    @Mock private MemberRepository memberRepository;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(TODAY.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
        memberService = new MemberService(memberRepository, fixedClock);
        lenient().when(memberRepository.save(any(Member.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void register_newEmail_savesActiveMember() {
        when(memberRepository.existsByEmail("ada@example.com")).thenReturn(false);

        Member result = memberService.register("Ada Lovelace", "ada@example.com");

        assertThat(result.getStatus()).isEqualTo(MembershipStatus.ACTIVE);
        assertThat(result.getRegisteredAt()).isEqualTo(TODAY);
    }

    @Test
    void register_duplicateEmail_throws() {
        when(memberRepository.existsByEmail("ada@example.com")).thenReturn(true);

        assertThatThrownBy(() -> memberService.register("Ada Lovelace", "ada@example.com"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getById_missing_throwsNotFound() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.getById(99L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void suspend_setsStatusSuspended() {
        Member member = Member.builder()
                .id(1L)
                .fullName("Ada")
                .email("ada@example.com")
                .status(MembershipStatus.ACTIVE)
                .registeredAt(TODAY)
                .outstandingFines(java.math.BigDecimal.ZERO)
                .build();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        Member result = memberService.suspend(1L);

        assertThat(result.getStatus()).isEqualTo(MembershipStatus.SUSPENDED);
    }

    @Test
    void reactivate_setsStatusActive() {
        Member member = Member.builder()
                .id(1L)
                .fullName("Ada")
                .email("ada@example.com")
                .status(MembershipStatus.SUSPENDED)
                .registeredAt(TODAY)
                .outstandingFines(java.math.BigDecimal.ZERO)
                .build();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        Member result = memberService.reactivate(1L);

        assertThat(result.getStatus()).isEqualTo(MembershipStatus.ACTIVE);
    }
}
