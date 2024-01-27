package com.jumani.rutaseg.dto.response;

import com.jumani.rutaseg.domain.DestinationType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DestinationResponse {
    private final DestinationType type;
    private final String code;
    private final String fob;
    private final String currency;
    private final String productDetails;
}
