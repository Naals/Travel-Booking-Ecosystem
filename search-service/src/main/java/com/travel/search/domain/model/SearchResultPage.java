package com.travel.search.domain.model;

import java.util.List;

/**
 * Generic paginated search result.
 * tookMillis mirrors Elasticsearch's own "took" field — useful for
 * exposing query performance to API consumers and dashboards.
 */
public record SearchResultPage<T>(
    List<T> content,
    long    totalHits,
    int     page,
    int     size,
    long    tookMillis
) {
    public int totalPages() {
        return size == 0 ? 0 : (int) Math.ceil((double) totalHits / size);
    }

    public boolean isLast() {
        return page >= totalPages() - 1;
    }
}
