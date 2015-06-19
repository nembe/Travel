package nl.yellowbrick.activation.service;

import nl.yellowbrick.data.BaseSpringTestCase;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.internet.MimeMessage;
import java.util.stream.IntStream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class AdminNotificationServiceTest extends BaseSpringTestCase {

    @Autowired AdminNotificationService notificationService;
    @Autowired JavaMailSender mailSender;

    @Before
    public void setUp() {
        // spy on the mail sender
        mailSender = spy(mailSender);
        notificationService.setMailSender(mailSender);

        // avoid triggering actual emails
        doNothing().when(mailSender).send(any(MimeMessage.class));
    }

    @Test
    public void avoids_sending_duplicate_message_in_short_period_of_time() throws Exception {
        // should only send the first of these ...
        IntStream.of(10).forEach(i -> notificationService.notifyCardPoolExhausted(1));
        // then one of these since it's a different product group ...
        notificationService.notifyCardPoolExhausted(2);

        // so in total 2 messages are expected
        verify(mailSender, times(2)).send(any(MimeMessage.class));
    }
}
