package com.jobbed.common.web;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Einheitlicher Wrapper für paginierte Antworten (siehe docs/api-design.md).
 * Kapselt die Spring-{@link Page}, ohne interne Repräsentationen zu leaken.
 */
public record ApiPage<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        List<SortOrder> sort
) {
    public record SortOrder(String property, String direction) {
    }

    public static <T> ApiPage<T> from(Page<T> page) {
        List<SortOrder> sortOrders = page.getSort().stream()
                .map(o -> new SortOrder(o.getProperty(), o.getDirection().name()))
                .toList();
        return new ApiPage<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                sortOrders
        );
    }

    /** Wandelt Inhalte eines Page über eine Mapping-Funktion um. */
    public static <S, T> ApiPage<T> from(Page<S> page, Function<S, T> mapper) {
        return from(page.map(mapper));
    }

    public ApiPage {
        if (sort == null) {
            sort = List.of();
        }
    }
}
