package com.travel.search.infrastructure.elasticsearch;

import com.travel.search.domain.model.ListingType;
import com.travel.search.domain.model.SearchDocument;
import com.travel.search.domain.model.SearchResultPage;
import com.travel.search.domain.model.SortOption;
import com.travel.search.domain.repository.SearchRepository;
import com.travel.search.domain.valueobject.GeoCoordinates;
import com.travel.search.domain.valueobject.PriceRange;
import com.travel.search.domain.valueobject.SearchCriteria;
import com.travel.search.infrastructure.elasticsearch.document.ListingDocument;
import com.travel.search.infrastructure.elasticsearch.repository.ListingElasticsearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Implements the SearchRepository domain port against Elasticsearch.
 *
 * Uses Spring Data ES's Criteria API rather than ElasticsearchRepository's
 * derived query methods, since the combination of full-text keyword match,
 * numeric range filters, geo-distance, availability toggle, and dynamic
 * sort cannot be expressed as a single derived method signature.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchRepositoryAdapter implements SearchRepository {

    private final ListingElasticsearchRepository jpaLikeRepository;
    private final ElasticsearchOperations        elasticsearchOperations;

    @Override
    public void index(SearchDocument doc) {
        ListingDocument entity = toEntity(doc);
        jpaLikeRepository.save(entity);
        log.debug("Indexed listing {} type={}", doc.getId(), doc.getListingType());
    }

    @Override
    public void touchAvailabilitySignal(String id, ListingType type) {
        jpaLikeRepository.findById(id).ifPresentOrElse(entity -> {
            entity.setLastAvailabilityEventAt(Instant.now());
            entity.setUpdatedAt(Instant.now());
            jpaLikeRepository.save(entity);
            log.debug("Touched availability signal for {} ({})", id, type);
        }, () -> log.warn(
            "Availability signal for unknown listing {} ({}) — not yet indexed?", id, type));
    }

    @Override
    public void markUnavailable(String id, ListingType type) {
        jpaLikeRepository.findById(id).ifPresentOrElse(entity -> {
            entity.setAvailable(false);
            entity.setUpdatedAt(Instant.now());
            jpaLikeRepository.save(entity);
            log.info("Marked listing {} ({}) unavailable", id, type);
        }, () -> log.warn(
            "Cannot mark unavailable — unknown listing {} ({})", id, type));
    }

    @Override
    public void delete(String id, ListingType type) {
        jpaLikeRepository.deleteById(id);
        log.info("Deleted listing {} ({}) from index", id, type);
    }

    @Override
    public SearchResultPage<SearchDocument> search(SearchCriteria criteria) {
        Criteria esCriteria = Criteria.where("id").exists(); // always-true base

        if (criteria.isOnlyAvailable()) {
            esCriteria = esCriteria.and(Criteria.where("available").is(true));
        }

        if (criteria.getListingType() != null) {
            esCriteria = esCriteria.and(
                Criteria.where("listingType").is(criteria.getListingType().name()));
        }

        if (StringUtils.hasText(criteria.getCity())) {
            esCriteria = esCriteria.and(Criteria.where("city").is(criteria.getCity()));
        }

        if (StringUtils.hasText(criteria.getKeyword())) {
            esCriteria = esCriteria.and(
                Criteria.where("title").contains(criteria.getKeyword())
                    .or(Criteria.where("description").contains(criteria.getKeyword())));
        }

        PriceRange range = criteria.getPriceRange();
        if (range != null) {
            Criteria priceCriteria = Criteria.where("priceAmount");
            if (range.getMin() != null && range.getMax() != null) {
                priceCriteria = priceCriteria.between(
                    range.getMin().doubleValue(), range.getMax().doubleValue());
            } else if (range.getMin() != null) {
                priceCriteria = priceCriteria.greaterThanEqual(range.getMin().doubleValue());
            } else if (range.getMax() != null) {
                priceCriteria = priceCriteria.lessThanEqual(range.getMax().doubleValue());
            }
            esCriteria = esCriteria.and(priceCriteria);
        }

        if (criteria.getMinRating() != null) {
            esCriteria = esCriteria.and(
                Criteria.where("rating").greaterThanEqual(criteria.getMinRating()));
        }

        GeoCoordinates near = criteria.getNear();
        if (near != null) {
            var geoPoint = new org.springframework.data.elasticsearch.core.geo.GeoPoint(
                near.getLatitude(), near.getLongitude());
            esCriteria = esCriteria.and(
                Criteria.where("location").within(geoPoint, criteria.getRadiusKm() + "km"));
        }

        CriteriaQuery query = new CriteriaQuery(esCriteria);
        query.setPageable(PageRequest.of(criteria.getPage(), criteria.getSize(), resolveSort(criteria.getSortBy())));

        SearchHits<ListingDocument> hits =
            elasticsearchOperations.search(query, ListingDocument.class);

        List<SearchDocument> content = hits.getSearchHits().stream()
            .map(hit -> toDomain(hit.getContent()))
            .toList();

        return new SearchResultPage<>(
            content,
            hits.getTotalHits(),
            criteria.getPage(),
            criteria.getSize(),
            hits.getExecutionDuration() != null
                ? hits.getExecutionDuration().toMillis() : 0L);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Sort resolveSort(SortOption sortBy) {
        return switch (sortBy) {
            case PRICE_ASC   -> Sort.by(Sort.Direction.ASC, "priceAmount");
            case PRICE_DESC  -> Sort.by(Sort.Direction.DESC, "priceAmount");
            case RATING_DESC -> Sort.by(Sort.Direction.DESC, "rating");
            case NEWEST      -> Sort.by(Sort.Direction.DESC, "createdAt");
            case RELEVANCE   -> Sort.unsorted(); // rely on ES _score
        };
    }

    private ListingDocument toEntity(SearchDocument d) {
        return ListingDocument.builder()
            .id(d.getId())
            .listingType(d.getListingType().name())
            .title(d.getTitle())
            .description(d.getDescription())
            .city(d.getCity())
            .country(d.getCountry())
            .location(d.getLocation() != null
                ? new org.springframework.data.elasticsearch.core.geo.GeoPoint(
                d.getLocation().getLatitude(), d.getLocation().getLongitude())
                : null)
            .priceAmount(d.getPriceAmount() != null ? d.getPriceAmount().doubleValue() : null)
            .currency(d.getCurrency())
            .rating(d.getRating())
            .available(d.isAvailable())
            .imageUrl(d.getImageUrl())
            .attributes(d.getAttributes())
            .createdAt(d.getCreatedAt())
            .updatedAt(d.getUpdatedAt())
            .lastAvailabilityEventAt(d.getLastAvailabilityEventAt())
            .build();
    }

    private SearchDocument toDomain(ListingDocument e) {
        GeoCoordinates location = e.getLocation() != null
            ? GeoCoordinates.of(e.getLocation().getLat(), e.getLocation().getLon())
            : null;

        return SearchDocument.builder()
            .id(e.getId())
            .listingType(ListingType.valueOf(e.getListingType()))
            .title(e.getTitle())
            .description(e.getDescription())
            .city(e.getCity())
            .country(e.getCountry())
            .location(location)
            .priceAmount(e.getPriceAmount() != null
                ? java.math.BigDecimal.valueOf(e.getPriceAmount()) : null)
            .currency(e.getCurrency())
            .rating(e.getRating())
            .available(e.isAvailable())
            .imageUrl(e.getImageUrl())
            .attributes(e.getAttributes() != null ? e.getAttributes() : Map.of())
            .createdAt(e.getCreatedAt())
            .updatedAt(e.getUpdatedAt())
            .lastAvailabilityEventAt(e.getLastAvailabilityEventAt())
            .build();
    }
}
