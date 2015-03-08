package nl.yellowbrick.admin;

import nl.yellowbrick.data.TestConfiguration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { MvcTestConfiguration.class, TestConfiguration.class })
@Transactional
public class BaseMvcTestCase {
    protected static Document parseHtml(MvcResult res) {
        try {
            return Jsoup.parse(res.getResponse().getContentAsString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
