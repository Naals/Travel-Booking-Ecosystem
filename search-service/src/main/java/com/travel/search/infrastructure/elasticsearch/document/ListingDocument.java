package com.travel.search.infrastructure.elasticsearch.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import lombok.*;

import java.time.Instant;
import java.util.Map;

/**
 * Elasticsearch document for the unified "listings" index.
 * Mirrors SearchDocument but uses Spring Data ES field annotations —
 * kept as a separate class so the domain layer has zero ES dependency.
 */
@Document(indexName = "listings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ListingDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String listingType;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Keyword)
    private String city;

    @Field(type = FieldType.Keyword)
    private String country;

    @GeoPointField
    private org.springframework.data.elasticsearch.core.geo.GeoPoint location;

    @Field(type = FieldType.Double)
    private Double priceAmount;

    @Field(type = FieldType.Keyword)
    private String currency;

    @Field(type = FieldType.Double)
    private Double rating;

    @Field(type = FieldType.Boolean)
    private boolean available;

    @Field(type = FieldType.Keyword, index = false)
    private String imageUrl;

    @Field(type = FieldType.Object)
    private Map<String, String> attributes;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant updatedAt;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant lastAvailabilityEventAt;
}
