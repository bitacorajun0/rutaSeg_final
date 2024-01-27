package com.jumani.rutaseg.service.email;

import com.jumani.rutaseg.dto.result.Error;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
public class EmailSenderDev implements EmailSender {
    @Override
    public Optional<Error> send(String from, List<String> to, String subject, String body) {
        log.info("DEV sent email with [from:{}] [to:{}] [subject:{}] [message:{}]", from, to, subject, body);
        return Optional.empty();
    }
}
