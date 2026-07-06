package com.travel.common.exception;

/**
 * Thrown when a domain invariant or business rule is violated.
 * Maps to HTTP 422 in GlobalExceptionHandler.
 *
 * Usage:
 *   throw new BusinessRuleViolationException("Email already registered",
 *                                            "EMAIL_ALREADY_EXISTS");
 *   throw new BusinessRuleViolationException("Cannot cancel a completed booking",
 *                                            "INVALID_STATUS_TRANSITION");
 */
public class BusinessRuleViolationException extends DomainException {

    public BusinessRuleViolationException(String message) {
        super(message, "BUSINESS_RULE_VIOLATION");
    }

    public BusinessRuleViolationException(String message, String errorCode) {
        super(message, errorCode);
    }
}
