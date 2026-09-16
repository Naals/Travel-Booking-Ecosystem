package com.travel.testsupport;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Publishes raw JSON test messages to Kafka topics — used to simulate
 * events that would, in a real deployment, come from another service
 * (e.g. property-service's inventory.reservation-confirmed,
 * payment-service's payment.payment-completed) without needing that
 * service actually running. This is what lets a single service's
 * integration suite exercise its saga-consumer wiring without
 * standing up all 21 services in one test run. See
 * BookingSagaIntegrationTest and ADR-016.
 */
public final class KafkaTestProducer implements AutoCloseable {

    private final KafkaProducer<String, String> producer;

    public KafkaTestProducer(String bootstrapServers) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        this.producer = new KafkaProducer<>(props);
    }

    public void send(String topic, String key, String jsonPayload) {
        try {
            producer.send(new ProducerRecord<>(topic, key, jsonPayload)).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to publish test message to " + topic, e);
        }
    }

    @Override
    public void close() {
        producer.close();
    }
}
