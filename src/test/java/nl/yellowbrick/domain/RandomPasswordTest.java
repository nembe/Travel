package nl.yellowbrick.domain;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;

public class RandomPasswordTest {

    @Test
    public void has60characters() {
        assertThat(password().get().length(), equalTo(60));
    }

    @Test
    public void isRandomized() {
        List<RandomPassword> observedPasswords = new ArrayList<>();

        IntStream.of(1000).forEach((i) -> {
            RandomPassword password = password();

            assertThat(observedPasswords, not(hasItem(password)));
            observedPasswords.add(password);
        });
    }

    private RandomPassword password() {
        return new RandomPassword();
    }
}