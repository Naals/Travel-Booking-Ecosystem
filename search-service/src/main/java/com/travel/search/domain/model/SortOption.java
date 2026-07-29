package com.travel.search.domain.model;

public enum SortOption {
    RELEVANCE,     // default — Elasticsearch _score
    PRICE_ASC,
    PRICE_DESC,
    RATING_DESC,
    NEWEST
}
