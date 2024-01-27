package com.jumani.rutaseg.dto.response;

import com.jumani.rutaseg.domain.FreeLoadType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class FreeLoadResponse {

    private final String patent;

    private final FreeLoadType type;

    private final String weight;

    private final String guide;

    private final String pema;

    private final List<DestinationResponse> destinations;

}
