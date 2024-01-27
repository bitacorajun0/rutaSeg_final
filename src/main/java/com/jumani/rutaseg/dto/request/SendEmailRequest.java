package com.jumani.rutaseg.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class SendEmailRequest {

    @NotBlank
    private final String subject;

    @NotBlank
    private final String body;

    @JsonCreator
    public SendEmailRequest(@JsonProperty("subject") String subject,
                            @JsonProperty("body") String body) {
        this.subject = subject;
        this.body = body;
    }
}
