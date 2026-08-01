package com.travel.search.infrastructure.web.controller;

import com.travel.common.response.ApiResponse;
import com.travel.search.application.dto.request.SearchRequest;
import com.travel.search.application.dto.response.SearchResponse;
import com.travel.search.application.usecase.SearchListingsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "Federated search across properties, hotels, flights, and vehicles")
public class SearchController {

    private final SearchListingsUseCase searchUseCase;

    @GetMapping
    @Operation(summary = "Search listings with keyword, filters, geo-distance, and sorting")
    public ResponseEntity<ApiResponse<SearchResponse>> search(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String type,
        @RequestParam(required = false) String city,
        @RequestParam(required = false) BigDecimal priceMin,
        @RequestParam(required = false) BigDecimal priceMax,
        @RequestParam(required = false) Double minRating,
        @RequestParam(defaultValue = "true") boolean onlyAvailable,
        @RequestParam(required = false) Double lat,
        @RequestParam(required = false) Double lng,
        @RequestParam(required = false) Double radiusKm,
        @RequestParam(required = false) String sortBy,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {

        SearchRequest request = new SearchRequest(
            keyword, type, city, priceMin, priceMax, minRating,
            onlyAvailable, lat, lng, radiusKm, sortBy, page, size);

        return ResponseEntity.ok(ApiResponse.ok(searchUseCase.execute(request)));
    }
}
