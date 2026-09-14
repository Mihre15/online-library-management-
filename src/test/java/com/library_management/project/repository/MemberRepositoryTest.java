package com.library_management.project.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.library_management.project.entity.Member;
import com.library_management.project.entity.MembershipStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class MemberRepositoryTest {

    @Autowired private MemberRepository memberRepository;

    private Member sampleMember() {
        return Member.builder()
                .fullName("Ada Lovelace")
                .email("ada@example.com")
                .status(MembershipStatus.ACTIVE)
                .registeredAt(LocalDate.now())
                .outstandingFines(BigDecimal.ZERO)
                .build();
    }

    @Test
    void findByEmail_returnsSavedMember() {
        memberRepository.save(sampleMember());

        assertThat(memberRepository.findByEmail("ada@example.com")).isPresent();
    }

    @Test
    void existsByEmail_falseForUnknownEmail() {
        assertThat(memberRepository.existsByEmail("nobody@example.com")).isFalse();
    }

    @Test
    void duplicateEmail_violatesUniqueConstraint() {
        memberRepository.saveAndFlush(sampleMember());

        assertThat(memberRepository.existsByEmail("ada@example.com")).isTrue();
        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> memberRepository.saveAndFlush(sampleMember()))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }
}
