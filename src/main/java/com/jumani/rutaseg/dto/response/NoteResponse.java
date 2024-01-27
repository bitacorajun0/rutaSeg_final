package com.jumani.rutaseg.dto.response;

import com.jumani.rutaseg.domain.Author;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZonedDateTime;

@AllArgsConstructor
@Getter
public class NoteResponse {
    private Long id;
    private Author author;
    private String content;
    private ZonedDateTime createdAt;
}
