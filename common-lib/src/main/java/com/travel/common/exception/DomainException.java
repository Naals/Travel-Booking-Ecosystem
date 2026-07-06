package com.travel.common.exception;

/**
 * Base exception for all domain-level errors in the travel platform.
 *
 * Always carries an errorCode in addition to a message so the global
 * handler can map it to a meaningful API response without string matching.
 *
 * Services should never throw raw RuntimeException — always extend this.
 */
public class DomainException extends RuntimeException {

    private final String errorCode;

    public DomainException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public DomainException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
