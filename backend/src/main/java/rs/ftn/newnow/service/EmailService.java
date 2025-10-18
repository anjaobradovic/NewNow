package rs.ftn.newnow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Registration Approved - NewNow");
            message.setText(String.format(
                "Hello %s,\n\n" +
                "Your registration request has been approved!\n\n" +
                "You can now log in to the NewNow application using your email and password.\n\n" +
                "Best regards,\n" +
                "NewNow Team",
                userName
            ));

            mailSender.send(message);
            log.info("Registration approved email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send registration approved email to: {}", toEmail, e);
        }
    }

    public void sendRegistrationRejectedEmail(String toEmail, String userName, String reason) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Registration Rejected - NewNow");
            message.setText(String.format(
                "Hello %s,\n\n" +
                "We regret to inform you that your registration request has been rejected.\n\n" +
                "Reason: %s\n\n" +
                "If you believe this was a mistake or have any questions, please contact our support team.\n\n" +
                "Best regards,\n" +
                "NewNow Team",
                userName,
                reason != null ? reason : "Not specified"
            ));

            mailSender.send(message);
            log.info("Registration rejected email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send registration rejected email to: {}", toEmail, e);
        }
    }

    public void sendPasswordChangeEmail(String toEmail, String userName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Password Changed - NewNow");
            message.setText(String.format(
                "Hello %s,\n\n" +
                "Your password has been successfully changed.\n\n" +
                "If you did not make this change, please contact our support team immediately.\n\n" +
                "Best regards,\n" +
                "NewNow Team",
                userName
            ));

            mailSender.send(message);
            log.info("Password change email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password change email to: {}", toEmail, e);
        }
    }
}
