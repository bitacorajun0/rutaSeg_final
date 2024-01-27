package com.jumani.rutaseg.controller;

import com.jumani.rutaseg.dto.request.SendEmailRequest;
import com.jumani.rutaseg.service.email.InternalEmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class EmailController {

    private final InternalEmailService service;

    @PostMapping("/send-email")
    public ResponseEntity<?> sendEmail(@Valid @RequestBody SendEmailRequest request) {
        return service.send(request.getSubject(), request.getBody())
                .map(err -> ResponseEntity.internalServerError().body(err))
                .orElse(ResponseEntity.noContent().build());
    }
}
