package com.jumani.rutaseg.dto.request;

import com.jumani.rutaseg.domain.ContainerType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ContainerRequest {

    private String code;

    private ContainerType type;

    private boolean repackage;

    private String bl;

    private String pema;

    private List<DestinationRequest> destinations;

}
