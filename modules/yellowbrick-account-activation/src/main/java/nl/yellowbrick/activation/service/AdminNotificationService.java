package nl.yellowbrick.activation.service;

import com.google.common.collect.Maps;
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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.time.LocalDateTime.now;

@Component
public class AdminNotificationService {

    // keep track of when messages are sent to avoid spamming the admin
    private static final HashMap<Integer, LocalDateTime> MESSAGES_SENT_TIMES = Maps.newHashMap();
    // make sure we don't send the same message twice within a certain time window
    private static final int MINUTES_BETWEEN_REPEATS = 6 * 60;

    private static final String FROM = "noreply@brickparking.com";

    private static final Function<String, String> POOL_EXHAUSTED_SUBJECT = profile ->
            String.format("[BRICKWALL %s] Card pool exhausted", profile);
    private static final Function<String, String> POOL_EXHAUSTED_BODY = productGroupId ->
            String.format("Impossible to assign transponder cards for product group id %s: card pool exhausted", productGroupId);

    private static final Function<String, String> POOL_EXHAUSTING_SUBJECT = profile ->
            String.format("[BRICKWALL %s] Card pool quickly exhausting", profile);
    private static final BiFunction<String, String, String> POOL_EXHAUSTING_BODY = (productGroupId, cardsLeft)  ->
            String.format("Card pool for product group id %s is quickly exhausting with only %s cards left",
                    productGroupId, cardsLeft);

    private final String profile;
    private final String adminEmail;
    private JavaMailSender mailSender;

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

    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void notifyCardPoolExhausted(long productGroupId) {
        try {
            MimeMessage message = createMessage(
                    POOL_EXHAUSTED_SUBJECT.apply(profile),
                    POOL_EXHAUSTED_BODY.apply(String.valueOf(productGroupId))
            );

            log.warn("Notifying admin card pool for product group {} has expired", productGroupId);

            sendMessage(message);
        } catch (MessagingException e) {
            log.error("Failed to compose notifyCardPoolExhausted email", e);
        }
    }

    public void notifyCardPoolExhausting(long productGroupId, int cardsAvailable) {
        // maybe card pool has already expired?
        if(cardsAvailable == 0) {
            notifyCardPoolExhausted(productGroupId);
            return;
        }

        try {
            MimeMessage message = createMessage(
                    POOL_EXHAUSTING_SUBJECT.apply(profile),
                    POOL_EXHAUSTING_BODY.apply(String.valueOf(productGroupId), String.valueOf(cardsAvailable))
            );

            log.warn("Notifying admin that at {} cards available the card pool for product group {} is about to expire",
                    cardsAvailable, productGroupId);

            sendMessage(message);
        } catch (MessagingException e) {
            log.error("Failed to compose notifyCardPoolExhausting email", e);
        }
    }

    private MimeMessage createMessage(String subject, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "ISO-8859-1");

        messageHelper.setFrom(FROM);
        messageHelper.setTo(adminEmail);
        messageHelper.setSubject(subject);
        messageHelper.setText(body, false);

        return message;
    }

    private void sendMessage(MimeMessage message) throws MessagingException {
        int msgHash = message.hashCode();

        if(MESSAGES_SENT_TIMES.containsKey(msgHash)
                && MESSAGES_SENT_TIMES.get(msgHash).isAfter(now().minusMinutes(MINUTES_BETWEEN_REPEATS))) {
            log.warn("skipping sending message {} to avoid spamming email", message.getSubject());
            return;
        }

        mailSender.send(message);
        MESSAGES_SENT_TIMES.put(msgHash, now());
    }
}
