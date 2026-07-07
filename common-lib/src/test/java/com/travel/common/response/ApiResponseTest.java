package com.travel.common.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ApiResponse")
class ApiResponseTest {

    @Nested
    @DisplayName("Success responses")
    class SuccessResponses {

        @Test
        @DisplayName("ok(data) sets success=true and data")
        void ok_withData() {
            var response = ApiResponse.ok("hello");
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData()).isEqualTo("hello");
            assertThat(response.getErrorCode()).isNull();
            assertThat(response.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("ok(message, data) carries custom message")
        void ok_withMessage() {
            var response = ApiResponse.ok("Done", 42);
            assertThat(response.getMessage()).isEqualTo("Done");
            assertThat(response.getData()).isEqualTo(42);
        }

        @Test
        @DisplayName("created(data) sets Created message")
        void created() {
            var response = ApiResponse.created("resource");
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo("Created successfully");
        }
    }

    @Nested
    @DisplayName("Error responses")
    class ErrorResponses {

        @Test
        @DisplayName("error(message, code) sets success=false and errorCode")
        void error_basic() {
            var response = ApiResponse.error("Not found", "RESOURCE_NOT_FOUND");
            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
            assertThat(response.getData()).isNull();
        }

        @Test
        @DisplayName("error with traceId carries traceId")
        void error_withTraceId() {
            var response = ApiResponse.error("Oops", "INTERNAL_ERROR", "trace-abc");
            assertThat(response.getTraceId()).isEqualTo("trace-abc");
        }
    }
}
