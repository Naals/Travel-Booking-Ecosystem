package com.travel.search.domain;

import com.travel.search.domain.model.ListingType;
import com.travel.search.domain.model.SortOption;
import com.travel.search.domain.valueobject.GeoCoordinates;
import com.travel.search.domain.valueobject.PriceRange;
import com.travel.search.domain.valueobject.SearchCriteria;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SearchCriteria")
class SearchCriteriaTest {

    @Nested
    @DisplayName("Defaults")
    class Defaults {

        @Test @DisplayName("builds with sensible defaults")
        void defaults() {
            SearchCriteria criteria = SearchCriteria.builder().build();
            assertThat(criteria.getSortBy()).isEqualTo(SortOption.RELEVANCE);
            assertThat(criteria.isOnlyAvailable()).isTrue();
            assertThat(criteria.getPage()).isZero();
            assertThat(criteria.getSize()).isEqualTo(20);
            assertThat(criteria.getListingType()).isNull();
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test @DisplayName("rejects negative page")
        void negativePage() {
            assertThatThrownBy(() -> SearchCriteria.builder().page(-1).build())
                .isInstanceOf(com.travel.common.exception.DomainException.class);
        }

        @Test @DisplayName("rejects size over 100")
        void sizeTooLarge() {
            assertThatThrownBy(() -> SearchCriteria.builder().size(101).build())
                .isInstanceOf(com.travel.common.exception.DomainException.class);
        }

        @Test @DisplayName("rejects size of zero")
        void sizeZero() {
            assertThatThrownBy(() -> SearchCriteria.builder().size(0).build())
                .isInstanceOf(com.travel.common.exception.DomainException.class);
        }

        @Test @DisplayName("rejects 'near' coordinates without radiusKm")
        void nearWithoutRadius() {
            assertThatThrownBy(() -> SearchCriteria.builder()
                .near(GeoCoordinates.of(41.0, 29.0), null)
                .build())
                .isInstanceOf(com.travel.common.exception.DomainException.class)
                .hasMessageContaining("radiusKm is required");
        }

        @Test @DisplayName("rejects negative radiusKm")
        void negativeRadius() {
            assertThatThrownBy(() -> SearchCriteria.builder()
                .near(GeoCoordinates.of(41.0, 29.0), -5.0)
                .build())
                .isInstanceOf(com.travel.common.exception.DomainException.class);
        }

        @Test @DisplayName("accepts valid geo search parameters")
        void validGeoSearch() {
            assertThatCode(() -> SearchCriteria.builder()
                .near(GeoCoordinates.of(41.0, 29.0), 10.0)
                .build())
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("PriceRange")
    class PriceRangeTests {

        @Test @DisplayName("rejects min greater than max")
        void minGreaterThanMax() {
            assertThatThrownBy(() -> PriceRange.of(
                new BigDecimal("500"), new BigDecimal("100")))
                .isInstanceOf(com.travel.common.exception.DomainException.class);
        }

        @Test @DisplayName("rejects negative minimum")
        void negativeMin() {
            assertThatThrownBy(() -> PriceRange.of(
                new BigDecimal("-10"), new BigDecimal("100")))
                .isInstanceOf(com.travel.common.exception.DomainException.class);
        }

        @Test @DisplayName("accepts open-ended range (max only)")
        void openEndedMax() {
            assertThatCode(() -> PriceRange.of(null, new BigDecimal("500")))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Builder full construction")
    class BuilderConstruction {

        @Test @DisplayName("builds with listing type filter")
        void withListingType() {
            SearchCriteria criteria = SearchCriteria.builder()
                .listingType(ListingType.HOTEL)
                .keyword("beach")
                .city("Antalya")
                .sortBy(SortOption.PRICE_ASC)
                .build();

            assertThat(criteria.getListingType()).isEqualTo(ListingType.HOTEL);
            assertThat(criteria.getKeyword()).isEqualTo("beach");
            assertThat(criteria.getSortBy()).isEqualTo(SortOption.PRICE_ASC);
        }
    }
}
