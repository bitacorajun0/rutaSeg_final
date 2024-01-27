package com.jumani.rutaseg.dto.request;

import com.jumani.rutaseg.domain.FreeLoadType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class FreeLoadRequest {

    private String patent;

    private FreeLoadType type;

    private String weight;

    private String guide;

    private String pema;

    private List<DestinationRequest> destinations;

}
