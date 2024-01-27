package com.jumani.rutaseg.repository;

import com.jumani.rutaseg.domain.Order;
import com.jumani.rutaseg.domain.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;


@Repository
@RequiredArgsConstructor
@FieldNameConstants
public class OrderRepositoryImpl implements OrderRepositoryExtended {

    private final EntityManager entityManager;
    private final ContainerRepository containerRepo;
    private final FreeLoadRepository freeLoadRepo;


    public List<Order> search(
            @Nullable String codeLike,
            @Nullable Boolean pema,
            @Nullable Boolean transport,
            @Nullable Boolean port,
            @Nullable LocalDate creationDateFrom,
            @Nullable LocalDate creationDateTo,
            @Nullable LocalDate arrivalDateFrom,
            @Nullable LocalDate arrivalDateTo,
            @Nullable LocalTime arrivalTimeFrom,
            @Nullable LocalTime arrivalTimeTo,
            @Nullable Long clientId,
            @Nullable OrderStatus status,
            @Nullable String loadCode,
            @Nullable String origin,
            @Nullable String target,
            @Nullable String consigneeCuit,
            @Nullable String destinationCode,
            List<Sort> sorts,
            int offset,
            int limit
    ) {

        final CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Order> criteriaQuery = builder.createQuery(Order.class);
        final Root<Order> root = criteriaQuery.from(Order.class);

        root.join(Order.Fields.client, JoinType.INNER);

        criteriaQuery.select(root);

        final Predicate[] predicates = createPredicates(builder, root, codeLike, pema, transport, port,
                creationDateFrom, creationDateTo, arrivalDateFrom, arrivalDateTo, arrivalTimeFrom, arrivalTimeTo, clientId, status,
                loadCode, origin, target, consigneeCuit, destinationCode);
        criteriaQuery.where(predicates);

        final List<jakarta.persistence.criteria.Order> orders = this.resolveOrders(builder, root, sorts);
        criteriaQuery.orderBy(orders);

        return entityManager.createQuery(criteriaQuery)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    private List<jakarta.persistence.criteria.Order> resolveOrders(CriteriaBuilder builder,
                                                                   Root<Order> root,
                                                                   List<Sort> sorts) {

        final List<jakarta.persistence.criteria.Order> orders = new ArrayList<>();
        for (Sort sort : sorts) {
            switch (sort.field()) {
                case "arrival_date" -> {
                    if (sort.ascending()) {
                        orders.add(builder.asc(root.get(Order.Fields.arrivalDate)));
                    } else {
                        orders.add(builder.desc(root.get(Order.Fields.arrivalDate)));
                    }
                }
                case "load_code" -> {
                    final Path<Object> containerCode = root.join("containers", JoinType.LEFT).get("code");
                    final Path<Object> freeLoadPatent = root.join("freeLoads", JoinType.LEFT).get("patent");
                    if (sort.ascending()) {
                        orders.add(builder.asc(containerCode));
                        orders.add(builder.asc(freeLoadPatent));
                    } else {
                        orders.add(builder.desc(containerCode));
                        orders.add(builder.desc(freeLoadPatent));
                    }
                }
                case "creation_date" -> {
                    if (sort.ascending()) {
                        orders.add(builder.asc(root.get(Order.Fields.id)));
                    } else {
                        orders.add(builder.desc(root.get(Order.Fields.id)));
                    }
                }
            }
        }
        return orders;
    }

    @Override
    public long count(@Nullable String codeLike,
                      @Nullable Boolean pema,
                      @Nullable Boolean transport,
                      @Nullable Boolean port,
                      @Nullable LocalDate creationDateFrom,
                      @Nullable LocalDate creationDateTo,
                      @Nullable LocalDate arrivalDateFrom,
                      @Nullable LocalDate arrivalDateTo,
                      @Nullable LocalTime arrivalTimeFrom,
                      @Nullable LocalTime arrivalTimeTo,
                      @Nullable Long clientId,
                      @Nullable OrderStatus status,
                      @Nullable String loadCode,
                      @Nullable String origin,
                      @Nullable String target,
                      @Nullable String consigneeCuit,
                      @Nullable String destinationCode) {

        final CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
        final Root<Order> root = criteriaQuery.from(Order.class);

        root.join(Order.Fields.client, JoinType.INNER);

        criteriaQuery.select(builder.count(root));

        final Predicate[] predicates = createPredicates(builder, root, codeLike, pema, transport, port,
                creationDateFrom, creationDateTo, arrivalDateFrom, arrivalDateTo, arrivalTimeFrom, arrivalTimeTo, clientId, status,
                loadCode, origin, target, consigneeCuit, destinationCode);
        criteriaQuery.where(predicates);

        return entityManager.createQuery(criteriaQuery).getSingleResult();
    }

    private Predicate[] createPredicates(CriteriaBuilder builder, Root<Order> root,
                                         @Nullable String codeLike,
                                         @Nullable Boolean pema,
                                         @Nullable Boolean transport,
                                         @Nullable Boolean port,
                                         @Nullable LocalDate creationDateFrom,
                                         @Nullable LocalDate creationDateTo,
                                         @Nullable LocalDate arrivalDateFrom,
                                         @Nullable LocalDate arrivalDateTo,
                                         @Nullable LocalTime arrivalTimeFrom,
                                         @Nullable LocalTime arrivalTimeTo,
                                         @Nullable Long clientId,
                                         @Nullable OrderStatus status,
                                         @Nullable String loadCode,
                                         @Nullable String origin,
                                         @Nullable String target,
                                         @Nullable String consigneeCuit,
                                         @Nullable String destinationCode) {

        final List<Predicate> predicates = new ArrayList<>();

        if (Objects.nonNull(codeLike)) {
            predicates.add(builder.like(root.get(Order.Fields.code), "%" + codeLike + "%"));
        }

        if (Objects.nonNull(pema)) {
            predicates.add(builder.equal(root.get(Order.Fields.pema), pema));
        }

        if (Objects.nonNull(transport)) {
            predicates.add(builder.equal(root.get(Order.Fields.transport), transport));
        }

        if (Objects.nonNull(port)) {
            predicates.add(builder.equal(root.get(Order.Fields.port), port));
        }

        if (Objects.nonNull(creationDateFrom)) {
            final long createdAtFromSeconds = creationDateFrom.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
            predicates.add(builder.greaterThanOrEqualTo(root.get(Order.Fields.createdAtIdx), createdAtFromSeconds));
        }

        if (Objects.nonNull(creationDateTo)) {
            final long createdAtToSeconds = creationDateTo.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
            predicates.add(builder.lessThanOrEqualTo(root.get(Order.Fields.createdAtIdx), createdAtToSeconds));
        }

        if (Objects.nonNull(arrivalDateFrom)) {
            predicates.add(builder.greaterThanOrEqualTo(root.get(Order.Fields.arrivalDate), arrivalDateFrom));
        }

        if (Objects.nonNull(arrivalDateTo)) {
            predicates.add(builder.lessThanOrEqualTo(root.get(Order.Fields.arrivalDate), arrivalDateTo));
        }

        if (Objects.nonNull(arrivalTimeFrom)) {
            predicates.add(builder.greaterThanOrEqualTo(root.get(Order.Fields.arrivalTime), arrivalTimeFrom));
        }

        if (Objects.nonNull(arrivalTimeTo)) {
            predicates.add(builder.lessThanOrEqualTo(root.get(Order.Fields.arrivalTime), arrivalTimeTo));
        }

        if (Objects.nonNull(clientId)) {
            predicates.add(builder.equal(root.get(Order.Fields.client).get(Client.Fields.id), clientId));
        }

        if (Objects.nonNull(status)) {
            predicates.add(builder.equal(root.get(Order.Fields.status), status));
        }

        if (Objects.nonNull(origin)) {
            predicates.add(builder.equal(root.get(Order.Fields.origin), origin));
        }

        if (Objects.nonNull(target)) {
            predicates.add(builder.equal(root.get(Order.Fields.target), target));
        }

        if (Objects.nonNull(consigneeCuit)) {
            predicates.add(builder.equal(root.get(Order.Fields.consignee).get(ConsigneeData.Fields.cuit), consigneeCuit));
        }

        final boolean shouldFilterIds = Objects.nonNull(loadCode) || Objects.nonNull(destinationCode);
        final List<Long> orderIds = new ArrayList<>();
        if (Objects.nonNull(loadCode)) {
            orderIds.addAll(containerRepo.findByCode(loadCode).stream().map(Container::getOrderId).toList());
            orderIds.addAll(freeLoadRepo.findByPatent(loadCode).stream().map(FreeLoad::getOrderId).toList());
        }

        if (Objects.nonNull(destinationCode)) {
            final String theDestinationCode = StringUtils.left(destinationCode, 16);
            orderIds.addAll(containerRepo.findOrderIdByDestinationCode(theDestinationCode));
            orderIds.addAll(freeLoadRepo.findOrderIdByDestinationCode(theDestinationCode));
        }

        if (shouldFilterIds) {
            predicates.add(root.get(Order.Fields.id).in(orderIds.stream().distinct().collect(Collectors.toList())));
        }

        return predicates.toArray(new Predicate[0]);
    }

    @Override
    public List<Object[]> getReport(@Nullable Long clientId, LocalDate dateFrom, LocalDate dateTo) {
        final String clientIdWhere = Optional.ofNullable(clientId)
                .map(id -> "and client_id = " + clientId)
                .orElse("");

        final long createdAtFromSeconds = dateFrom.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
        final long createdAtToSeconds = dateTo.atStartOfDay().toEpochSecond(ZoneOffset.UTC);

        final Query query = entityManager.createNativeQuery("""
                select\s
                        o.id as "op",
                        cl.name as "cliente",\s
                        DATE_FORMAT(o.arrival_date, '%d/%m/%Y') as "f.turno",\s
                        left(o.arrival_time, 5) as "h.turno",\s
                        o.origin as "de",\s
                        o.target as "a",\s
                        \s
                         case o.free_load\s
                     		when 1 then "Si"
                     		else "No"
                         end as "c.suelta",
                        \s
                     	case o.free_load\s
                     		when 0 then ctrd.codes
                     		else ctrd.codes
                         end as "destinaciones",
                     
                         case o.free_load\s
                     		when 1 then fl.patent
                     		else ctr.code
                         end as "ctr/patente",
                        \s
                         case o.free_load\s
                     		when 1 then fl.type
                     		else ctr.type
                         end as "tipo",
                        \s
                     	co.name as "factura nombre",
                      	co.cuit as "factura cuit",
                        \s
                         dr.company as "e.tte",
                        \s
                     	case o.free_load\s
                     		when 1 then "-"
                     		else ctr.pema
                         end as "e.pema",
                        \s
                        case o.port\s
                     		when 1 then "Si"
                     		else "No"
                         end as "g.pto"
                         
                from orders o
                join clients cl on o.client_id = cl.id
                left join consignee_datas co on o.consignee_id = co.id
                left join containers ctr on ctr.order_id = o.id
                left join (select GROUP_CONCAT(code) as "codes", id from container_destinations group by id) ctrd on ctrd.id = ctr.id
                left join free_loads fl on fl.order_id = o.id
                left join (select GROUP_CONCAT(code) as "codes", id from free_load_destinations group by id) fld on fld.id = fl.id
                left join driver_datas dr on dr.id = o.driver_data_id
                """
                + String.format("""
                where o.status != 'CANCELLED'
                and o.created_at_idx between '%s' and '%s' %s order by o.id desc;
                """, createdAtFromSeconds, createdAtToSeconds, clientIdWhere));

        return query.getResultList();
    }
}