package com.travel.search.domain.valueobject;

import com.travel.shared.domain.ValueObject;
import com.travel.common.exception.DomainException;
import com.travel.search.domain.model.ListingType;
import com.travel.search.domain.model.SortOption;

/**
 * Encapsulates a fully-validated search request.
 * Built once at the application layer, passed unchanged into the
 * infrastructure adapter — the adapter never re-validates.
 */
public final class SearchCriteria implements ValueObject {

    private static final int MAX_PAGE_SIZE = 100;

    private final String         keyword;
    private final ListingType    listingType;   // null = search all types
    private final String         city;
    private final PriceRange     priceRange;    // nullable
    private final Double         minRating;     // nullable
    private final boolean        onlyAvailable;
    private final GeoCoordinates near;           // nullable — enables geo-distance filter
    private final Double         radiusKm;       // required if 'near' is set
    private final SortOption     sortBy;
    private final int            page;
    private final int            size;

    private SearchCriteria(String keyword, ListingType listingType, String city,
                           PriceRange priceRange, Double minRating,
                           boolean onlyAvailable, GeoCoordinates near, Double radiusKm,
                           SortOption sortBy, int page, int size) {
        if (page < 0)
            throw new DomainException("Page must not be negative", "INVALID_SEARCH_CRITERIA");
        if (size < 1 || size > MAX_PAGE_SIZE)
            throw new DomainException(
                "Page size must be between 1 and " + MAX_PAGE_SIZE, "INVALID_SEARCH_CRITERIA");
        if (near != null && radiusKm == null)
            throw new DomainException(
                "radiusKm is required when 'near' coordinates are provided", "INVALID_SEARCH_CRITERIA");
        if (radiusKm != null && radiusKm <= 0)
            throw new DomainException("radiusKm must be positive", "INVALID_SEARCH_CRITERIA");

        this.keyword       = keyword;
        this.listingType   = listingType;
        this.city          = city;
        this.priceRange    = priceRange;
        this.minRating     = minRating;
        this.onlyAvailable = onlyAvailable;
        this.near          = near;
        this.radiusKm      = radiusKm;
        this.sortBy        = sortBy != null ? sortBy : SortOption.RELEVANCE;
        this.page          = page;
        this.size          = size;
    }

    public static Builder builder() { return new Builder(); }

    public String         getKeyword()       { return keyword; }
    public ListingType    getListingType()   { return listingType; }
    public String         getCity()          { return city; }
    public PriceRange     getPriceRange()    { return priceRange; }
    public Double         getMinRating()     { return minRating; }
    public boolean        isOnlyAvailable()  { return onlyAvailable; }
    public GeoCoordinates getNear()          { return near; }
    public Double         getRadiusKm()      { return radiusKm; }
    public SortOption     getSortBy()        { return sortBy; }
    public int            getPage()          { return page; }
    public int            getSize()          { return size; }

    public static final class Builder {
        private String         keyword;
        private ListingType    listingType;
        private String         city;
        private PriceRange     priceRange;
        private Double         minRating;
        private boolean        onlyAvailable = true;
        private GeoCoordinates near;
        private Double         radiusKm;
        private SortOption     sortBy = SortOption.RELEVANCE;
        private int            page = 0;
        private int            size = 20;

        public Builder keyword(String v)             { this.keyword = v; return this; }
        public Builder listingType(ListingType v)    { this.listingType = v; return this; }
        public Builder city(String v)                { this.city = v; return this; }
        public Builder priceRange(PriceRange v)       { this.priceRange = v; return this; }
        public Builder minRating(Double v)            { this.minRating = v; return this; }
        public Builder onlyAvailable(boolean v)       { this.onlyAvailable = v; return this; }
        public Builder near(GeoCoordinates v, Double radiusKm) { this.near = v; this.radiusKm = radiusKm; return this; }
        public Builder sortBy(SortOption v)           { this.sortBy = v; return this; }
        public Builder page(int v)                    { this.page = v; return this; }
        public Builder size(int v)                    { this.size = v; return this; }

        public SearchCriteria build() {
            return new SearchCriteria(keyword, listingType, city, priceRange,
                minRating, onlyAvailable, near, radiusKm, sortBy, page, size);
        }
    }
}
