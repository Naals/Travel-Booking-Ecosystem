package com.travel.booking.infrastructure.messaging.config;

import com.travel.common.event.KafkaTopics;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * First generic Kafka retry + Dead Letter Queue configuration in this
 * platform — see ADR-016. Every consumer since Day 6 has relied on
 * "don't acknowledge on failure" alone, with no explicit backoff and
 * no DLQ; that gap is exactly what let the Day 7 saga bug go unnoticed
 * until an integration test actually exercised it — an unacknowledged
 * record under manual ack mode isn't automatically retried on the
 * next poll; it needs a container-level error handler to seek back
 * and redeliver it, which is what this class adds.
 *
 * FixedBackOff(1000, 3): retry the failed record up to 3 times, 1
 * second apart, before giving up. This budget is sized for
 * SagaOutOfOrderException specifically — the missing precondition
 * event is expected to land within that window under normal
 * operation. A genuinely stuck saga (its precondition event lost, or
 * never coming) is exactly the case the DLQ exists to catch for
 * manual investigation, rather than blocking this partition forever.
 *
 * DLQ topics (e.g. inventory.reservation-confirmed.dlq) are expected
 * to auto-create on first publish for local development. Explicit
 * provisioning (fixed partition counts, disabling auto-create) is
 * deferred to the Kubernetes/production infrastructure hardening pass
 * already tracked as remaining work.
 *
 * Scoped to booking-service only today. Rolling the same pattern out
 * to the other 20 services' consumers is real, valuable, explicitly
 * tracked future work — not attempted in one sweep here.
 */
@Slf4j
@Configuration
public class KafkaErrorHandlingConfig {

    @Bean
    public DefaultErrorHandler bookingSagaErrorHandler(KafkaTemplate<String, String> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
            (record, ex) -> new TopicPartition(record.topic() + KafkaTopics.DLQ_SUFFIX, record.partition()));

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, new FixedBackOff(1_000L, 3));

        handler.setRetryListeners((record, ex, deliveryAttempt) ->
            log.warn("Saga event retry {} for topic={} key={}: {}",
                deliveryAttempt, record.topic(), record.key(), ex.getMessage()));

        return handler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
        ConsumerFactory<String, String> consumerFactory,
        DefaultErrorHandler bookingSagaErrorHandler) {

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(bookingSagaErrorHandler);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }
}
