package com.jumani.rutaseg.dto.result;

import java.util.List;
import java.util.function.Function;

public record PaginatedResult<T>(long totalElements, int totalPages, int pageSize, int page, List<T> elements) {
    public <E> PaginatedResult<E> map(Function<T, E> mapper) {
        final List<E> mappedElements = this.elements.stream().map(mapper).toList();
        return new PaginatedResult<>(totalElements, totalPages, pageSize, page, mappedElements);
    }
}
