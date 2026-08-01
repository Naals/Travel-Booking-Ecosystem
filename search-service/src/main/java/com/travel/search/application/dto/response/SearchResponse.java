package com.travel.search.application.dto.response;

import com.travel.search.domain.model.SearchDocument;
import com.travel.search.domain.model.SearchResultPage;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record SearchResponse(
    List<ListingItem> results,
    long   totalHits,
    int    page,
    int    size,
    int    totalPages,
    long   tookMillis
) {
    public static SearchResponse from(SearchResultPage<SearchDocument> page) {
        List<ListingItem> items = page.content().stream()
            .map(ListingItem::from)
            .toList();

        return new SearchResponse(
            items, page.totalHits(), page.page(),
            page.size(), page.totalPages(), page.tookMillis());
    }

    public record ListingItem(
        String              id,
        String              listingType,
        String              title,
        String              city,
        String              country,
        BigDecimal          priceAmount,
        String              currency,
        Double              rating,
        boolean             available,
        String              imageUrl,
        Map<String, String> attributes
    ) {
        public static ListingItem from(SearchDocument d) {
            return new ListingItem(
                d.getId(), d.getListingType().name(), d.getTitle(),
                d.getCity(), d.getCountry(), d.getPriceAmount(),
                d.getCurrency(), d.getRating(), d.isAvailable(),
                d.getImageUrl(), d.getAttributes());
        }
    }
}
