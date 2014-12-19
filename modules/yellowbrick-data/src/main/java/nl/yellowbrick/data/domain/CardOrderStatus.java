package nl.yellowbrick.data.domain;

import java.util.Arrays;

public enum CardOrderStatus {

    INSERTED(1), ACCEPTED(2), EXPORTED(3);

    private final int code;

    CardOrderStatus(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static final CardOrderStatus byCode(int code) {
        return Arrays.asList(CardOrderStatus.values()).stream().filter((status) -> {
            return status.code() == code;
        }).findFirst().orElseThrow(() -> new IllegalArgumentException("unknown card order status " + code));
    }
}
