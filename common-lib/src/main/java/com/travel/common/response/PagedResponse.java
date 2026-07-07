package com.travel.common.response;

import java.util.List;

/**
 * Paginated response wrapper for list endpoints.
 *
 * Usage:
 *   return PagedResponse.of(page.getContent(), page.getNumber(),
 *                           page.getSize(), page.getTotalElements());
 */
public final class PagedResponse<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean first;
    private final boolean last;

    private PagedResponse(List<T> content, int page, int size, long totalElements) {
        this.content       = content;
        this.page          = page;
        this.size          = size;
        this.totalElements = totalElements;
        this.totalPages    = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        this.first         = page == 0;
        this.last          = page >= totalPages - 1;
    }

    public static <T> PagedResponse<T> of(List<T> content, int page,
                                          int size, long totalElements) {
        return new PagedResponse<>(content, page, size, totalElements);
    }

    public List<T> getContent()       { return content; }
    public int getPage()              { return page; }
    public int getSize()              { return size; }
    public long getTotalElements()    { return totalElements; }
    public int getTotalPages()        { return totalPages; }
    public boolean isFirst()          { return first; }
    public boolean isLast()           { return last; }
}
