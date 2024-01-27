package com.jumani.rutaseg.dto.request;

import com.jumani.rutaseg.domain.DestinationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class DestinationRequest {
    private DestinationType type;
    private String code;
    private String fob;
    private String currency;
    private String productDetails;
}
