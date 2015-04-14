package nl.yellowbrick.travelcard.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class EmailNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailNotificationService.class);

    private final JavaMailSender mailSender;
    private String adminEmail;

    @Autowired
    public EmailNotificationService(JavaMailSender mailSender, @Value("${adminEmail}") String adminEmail) {
        this.mailSender = mailSender;
        this.adminEmail = adminEmail;
    }

    public void notifyFileImported(Path file) {
        try {
            MimeMessageHelper helper = messageHelper("Travelcard whitelist imported", "Path to file: " + file.toString());
            mailSender.send(helper.getMimeMessage());
        } catch (Exception e) {
            LOGGER.error("Failed to send notifyFileImported email", e);
        }
    }

    public void notifyImportFailed(Path file, String reason) {
        try {
            String msgBody = "Path to file: " + file.toString() + "\nreason: " + reason;

            MimeMessageHelper helper = messageHelper("Failure to import Travelcard whitelist ", msgBody);

            if(lessThan1MB(file))
                helper.addAttachment(file.toFile().getName(), file.toFile());

            mailSender.send(helper.getMimeMessage());
        } catch (Exception e) {
            LOGGER.error("Failed to send notifyImportFailed email", e);
        }
    }

    private boolean lessThan1MB(Path file) {
        try {
            return Files.size(file) < 1024 * 1024;
        } catch (IOException e) {
            LOGGER.error("Failed to determine file size for attachment", e);
            return false;
        }
    }

    private MimeMessageHelper messageHelper(String subject, String text) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "ISO-8859-1");

        messageHelper.setTo(adminEmail);
        messageHelper.setFrom(adminEmail);
        messageHelper.setSubject(subject);
        messageHelper.setText(text);

        return messageHelper;
    }
}
