package nl.yellowbrick.admin.util;

public class LuhnUtil {

    /**
     * @see http://en.wikipedia.org/wiki/Luhn_algorithm
     */
    public static int calculateLuhnCode(String numberStr) {
        int luhnDigit;
        int currDigit;
        int sum = 0;
        int lastPos = numberStr.length() - 1;

        for (int i = lastPos; i >= 0; i--) {
            currDigit = new Integer("" + numberStr.charAt(i));
            if ((lastPos - i) % 2 == 0) currDigit = currDigit * 2;
            if (currDigit >= 10) { // calculate sum of digits of position
                currDigit = 1 + (currDigit - 10);
            }
            sum += currDigit;
        }
        luhnDigit = sum % 10;
        if (luhnDigit != 0) {
            luhnDigit = 10 - luhnDigit;
        }
        return luhnDigit;
    }

    /**
     * @see http://www.tech-faq.com/the-luhn-check-digit-algorithm-in-java.html
     */
    public static boolean isValidLuhn(String digitsOnly) {
        int digit;
        int addend;
        int sum = 0;
        boolean timesTwo = false;

        for (int i = digitsOnly.length() - 1; i >= 0; i--) {
            digit = Integer.parseInt(digitsOnly.substring(i, i + 1));
            if (timesTwo) {
                addend = digit * 2;
                if (addend > 9) {
                    addend -= 9;
                }
            } else {
                addend = digit;
            }
            sum += addend;
            timesTwo = !timesTwo;
        }
        int modulus = sum % 10;
        return modulus == 0;
    }
}
