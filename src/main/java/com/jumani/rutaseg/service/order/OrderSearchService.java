package com.jumani.rutaseg.service.order;

import com.jumani.rutaseg.domain.Order;
import com.jumani.rutaseg.domain.OrderStatus;
import com.jumani.rutaseg.domain.Sort;
import com.jumani.rutaseg.dto.result.PaginatedResult;
import com.jumani.rutaseg.repository.OrderRepository;
import com.jumani.rutaseg.util.PaginationUtil;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class OrderSearchService {
    private final Map<SearchParamsKey, PaginatedResult<Order>> cache;

    private final OrderRepository orderRepo;

    public PaginatedResult<Order> search(String code, Boolean pema, Boolean transport, Boolean port,
                                         LocalDate creationDateFrom, LocalDate creationDateTo,
                                         LocalDate arrivalDateFrom, LocalDate arrivalDateTo,
                                         LocalTime arrivalTimeFrom, LocalTime arrivalTimeTo,
                                         Long clientId,
                                         OrderStatus status,
                                         String loadCode,
                                         String origin,
                                         String target,
                                         String consigneeCuit,
                                         String destinationCode,
                                         List<Sort> sorts,
                                         int pageSize,
                                         int page) {

        final SearchParamsKey key = new SearchParamsKey(code, pema, transport, port,
                creationDateFrom, creationDateTo, arrivalDateFrom, arrivalDateTo, arrivalTimeFrom, arrivalTimeTo,
                clientId, status, loadCode, origin, target, consigneeCuit, destinationCode, sorts, pageSize, page);

        if (!cache.containsKey(key)) {
            final PaginatedResult<Order> result = this.doSearch(code, pema, transport, port,
                    creationDateFrom, creationDateTo, arrivalDateFrom, arrivalDateTo, arrivalTimeFrom, arrivalTimeTo,
                    clientId, status, loadCode, origin, target, consigneeCuit, destinationCode, sorts, pageSize, page);

            cache.put(key, result);
            return result;
        }

        return cache.get(key);
    }

    private PaginatedResult<Order> doSearch(String code, Boolean pema, Boolean transport, Boolean port,
                                            LocalDate creationDateFrom, LocalDate creationDateTo,
                                            LocalDate arrivalDateFrom, LocalDate arrivalDateTo,
                                            LocalTime arrivalTimeFrom, LocalTime arrivalTimeTo,
                                            Long clientId,
                                            OrderStatus status,
                                            String loadCode,
                                            String origin,
                                            String target,
                                            String consigneeCuit,
                                            String destinationCode,
                                            List<Sort> sorts,
                                            int pageSize,
                                            int page) {
        final long totalElements = orderRepo.count(
                code,
                pema,
                transport,
                port,
                creationDateFrom,
                creationDateTo,
                arrivalDateFrom,
                arrivalDateTo,
                arrivalTimeFrom,
                arrivalTimeTo,
                clientId,
                status,
                loadCode,
                origin,
                target,
                consigneeCuit,
                destinationCode);

        return PaginationUtil.get(totalElements, pageSize, page, (offset, limit) ->
                orderRepo.search(
                        code,
                        pema,
                        transport,
                        port,
                        creationDateFrom,
                        creationDateTo,
                        arrivalDateFrom,
                        arrivalDateTo,
                        arrivalTimeFrom,
                        arrivalTimeTo,
                        clientId,
                        status,
                        loadCode,
                        origin,
                        target,
                        consigneeCuit,
                        destinationCode,
                        sorts,
                        offset,
                        limit
                )
        );
    }

    public record SearchParamsKey(
            String code, Boolean pema, Boolean transport, Boolean port,
            LocalDate creationDateFrom, LocalDate creationDateTo,
            LocalDate arrivalDateFrom, LocalDate arrivalDateTo,
            LocalTime arrivalTimeFrom, LocalTime arrivalTimeTo,
            Long clientId, OrderStatus status,
            String loadCode,
            String origin,
            String target,
            String consigneeCuit,
            String destinationCode,
            List<Sort> sorts,
            int pageSize, int page) {
    }
}