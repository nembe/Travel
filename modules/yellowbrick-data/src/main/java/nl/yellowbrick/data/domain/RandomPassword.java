package nl.yellowbrick.data.domain;

import org.mindrot.jbcrypt.BCrypt;

import java.security.SecureRandom;
import java.util.Random;

public class RandomPassword {

    private final String password;

    public RandomPassword() {
        this.password = generateRandomPassword();
    }

    public String get() {
        return password;
    }

    private static String generateRandomPassword() {
        return BCrypt.hashpw(generatePassword(), BCrypt.gensalt());
    }

    // this implementation was lifted almost as-is from the old taxameter app
    // only change was switching Random for SecureRandom
    // TODO refactor
    private static String generatePassword() {
        Random rnd = new SecureRandom();
        char passwordChars[] = new char[10];
        passwordChars[0] = (char)('A' + rnd.nextInt(26)); //first a letter
        passwordChars[1] = (char)('0' + rnd.nextInt(10)); // then a number
        for ( int i = 2; i < 10; i++ ) {
            int baseValue = '0' + rnd.nextInt(74);
            while ( (baseValue >= 58 && baseValue <= 64) || (baseValue >= 91 && baseValue <= 96) ) {
                baseValue = '0' + rnd.nextInt(74);
            }
            passwordChars[i] = (char)(baseValue);
        }

        return new String(passwordChars);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RandomPassword password1 = (RandomPassword) o;

        if (!password.equals(password1.password)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return password.hashCode();
    }
}
