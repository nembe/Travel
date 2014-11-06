package nl.yellowbrick.domain;

import java.security.SecureRandom;
import java.util.Random;

public class RandomPinCode {

    private final String code;

    public RandomPinCode() {
        this.code = generateRandom4CharCode();
    }

    public String get() {
        return code;
    }

    private static String generateRandom4CharCode() {
        Random random = new SecureRandom();

        return String.format("%04d", random.nextInt(9999 + 1));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RandomPinCode pinCode = (RandomPinCode) o;

        if (!code.equals(pinCode.code)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }
}
