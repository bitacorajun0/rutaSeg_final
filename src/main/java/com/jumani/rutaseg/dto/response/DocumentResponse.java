package com.jumani.rutaseg.dto.response;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.ZonedDateTime;

@EqualsAndHashCode(exclude = "createdAt")
@AllArgsConstructor
@Getter
public class DocumentResponse {
    private final long id;
    private final ZonedDateTime createdAt;
    private final  String name;
    private final  String resource;
    private final  String link;
}
