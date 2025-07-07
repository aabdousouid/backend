package com.bezkoder.spring.security.postgresql.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;



    public void sendApplicationEmail(String toEmail){
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("Job application");


            String htmlContent = ApplicationEmail();
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }

    }

    public void sendVerificationEmail(String toEmail, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("Verify Your Email Address");

            String verificationUrl = baseUrl + "/api/auth/verify?token=" + token;

            String htmlContent = buildVerificationEmailContent(verificationUrl);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
    private String buildVerificationEmailContent(String verificationUrl) {
        return "<html>" +
                "<body>" +
                "<h2>Welcome! Please verify your email</h2>" +
                "<p>Thank you for registering. Please click the link below to verify your email address:</p>" +
                "<a href='" + verificationUrl + "' style='background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>Verify Email</a>" +
                "<p>This link will expire in 24 hours.</p>" +
                "<p>If you didn't create an account, please ignore this email.</p>" +
                "</body>" +
                "</html>";
    }


    private String ApplicationEmail() {
        return "<html>" +
                "<body>" +
                "<h2>Application sent successfully</h2>" +
                "<p>Thank you for applying to job offer we will reach out soon inchalah:</p>" +
                "<p>If you didn't apply or don't have an account, please ignore this email.</p>" +
                "</body>" +
                "</html>";
    }
}
