package com.jumani.rutaseg.util;

import com.jumani.rutaseg.dto.result.PaginatedResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.jumani.rutaseg.TestDataGen.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class PaginationUtilTest {


    @Test
    void get_FirstPage() {
        final List<Long> elements = List.of(randomId(), randomId());

        final PaginationUtil.Fetcher<Long> fetcher = mock(PaginationUtil.Fetcher.class);

        when(fetcher.fetch(0, 10)).thenReturn(elements);

        final PaginatedResult<Long> expectedResult = new PaginatedResult<>(100, 10,
                10, 1, elements);

        final PaginatedResult<Long> actualResult = PaginationUtil.get(100, 10, 1, fetcher);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    void get_LastPage() {
        final List<Long> elements = List.of(randomId(), randomId());

        final PaginationUtil.Fetcher<Long> fetcher = mock(PaginationUtil.Fetcher.class);

        when(fetcher.fetch(90, 10)).thenReturn(elements);

        final PaginatedResult<Long> expectedResult = new PaginatedResult<>(100, 10,
                10, 10, elements);

        final PaginatedResult<Long> actualResult = PaginationUtil.get(100, 10, 10, fetcher);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    void get_MiddlePage() {
        final List<Long> elements = List.of(randomId(), randomId());

        final PaginationUtil.Fetcher<Long> fetcher = mock(PaginationUtil.Fetcher.class);

        when(fetcher.fetch(30, 10)).thenReturn(elements);

        final PaginatedResult<Long> expectedResult = new PaginatedResult<>(100, 10,
                10, 4, elements);

        final PaginatedResult<Long> actualResult = PaginationUtil.get(100, 10, 4, fetcher);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    void get_OddTotalResults_ShouldRoundToOneExtraPage() {
        final List<Long> elements = List.of(randomId());

        final PaginationUtil.Fetcher<Long> fetcher = mock(PaginationUtil.Fetcher.class);

        when(fetcher.fetch(100, 10)).thenReturn(elements);

        final PaginatedResult<Long> expectedResult = new PaginatedResult<>(101, 11,
                10, 11, elements);

        final PaginatedResult<Long> actualResult = PaginationUtil.get(101, 10, 11, fetcher);

        assertEquals(expectedResult, actualResult);
    }
}