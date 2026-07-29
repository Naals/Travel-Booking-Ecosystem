package com.travel.search.infrastructure.elasticsearch.config;

import com.travel.search.infrastructure.elasticsearch.document.ListingDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;

/**
 * Ensures the "listings" index exists with the correct mapping on startup.
 * Spring Data ES can auto-create indices lazily on first write, but doing
 * it explicitly here means the mapping is guaranteed correct before the
 * first Kafka consumer message arrives, and makes index lifecycle visible
 * in logs rather than happening silently.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticsearchIndexInitializer implements ApplicationRunner {

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public void run(ApplicationArguments args) {
        IndexOperations indexOps = elasticsearchOperations.indexOps(ListingDocument.class);

        if (!indexOps.exists()) {
            indexOps.create();
            indexOps.putMapping(indexOps.createMapping(ListingDocument.class));
            log.info("Created Elasticsearch index 'listings' with mapping for ListingDocument");
        } else {
            log.info("Elasticsearch index 'listings' already exists — skipping creation");
        }
    }
}
