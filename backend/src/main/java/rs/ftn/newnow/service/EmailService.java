package rs.ftn.newnow.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@newnow.com}")
    private String fromEmail;

    public void sendRegistrationApprovedEmail(String toEmail, String userName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("🎉 Welcome to NewNow - Registration Approved!");
            
            String htmlContent = buildEmailTemplate(
                "Registration Approved!",
                userName,
                "Great news! Your registration request has been approved and you can start using the NewNow application.",
                "You can now log in using your email address and password, and start discovering events and happenings in your area.",
                "Sign In",
                "#10b981",
                "✅"
            );
            
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Registration approved email sent to: {}", toEmail);
        } catch (MessagingException | org.springframework.mail.MailException e) {
            log.error("Failed to send registration approved email to: {}", toEmail, e);
        }
    }

    public void sendRegistrationRejectedEmail(String toEmail, String userName, String reason) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("NewNow - Registration Request Update");
            
            String reasonText = reason != null && !reason.isEmpty() 
                ? "Reason: " + reason 
                : "The system administrator has reviewed your request, but at this time it is not possible to approve the registration.";
            
            String htmlContent = buildEmailTemplate(
                "Registration Request",
                userName,
                "Thank you for your interest in the NewNow application.",
                "We regret to inform you that your registration request has not been approved. " + reasonText + 
                "<br><br>If you believe this was a mistake or have any questions, please feel free to contact our support team.",
                null,
                "#ef4444",
                "ℹ️"
            );
            
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Registration rejected email sent to: {}", toEmail);
        } catch (MessagingException | org.springframework.mail.MailException e) {
            log.error("Failed to send registration rejected email to: {}", toEmail, e);
        }
    }

    public void sendPasswordChangeEmail(String toEmail, String userName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("🔒 NewNow - Password Changed");
            
            String htmlContent = buildEmailTemplate(
                "Password Changed",
                userName,
                "Your password has been successfully changed.",
                "This is an automatic notification that the password for your NewNow account has just been changed. " +
                "<br><br><strong>If you did not make this change, please contact our support team immediately to protect your account.</strong>",
                null,
                "#3b82f6",
                "🔐"
            );
            
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Password change email sent to: {}", toEmail);
        } catch (MessagingException | org.springframework.mail.MailException e) {
            log.error("Failed to send password change email to: {}", toEmail, e);
        }
    }

    private String buildEmailTemplate(String title, String userName, String heading, 
                                     String content, String buttonText, String accentColor, String emoji) {
        String buttonHtml = "";
        if (buttonText != null) {
            buttonHtml = String.format(
                "<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"margin: 30px 0;\">" +
                "<tr>" +
                "<td style=\"border-radius: 6px; background-color: %s;\">" +
                "<a href=\"#\" style=\"display: inline-block; padding: 14px 32px; font-family: 'Segoe UI', Arial, sans-serif; font-size: 16px; color: #ffffff; text-decoration: none; font-weight: 600;\">%s</a>" +
                "</td>" +
                "</tr>" +
                "</table>",
                accentColor, buttonText
            );
        }

        return String.format(
            "<!DOCTYPE html>" +
            "<html lang=\"en\">" +
            "<head>" +
            "<meta charset=\"UTF-8\">" +
            "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
            "<title>%s</title>" +
            "</head>" +
            "<body style=\"margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f3f4f6;\">" +
            "<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%%\" style=\"background-color: #f3f4f6; padding: 40px 20px;\">" +
            "<tr>" +
            "<td align=\"center\">" +
            "<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"600\" style=\"background-color: #ffffff; border-radius: 12px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); overflow: hidden;\">" +
            
            "<!-- Header -->" +
            "<tr>" +
            "<td style=\"background: linear-gradient(135deg, %s 0%%, %s 100%%); padding: 40px 30px; text-align: center;\">" +
            "<div style=\"font-size: 48px; margin-bottom: 10px;\">%s</div>" +
            "<h1 style=\"margin: 0; color: #ffffff; font-size: 32px; font-weight: 700; text-shadow: 0 2px 4px rgba(0,0,0,0.1);\">NewNow</h1>" +
            "<p style=\"margin: 10px 0 0 0; color: #ffffff; font-size: 14px; opacity: 0.95;\">Discover events in your city</p>" +
            "</td>" +
            "</tr>" +
            
            "<!-- Content -->" +
            "<tr>" +
            "<td style=\"padding: 40px 30px;\">" +
            "<h2 style=\"margin: 0 0 10px 0; color: #1f2937; font-size: 24px; font-weight: 600;\">Hello, %s!</h2>" +
            "<p style=\"margin: 0 0 20px 0; color: %s; font-size: 18px; font-weight: 600;\">%s</p>" +
            "<p style=\"margin: 0; color: #4b5563; font-size: 16px; line-height: 1.6;\">%s</p>" +
            "%s" +
            "</td>" +
            "</tr>" +
            
            "<!-- Footer -->" +
            "<tr>" +
            "<td style=\"background-color: #f9fafb; padding: 30px; text-align: center; border-top: 1px solid #e5e7eb;\">" +
            "<p style=\"margin: 0 0 10px 0; color: #6b7280; font-size: 14px;\">This is an automated message, please do not reply to this email.</p>" +
            "<p style=\"margin: 0; color: #9ca3af; font-size: 12px;\">&copy; 2025 NewNow. All rights reserved.</p>" +
            "<div style=\"margin-top: 20px;\">" +
            "<span style=\"display: inline-block; margin: 0 10px; font-size: 24px;\">🎭</span>" +
            "<span style=\"display: inline-block; margin: 0 10px; font-size: 24px;\">🎵</span>" +
            "<span style=\"display: inline-block; margin: 0 10px; font-size: 24px;\">🎪</span>" +
            "<span style=\"display: inline-block; margin: 0 10px; font-size: 24px;\">🎨</span>" +
            "</div>" +
            "</td>" +
            "</tr>" +
            
            "</table>" +
            "</td>" +
            "</tr>" +
            "</table>" +
            "</body>" +
            "</html>",
            title,
            accentColor, darkenColor(accentColor),
            emoji,
            userName,
            accentColor,
            heading,
            content,
            buttonHtml
        );
    }

    private String darkenColor(String hexColor) {
        // Simple darkening by reducing RGB values by 20%
        if (!hexColor.startsWith("#")) return hexColor;
        
        try {
            int r = Integer.parseInt(hexColor.substring(1, 3), 16);
            int g = Integer.parseInt(hexColor.substring(3, 5), 16);
            int b = Integer.parseInt(hexColor.substring(5, 7), 16);
            
            r = (int)(r * 0.8);
            g = (int)(g * 0.8);
            b = (int)(b * 0.8);
            
            return String.format("#%02x%02x%02x", r, g, b);
        } catch (Exception e) {
            return hexColor;
        }
    }
}
