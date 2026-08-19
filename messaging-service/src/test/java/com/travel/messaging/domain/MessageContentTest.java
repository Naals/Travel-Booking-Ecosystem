package com.travel.messaging.domain;

import com.travel.messaging.domain.valueobject.MessageContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MessageContent value object")
class MessageContentTest {

    @Test @DisplayName("rejects blank content")
    void rejectsBlank() {
        assertThatThrownBy(() -> MessageContent.of("   "))
            .isInstanceOf(com.travel.common.exception.DomainException.class);
    }

    @Test @DisplayName("rejects content over 2000 characters")
    void rejectsTooLong() {
        assertThatThrownBy(() -> MessageContent.of("x".repeat(2001)))
            .isInstanceOf(com.travel.common.exception.DomainException.class);
    }

    @Test @DisplayName("preview returns full text when under the preview length")
    void previewShortText() {
        MessageContent content = MessageContent.of("Short message");
        assertThat(content.preview()).isEqualTo("Short message");
    }

    @Test @DisplayName("preview truncates and appends ellipsis for long text")
    void previewLongText() {
        MessageContent content = MessageContent.of("x".repeat(150));
        assertThat(content.preview()).hasSize(103); // 100 chars + "..."
        assertThat(content.preview()).endsWith("...");
    }
}
