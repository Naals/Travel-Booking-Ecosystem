package com.travel.search.infrastructure.elasticsearch.repository;

import com.travel.search.infrastructure.elasticsearch.document.ListingDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data Elasticsearch repository.
 * Used for simple CRUD (save, delete, findById); complex filtered
 * search queries go through ElasticsearchOperations in
 * SearchRepositoryAdapter instead, since derived query methods can't
 * express the combination of full-text + range + geo + sort needed here.
 */
@Repository
public interface ListingElasticsearchRepository
    extends ElasticsearchRepository<ListingDocument, String> {
}
