package com.jumani.rutaseg.service.email;

import com.jumani.rutaseg.dto.result.Error;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class EmailSenderImpl implements EmailSender {
    private final JavaMailSender emailSender;

    public Optional<Error> send(String from, List<String> to, String subject, String body) {
        final SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom(from);
        email.setTo(to.toArray(new String[]{}));
        email.setSubject(subject);
        email.setText(body);

        try {
            emailSender.send(email);
            return Optional.empty();
        } catch (Exception e) {
            log.error("failed to send email {}", email, e);
            return Optional.of(new Error("email_send_error", "failed to send email"));
        }
    }
}
