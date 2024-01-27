package com.jumani.rutaseg.service.email;

import com.jumani.rutaseg.dto.result.Error;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class InternalEmailService {
    private static final String FROM = "SISTEMA <no-responder@gmail.com>";
    private final EmailSender emailSender;
    private final List<String> to;

    public final Optional<Error> send(String subject, String body) {
        return emailSender.send(FROM, this.to, subject, body);
    }
}
