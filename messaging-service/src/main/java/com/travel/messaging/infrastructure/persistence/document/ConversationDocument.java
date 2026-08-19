package com.travel.messaging.infrastructure.persistence.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Document(collection = "conversations")
@CompoundIndexes({
    @CompoundIndex(name = "participants_context_idx", def = "{'participantIds': 1, 'contextType': 1}")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConversationDocument {

    @Id
    private String id;

    @Indexed
    private List<String> participantIds;

    private String contextType;
    private String bookingId;
    private String status;

    private String  lastMessagePreview;
    private String  lastMessageSenderId;
    private Instant lastMessageAt;

    private Map<String, Instant> lastReadAt;

    private Instant createdAt;
    private Instant updatedAt;
}
