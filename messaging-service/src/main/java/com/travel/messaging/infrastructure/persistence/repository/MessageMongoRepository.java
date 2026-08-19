package com.travel.messaging.infrastructure.persistence.repository;

import com.travel.messaging.infrastructure.persistence.document.MessageDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageMongoRepository extends MongoRepository<MessageDocument, String> {
    Page<MessageDocument> findByConversationId(String conversationId, Pageable pageable);
    long                    countByConversationId(String conversationId);
}
