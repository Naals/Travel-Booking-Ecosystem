package com.travel.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Standard API response envelope used by every service in the platform.
 *
 * All REST endpoints return this type. Clients can always inspect
 * "success" before reading "data" or "errorCode".
 *
 * Example success:
 * {
 *   "success": true,
 *   "message": "Created successfully",
 *   "data": { ... },
 *   "timestamp": "2024-01-15T10:30:00Z"
 * }
 *
 * Example error:
 * {
 *   "success": false,
 *   "message": "Email address is already registered",
 *   "errorCode": "EMAIL_ALREADY_EXISTS",
 *   "traceId": "abc123",
 *   "timestamp": "2024-01-15T10:30:00Z"
 * }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;
    private final String errorCode;
    private final String traceId;
    private final Instant timestamp;

    private ApiResponse(boolean success, String message, T data,
                        String errorCode, String traceId) {
        this.success   = success;
        this.message   = message;
        this.data      = data;
        this.errorCode = errorCode;
        this.traceId   = traceId;
        this.timestamp = Instant.now();
    }

    // ── Success factories ────────────────────────────────────────────────────

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "Success", data, null, null);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, null, null);
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(true, "Created successfully", data, null, null);
    }

    // ── Error factories ──────────────────────────────────────────────────────

    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return new ApiResponse<>(false, message, null, errorCode, null);
    }

    public static <T> ApiResponse<T> error(String message, String errorCode, String traceId) {
        return new ApiResponse<>(false, message, null, errorCode, traceId);
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public boolean isSuccess()    { return success; }
    public String getMessage()    { return message; }
    public T getData()            { return data; }
    public String getErrorCode()  { return errorCode; }
    public String getTraceId()    { return traceId; }
    public Instant getTimestamp() { return timestamp; }
}
