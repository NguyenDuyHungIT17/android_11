package com.example.backendTeam12.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Component
public class EmailUtil {

    private final JavaMailSender mailSender;

    @Value("${app.reset-token-expiration}")
    private int expirationMinutes;

    @Autowired
    public EmailUtil(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendResetPasswordEmail(String to, String code, String userName) throws MessagingException, IOException {
        // Load template
        InputStream is = new ClassPathResource("templates/reset-password.html").getInputStream();
        String template = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        // Replace placeholders
        String content = template.replace("${username}", userName)
                                 .replace("${codeResetPassword}", code)
                                 .replace("${expiration}", String.valueOf(expirationMinutes));

        // Send email
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
        helper.setTo(to);
        helper.setSubject("Yêu cầu đặt lại mật khẩu");
        helper.setText(content, true);
        mailSender.send(message);
    }
}
