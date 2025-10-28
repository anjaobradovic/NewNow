package rs.ftn.newnow.config;

import jakarta.mail.internet.MimeMessage;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;

@TestConfiguration
@Profile("test")
public class TestEmailConfig {

    @Bean
    @Primary
    public JavaMailSender javaMailSender() {
        JavaMailSender mailSender = Mockito.mock(JavaMailSender.class);
        MimeMessage mimeMessage = Mockito.mock(MimeMessage.class);
        Mockito.when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        Mockito.doNothing().when(mailSender).send(Mockito.any(MimeMessage.class));
        return mailSender;
    }
}
