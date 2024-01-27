package com.jumani.rutaseg.dto.response;


import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
@AllArgsConstructor
@Getter
public class ConsigneeDataResponse {


    private String name;

    private Long cuit;
}
