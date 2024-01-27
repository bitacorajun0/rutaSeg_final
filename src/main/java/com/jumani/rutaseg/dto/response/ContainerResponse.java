package com.jumani.rutaseg.dto.response;

import com.jumani.rutaseg.domain.ContainerType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@EqualsAndHashCode
@AllArgsConstructor
@Getter
public class ContainerResponse {

    private final String code;
    private final ContainerType type;
    private final String bl;
    private final boolean repackage;
    private final String pema;
    private final List<DestinationResponse> destinations;

}
