package nl.yellowbrick.service;

import nl.yellowbrick.BaseSpringTestCase;
import nl.yellowbrick.dao.*;
import nl.yellowbrick.database.DbHelper;
import nl.yellowbrick.domain.Config;
import nl.yellowbrick.domain.Customer;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.util.StreamUtils;

import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

public class EmailNotificationServiceTest extends BaseSpringTestCase {

    @Autowired @InjectMocks EmailNotificationService emailNotificationService;

    @Autowired @Spy ConfigDao configDao;
    @Autowired @Spy CustomerDao customerDao;
    @Autowired @Spy SystemUserDao systemUserDao;
    @Autowired @Spy MessageDao messageDao;
    @Autowired @Spy JavaMailSender mailSender;

    @Autowired DbHelper db;

    Customer customer;

    @Before
    public void initMocks() throws Exception {
        MockitoAnnotations.initMocks(this);

        // avoid really sending emails
        doNothing().when(mailSender).send(any(MimeMessage.class));

        customer = new Customer();
        customer.setCustomerId(4776);
        customer.setCustomerNr("ABC123");
        customer.setGender("M");
        customer.setFirstName("Rui");
        customer.setInfix("de");
        customer.setLastName("Salgado");
        customer.setProductGroupID(8);
        customer.setEmail("rui.salgado@kabisa.nl");
    }

    @Test
    public void no_op_if_cant_get_config() {
        // no config
        doReturn(new ArrayList<>()).when(configDao).findAllBySection(any());

        emailNotificationService.notifyAccountAccepted(customer);

        verifyZeroInteractions(mailSender);
    }

    @Test
    public void no_op_if_cant_get_message_contents() {
        // no messages
        when(messageDao.getMessage(any(), any())).thenReturn(Optional.empty());
        when(messageDao.getGroupSpecificMessage(any(), anyInt(), any())).thenReturn(Optional.empty());

        emailNotificationService.notifyAccountAccepted(customer);

        verifyZeroInteractions(mailSender);
    }

    @Test
    // TODO this functionality should probably be moved elsewhere
    public void creates_user_token() {
        emailNotificationService.notifyAccountAccepted(customer);

        verify(systemUserDao).createAndStoreUserToken(any(Customer.class), any(LocalDateTime.class));
    }

    @Test
    public void sends_account_accepted_email() {
        emailNotificationService.notifyAccountAccepted(customer);

        verify(mailSender).send(argThat(is(expectedMailMessage())));
    }

    @Test
    public void uses_customer_locale_if_set() {
        db.accept((t) -> t.update("UPDATE CUSTOMER_REGISTRATION SET locale = 'pt_PT'"));

        emailNotificationService.notifyAccountAccepted(customer);

        verify(messageDao, times(2)).getMessage(anyString(), eq("pt_PT"));
        verify(messageDao).getGroupSpecificMessage(anyString(), anyInt(), eq("pt_PT"));
    }

    @Test
    public void otherwise_uses_locale_from_database() {
        // unset locale from customer registration
        db.accept((t) -> t.update("UPDATE CUSTOMER_REGISTRATION SET locale = NULL"));
        // change config locale to something we can assert on the test below
        doReturn(Optional.of(esLocaleCfg())).when(configDao).findSectionField(eq(ConfigSection.YB), eq("DEFAULT_LOCALE"));

        emailNotificationService.notifyAccountAccepted(customer);

        verify(messageDao, times(2)).getMessage(anyString(), eq("es_ES"));
        verify(messageDao).getGroupSpecificMessage(anyString(), anyInt(), eq("es_ES"));
    }

    @Test
    public void uses_nl_locale_as_last_resort() {
        // there are no locales at all in the database!
        db.accept((t) -> {
            t.update("UPDATE CUSTOMER_REGISTRATION SET locale = NULL");
            t.update("DELETE FROM TBLCONFIG WHERE field = 'DEFAULT_LOCALE'");
        });

        emailNotificationService.notifyAccountAccepted(customer);

        verify(messageDao, times(2)).getMessage(anyString(), eq("nl_NL"));
        verify(messageDao).getGroupSpecificMessage(anyString(), anyInt(), eq("nl_NL"));
    }

    private Object esLocaleCfg() {
        Config cfg = new Config();
        cfg.setValue("es_ES");

        return cfg;
    }

    private Matcher<MimeMessage> expectedMailMessage() {
        return new ArgumentMatcher<MimeMessage>() {

            boolean recipientsMatch(MimeMessage msg) throws Exception {
                return msg.getReplyTo()[0].toString().equals("info@yellowbrick.nl");
            }

            boolean replyToMatches(MimeMessage msg) throws Exception {
                return msg.getAllRecipients()[0].toString().equals("rui.salgado@kabisa.nl");
            }

            boolean subjectMatches(MimeMessage msg) throws Exception {
                return msg.getSubject().equals("Welkom bij Yellowbrick!");
            }

            boolean bodyMatches(MimeMessage msg) throws Exception {
                MimeMultipart msgContent = (MimeMultipart) msg.getContent();
                MimeBodyPart msgBody = (MimeBodyPart) msgContent.getBodyPart(0);

                String body = StreamUtils.copyToString(msgBody.getInputStream(), Charset.forName("UTF-8"));

                return body.contains("Geachte heer de Salgado")
                        && body.contains("ABC123")
                        && body.matches("(?s).*http:\\/\\/localhost:8084\\/MyYellowbrick\\/auth\\/password\\/reset\\/.*")
                        && !body.contains("%");
            }

            @Override
            public boolean matches(Object item) {
                MimeMessage msg = (MimeMessage) item;

                try {
                    return recipientsMatch(msg)
                            && replyToMatches(msg)
                            && subjectMatches(msg)
                            && bodyMatches(msg);
                } catch(Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("the account activation email");
            }
        };
    }
}