package com.digital_timetable.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.List;

@Component
public class EmailClient {

    private final JavaMailSender mailSender;

    @Value("${app.mail.enabled:true}")
    private boolean mailEnabled;

    @Value("${app.mail.from:no-reply@digital-timetable.local}")
    private String defaultFrom;

    public EmailClient(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendEmail(@NonNull List<String> to, @NonNull String subject, @NonNull String body) {
        if (!mailEnabled || to.isEmpty()) return;
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            // Parse display name if present in defaultFrom
            InternetAddress fromAddress = new InternetAddress(defaultFrom);
            helper.setFrom(fromAddress);
            helper.setTo(to.toArray(new String[0]));
            helper.setSubject(subject);
            helper.setText(body, false);
            mailSender.send(mimeMessage);
        } catch (Exception ex) {
            // Log and continue; email failures shouldn't break core flows
            System.err.println("Email send failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void sendEmailTo(@Nullable String to, @NonNull String subject, @NonNull String body) {
        if (to == null || to.isBlank()) return;
        sendEmail(java.util.List.of(to), subject, body);
    }
}
