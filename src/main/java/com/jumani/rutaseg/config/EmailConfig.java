package com.jumani.rutaseg.config;

import com.jumani.rutaseg.service.email.EmailSender;
import com.jumani.rutaseg.service.email.EmailSenderDev;
import com.jumani.rutaseg.service.email.EmailSenderImpl;
import com.jumani.rutaseg.service.email.InternalEmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.List;
import java.util.Properties;

@Configuration
public class EmailConfig {

    @Bean
    @Profile("!local & !integration_test")
    public EmailSender emailSender(@Value("${email.username}") String username,
                                   @Value("${email.password}") String password) {

        final JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost("smtp.gmail.com");
        javaMailSender.setPort(587);

        javaMailSender.setUsername(username);
        javaMailSender.setPassword(password);

        Properties props = javaMailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "false");

        return new EmailSenderImpl(javaMailSender);
    }

    @Bean
    @Profile("local | integration_test")
    public EmailSender emailSenderDev() {
        return new EmailSenderDev();
    }

    @Bean
    public InternalEmailService internalEmailService(EmailSender emailSender,
                                                     @Value("${email.internal.to}") List<String> to) {

        return new InternalEmailService(emailSender, to);
    }
}
