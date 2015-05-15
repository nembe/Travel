package nl.yellowbrick.activation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.function.Function;

@Component
public class AdminNotificationService {

    private static final String FROM = "noreply@brickparking.com";

    private static final Function<String, String> POOL_EXHAUSTED_SUBJECT = profile ->
            String.format("[BRICKWALL %s] Card pool exhausted", profile);
    private static final Function<String, String> POOL_EXHAUSTED_BODY = productGroupId ->
            String.format("Impossible to assign transponder cards for product group id %s: card pool exhausted", productGroupId);

    private final String profile;
    private final String adminEmail;
    private final JavaMailSender mailSender;

    private Logger log = LoggerFactory.getLogger(AdminNotificationService.class);

    @Autowired
    public AdminNotificationService(Environment environment,
                                    @Value("${adminEmail}") String adminEmail,
                                    JavaMailSender mailSender) {
        this.profile = environment.getActiveProfiles().length > 0
                ? environment.getActiveProfiles()[0]
                : environment.getDefaultProfiles()[0];
        this.adminEmail = adminEmail;
        this.mailSender = mailSender;
    }

    public void notifyCardPoolExhausted(int productGroupId) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "ISO-8859-1");

            messageHelper.setFrom(FROM);
            messageHelper.setTo(adminEmail);
            messageHelper.setSubject(POOL_EXHAUSTED_SUBJECT.apply(profile));
            messageHelper.setText(POOL_EXHAUSTED_BODY.apply(String.valueOf(productGroupId)), false);

            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to compose notifyCardPoolExhausted email", e);
        }
    }
}
