package nl.yellowbrick.data.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum PaymentMethod {

    VISA(601), MASTERCARD(603), DIRECT_DEBIT(602, 703), UNKNOWN;

    private final List<Integer> codes = new ArrayList<>();

    PaymentMethod(int... codes) {
        for(int code: codes) {
            this.codes.add(code);
        }
    }

    public static PaymentMethod forCode(int code) {
        return Arrays.asList(PaymentMethod.values()).stream().filter((payMethod) -> {
            return payMethod.codes.contains(code);
        }).findFirst().orElse(PaymentMethod.UNKNOWN);
    }
}
