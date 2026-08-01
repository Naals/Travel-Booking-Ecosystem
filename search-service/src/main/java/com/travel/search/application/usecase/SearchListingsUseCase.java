package com.travel.search.application.usecase;

import com.travel.search.application.dto.request.SearchRequest;
import com.travel.search.application.dto.response.SearchResponse;
import com.travel.search.domain.model.*;
import com.travel.search.domain.repository.SearchRepository;
import com.travel.search.domain.valueobject.GeoCoordinates;
import com.travel.search.domain.valueobject.PriceRange;
import com.travel.search.domain.valueobject.SearchCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchListingsUseCase {

    private final SearchRepository repository;

    public SearchResponse execute(SearchRequest request) {
        SearchCriteria.Builder builder = SearchCriteria.builder()
            .keyword(request.keyword())
            .city(request.city())
            .onlyAvailable(request.onlyAvailable())
            .page(request.page())
            .size(request.size());

        if (request.type() != null) {
            builder.listingType(ListingType.valueOf(request.type().toUpperCase()));
        }
        if (request.priceMin() != null || request.priceMax() != null) {
            builder.priceRange(PriceRange.of(request.priceMin(), request.priceMax()));
        }
        if (request.minRating() != null) {
            builder.minRating(request.minRating());
        }
        if (request.sortBy() != null) {
            builder.sortBy(SortOption.valueOf(request.sortBy().toUpperCase()));
        }
        if (request.lat() != null && request.lng() != null && request.radiusKm() != null) {
            builder.near(GeoCoordinates.of(request.lat(), request.lng()), request.radiusKm());
        }

        SearchResultPage<SearchDocument> results = repository.search(builder.build());
        return SearchResponse.from(results);
    }
}
