package com.jumani.rutaseg.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class DriverDataRequest {

    private String name;
    private String phone;
    private String chasis;
    private String semi;
    private String company;

}
