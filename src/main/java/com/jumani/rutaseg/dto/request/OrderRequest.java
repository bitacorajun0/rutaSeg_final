package com.jumani.rutaseg.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class OrderRequest {

    private String code;
    @NotNull
    @Positive
    private Long clientId;
    private boolean pema;
    private boolean port;
    private boolean transport;
    private LocalDate arrivalDate;
    private LocalTime arrivalTime;
    private String origin;
    private String target;
    private Boolean freeLoad;
    private DriverDataRequest driverData;
    private CustomsDataRequest customsData;
    private List<ContainerRequest> containers;
    private List<FreeLoadRequest> freeLoads;
    private ConsigneeDataRequest consignee;

    public boolean isFreeLoad() {
        return Optional.ofNullable(this.freeLoad).orElse(false);
    }
}
