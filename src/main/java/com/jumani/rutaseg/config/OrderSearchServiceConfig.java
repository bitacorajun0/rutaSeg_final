package com.jumani.rutaseg.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jumani.rutaseg.domain.Order;
import com.jumani.rutaseg.dto.result.PaginatedResult;
import com.jumani.rutaseg.repository.OrderRepository;
import com.jumani.rutaseg.service.order.OrderSearchService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class OrderSearchServiceConfig {

    @Bean
    public OrderSearchService orderSearchService(OrderRepository orderRepo) {
        final Cache<OrderSearchService.SearchParamsKey, PaginatedResult<Order>> cache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .build();

        return new OrderSearchService(cache.asMap(), orderRepo);
    }

}
