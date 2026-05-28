package com.phuocnt.trading_platform_be.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void senVerificationOtpEmail(String email, String otpCode) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

        helper.setSubject("Verification OTP");
        helper.setText("Your OTP code is: " + otpCode);
        helper.setTo(email);

        try {
            mailSender.send(message);
        } catch (MailException e) {
            throw new MailSendException(e.getMessage());
        }
    }
}