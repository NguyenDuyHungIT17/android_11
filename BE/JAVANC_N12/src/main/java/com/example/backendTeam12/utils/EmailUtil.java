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

    @Value("${app.reset-password-url}")
    private String resetPasswordUrl;

    @Value("${app.reset-token-expiration}")
    private int expirationMinutes;

    @Autowired
    public EmailUtil(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendResetPasswordEmail(String to, String token, String userName) throws MessagingException, IOException {
        
        String resetLink = resetPasswordUrl + token;

        
        InputStream is = new ClassPathResource("templates/reset-password.html").getInputStream();
        String template = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        
        String content = template.replace("${resetLink}", resetLink)
                                 .replace("${expiration}", String.valueOf(expirationMinutes))
                                 .replace("${username}", userName);

        
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
        helper.setTo(to);
        helper.setSubject("Yêu cầu đặt lại mật khẩu");
        helper.setText(content, true);
        mailSender.send(message);
    }
}
