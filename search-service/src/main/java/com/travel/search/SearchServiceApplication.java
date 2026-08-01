package com.travel.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Search Service.
 * CQRS read-side. Tier: 2 (final inventory + search service).
 *
 * No relational database, no Flyway migration — Elasticsearch is the
 * only datastore, initialized on startup by ElasticsearchIndexInitializer.
 *
 * Boot order: discovery-server → config-server → search-service
 * Depends on: Elasticsearch, Kafka
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
public class SearchServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SearchServiceApplication.class, args);
    }
}
