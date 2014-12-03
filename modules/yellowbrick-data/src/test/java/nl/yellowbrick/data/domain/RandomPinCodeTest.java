package nl.yellowbrick.data.domain;

import org.junit.Test;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class RandomPinCodeTest {

    @Test
    public void has_4_random_numbers() {
        String previousCode = "";

        Stream.generate(RandomPinCode::new).limit(1000).forEach((pinCode) -> {
            String code = pinCode.get();

            assertThat(code.length(), equalTo(4));
            assertThat(Integer.parseInt(code), is(lessThanOrEqualTo(9999)));
            assertThat(code, not(equalTo(previousCode)));
        });
    }
}
