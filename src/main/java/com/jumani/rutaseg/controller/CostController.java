package com.jumani.rutaseg.controller;

import com.jumani.rutaseg.domain.Cost;
import com.jumani.rutaseg.domain.Note;
import com.jumani.rutaseg.domain.Order;
import com.jumani.rutaseg.dto.request.CostRequest;
import com.jumani.rutaseg.dto.response.CostResponse;
import com.jumani.rutaseg.dto.response.SessionInfo;
import com.jumani.rutaseg.dto.result.PaginatedResult;
import com.jumani.rutaseg.exception.ForbiddenException;
import com.jumani.rutaseg.exception.NotFoundException;
import com.jumani.rutaseg.handler.Session;
import com.jumani.rutaseg.repository.OrderRepository;
import com.jumani.rutaseg.util.PaginationUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/orders/{orderId}/costs")
public class CostController {

    private final OrderRepository orderRepo;

    public CostController(OrderRepository orderRepo) {
        this.orderRepo = orderRepo;
    }

    @GetMapping
    public ResponseEntity<PaginatedResult<Cost>> search(@PathVariable("orderId") long orderId,
                                                        @RequestParam(value = "page", defaultValue = "1") int page,
                                                        @RequestParam(value = "page_size", defaultValue = "100") int pageSize,
                                                        @Session SessionInfo session) {

        final Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new NotFoundException(String.format("order with id [%s] not found", orderId)));

        if (!session.admin() && !Objects.equals(order.getClient().getUserId(), session.userId())) {
            throw new ForbiddenException();
        }

        final List<Cost> costs = order.getCosts();
        final PaginatedResult<Cost> result = PaginationUtil.get(costs.size(), pageSize, page,
                (offset, limit) -> costs.stream()
                        .sorted(Comparator.comparing(Cost::getCreatedAt))
                        .skip(offset)
                        .limit(limit)
                        .toList()
        );

        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<CostResponse> createCost(
            @PathVariable("orderId") long orderId,
            @RequestBody @Valid CostRequest costRequest,
            @Session SessionInfo session
    ) {
        if (!session.admin()) {
            throw new ForbiddenException();
        }

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new NotFoundException(String.format("order with id [%s] not found", orderId)));

        Cost newCost = new Cost(
                costRequest.getAmount(),
                costRequest.getDescription(),
                costRequest.getType(),
                session.userId()
        );

        order.updateCost(newCost);
        final Order updatedOrder = orderRepo.save(order);

        final Cost createdCost = updatedOrder.getCosts().stream().filter(newCost::equals).findFirst().orElseThrow();

        CostResponse costResponse = createCostResponse(createdCost);

        return ResponseEntity.status(HttpStatus.CREATED).body(costResponse);
    }

    private CostResponse createCostResponse(Cost cost) {
        return new CostResponse(
                cost.getId(),
                cost.getAmount(),
                cost.getDescription(),
                cost.getType(),
                cost.getCreatedAt()
        );
    }

    @PutMapping("/{costId}")
    public ResponseEntity<CostResponse> updateCost(
            @PathVariable("orderId") long orderId,
            @PathVariable("costId") long costId,
            @RequestBody @Valid CostRequest costRequest,
            @Session SessionInfo session
    ) {
        if (!session.admin()) {
            throw new ForbiddenException();
        }

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new NotFoundException(String.format("order with id [%s] not found", orderId)));

        Optional<Cost> costOptional = order.findCost(costId);
        if (costOptional.isEmpty()) {
            throw new NotFoundException(String.format("cost with id [%s] not found in order [%s]", costId, orderId));
        }

        Cost existingCost = costOptional.get();

        existingCost.update(
                costRequest.getAmount(),
                costRequest.getDescription(),
                costRequest.getType()
        );

        orderRepo.save(order);

        CostResponse costResponse = createCostResponse(existingCost);

        return ResponseEntity.ok(costResponse);
    }

    @DeleteMapping("/{costId}")
    public ResponseEntity<Void> deleteCost(
            @PathVariable("orderId") long orderId,
            @PathVariable("costId") long costId,
            @Session SessionInfo session
    ) {
        if (!session.admin()) {
            throw new ForbiddenException();
        }

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new NotFoundException(String.format("order with id [%s] not found", orderId)));

        Optional<Cost> costToRemoveOptional = order.removeCost(costId);

        if (costToRemoveOptional.isPresent()) {
            orderRepo.save(order);
        }

        return ResponseEntity.noContent().build();
    }
}