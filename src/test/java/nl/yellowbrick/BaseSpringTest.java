package nl.yellowbrick;

import nl.yellowbrick.bootstrap.AccountActivationTask;
import nl.yellowbrick.bootstrap.Application;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class, AccountActivationTask.class })
public abstract class BaseSpringTest {

}
