package com.jumani.rutaseg.service.email;

import com.jumani.rutaseg.dto.result.Error;

import java.util.List;
import java.util.Optional;

public interface EmailSender {
    Optional<Error> send(String from, List<String> to, String subject, String text);
}
