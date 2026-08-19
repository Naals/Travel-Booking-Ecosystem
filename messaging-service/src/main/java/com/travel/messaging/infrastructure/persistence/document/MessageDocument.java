package com.travel.messaging.infrastructure.persistence.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "messages")
@CompoundIndexes({
    @CompoundIndex(name = "conversation_sentAt_idx", def = "{'conversationId': 1, 'sentAt': -1}")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MessageDocument {

    @Id
    private String id;

    private String conversationId;
    private String senderId;
    private String recipientId;
    private String content; // null once the message has been deleted
    private String status;

    private Instant sentAt;
    private Instant deletedAt;
}
