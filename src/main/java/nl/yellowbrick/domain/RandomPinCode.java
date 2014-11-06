package nl.yellowbrick.domain;

import java.security.SecureRandom;
import java.util.Random;

public class RandomPinCode {

    private final String code;

    public RandomPinCode() {
        this.code = generateRandom4CharCode();
    }

    public String code() {
        return code;
    }

    private static String generateRandom4CharCode() {
        Random random = new SecureRandom();

        return String.format("%04d", random.nextInt(9999 + 1));
    }
}
