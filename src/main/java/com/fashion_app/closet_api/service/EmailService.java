package com.fashion_app.closet_api.service;

import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EmailService{
    private final JavaMailSender javaMailSender;

    public void sendNotification(String to, String email) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);
            messageHelper.setTo(to);
            messageHelper.setSubject("Confirm your email");
            messageHelper.setText(email, true);
            messageHelper.setFrom("noreply@mail.com");
            javaMailSender.send(message);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to send email", e);
        }
    }
}
