package com.jumani.rutaseg.util;

import com.jumani.rutaseg.dto.result.PaginatedResult;

import java.util.List;

public final class PaginationUtil {

    private PaginationUtil() {

    }

    public static <T> PaginatedResult<T> get(long totalElements,
                                             int pageSize,
                                             int page,
                                             Fetcher<T> fetcher) {

        final int totalPages = resolveTotalPages(totalElements, pageSize);
        final int offset = resolveOffset(pageSize, page);

        final List<T> elements = fetcher.fetch(offset, pageSize);

        return new PaginatedResult<>(totalElements, totalPages, pageSize, page, elements);
    }

    private static int resolveOffset(int pageSize, int page) {
        final int skippedPages = page - 1;
        return skippedPages * pageSize;
    }

    private static int resolveTotalPages(long totalElements, int pageSize) {
        return (int) Math.ceil((double) totalElements / pageSize);
    }


    @FunctionalInterface
    public interface Fetcher<T> {
        List<T> fetch(int offset, int limit);
    }
}
