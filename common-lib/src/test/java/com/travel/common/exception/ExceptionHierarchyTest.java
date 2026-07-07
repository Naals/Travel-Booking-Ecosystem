package com.travel.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Exception hierarchy")
class ExceptionHierarchyTest {

    @Test
    @DisplayName("DomainException carries errorCode")
    void domainException_errorCode() {
        var ex = new DomainException("bad thing", "BAD_THING");
        assertThat(ex.getMessage()).isEqualTo("bad thing");
        assertThat(ex.getErrorCode()).isEqualTo("BAD_THING");
    }

    @Test
    @DisplayName("ResourceNotFoundException is a DomainException")
    void resourceNotFound_isDomainException() {
        var ex = new ResourceNotFoundException("Booking", "id-123");
        assertThat(ex).isInstanceOf(DomainException.class);
        assertThat(ex.getMessage()).contains("Booking").contains("id-123");
        assertThat(ex.getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
    }

    @Test
    @DisplayName("BusinessRuleViolationException uses default code when not provided")
    void businessRule_defaultCode() {
        var ex = new BusinessRuleViolationException("Cannot do that");
        assertThat(ex.getErrorCode()).isEqualTo("BUSINESS_RULE_VIOLATION");
    }

    @Test
    @DisplayName("BusinessRuleViolationException uses provided code")
    void businessRule_customCode() {
        var ex = new BusinessRuleViolationException("Email taken", "EMAIL_ALREADY_EXISTS");
        assertThat(ex.getErrorCode()).isEqualTo("EMAIL_ALREADY_EXISTS");
    }
}
