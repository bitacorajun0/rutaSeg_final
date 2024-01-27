package com.jumani.rutaseg.dto.response;

import com.jumani.rutaseg.domain.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;

@EqualsAndHashCode(exclude = "createdAt")
@AllArgsConstructor
@Getter
public class OrderResponse {
    private final long id;
    private final String code;
    private final long clientId;
    private final long createdByUserId;
    private final boolean pema;
    private final boolean port;
    private final boolean transport;
    private final LocalDate arrivalDate;
    private final LocalTime arrivalTime;
    private final String origin;
    private final String target;
    private Boolean freeLoad;
    private final OrderStatus status;
    private final ZonedDateTime createdAt;
    private final ZonedDateTime finishedAt;
    private final DriverDataResponse driverData;
    private final CustomsDataResponse customsData;
    private final List<ContainerResponse> containers;
    private final int containerQty;
    private final List<FreeLoadResponse> freeLoads;
    private final int freeLoadQty;
    private final ConsigneeDataResponse consignee;
    private final List<DocumentResponse> documents;
    private final List<NoteResponse> notes;
    private final List<CostResponse> costs;
    private final boolean returned;
    private final boolean billed;
}

