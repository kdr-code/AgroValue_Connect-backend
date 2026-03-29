package com.agrovalue.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String toEmail, String token) {

        String subject = "Verify your account";
        String url = "http://localhost:8080/auth/verify?token=" + token;

        String message = "Click the link to verify your account:\n" + url;

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(toEmail);
        mail.setSubject(subject);
        mail.setText(message);

        mailSender.send(mail);
    }
}