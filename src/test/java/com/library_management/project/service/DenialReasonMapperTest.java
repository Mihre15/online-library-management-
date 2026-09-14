package com.library_management.project.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DenialReasonMapperTest {

    @Test
    void mapsSuspendedFirst() {
        assertThat(DenialReasonMapper.toSpecReason("Member is suspended; No copies of this book are available"))
                .isEqualTo(DenialReasonMapper.ACCOUNT_SUSPENDED);
    }

    @Test
    void mapsFine() {
        assertThat(DenialReasonMapper.toSpecReason("Member's outstanding fines exceed 10.00"))
                .isEqualTo(DenialReasonMapper.FINE_TOO_HIGH);
    }

    @Test
    void mapsLimit() {
        assertThat(DenialReasonMapper.toSpecReason("Member has reached the maximum of 5 active loans"))
                .isEqualTo(DenialReasonMapper.BORROW_LIMIT);
    }

    @Test
    void mapsNoCopies() {
        assertThat(DenialReasonMapper.toSpecReason("No copies of this book are available"))
                .isEqualTo(DenialReasonMapper.NO_COPIES);
    }
}
