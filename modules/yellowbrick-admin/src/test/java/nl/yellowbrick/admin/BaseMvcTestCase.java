package nl.yellowbrick.admin;

import nl.yellowbrick.data.TestConfiguration;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { MvcTestConfiguration.class, TestConfiguration.class })
@Transactional
public class BaseMvcTestCase {
}
