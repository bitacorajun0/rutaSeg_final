package com.jumani.rutaseg.dto.response;

import com.jumani.rutaseg.domain.Consignee;

import java.util.List;

public record ClientResponse(long id, Long userId, String name, String phone, Long cuit, List<Consignee> consignees) {
}
