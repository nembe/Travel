package nl.yellowbrick.data.domain;

import java.util.Arrays;

public enum CardStatus {
    ACTIVE(1), INACTIVE(2), INSTOCK(3), NONEXISTING(-1);

    private int code;

    private CardStatus(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static final CardStatus byCode(int code) {
        return Arrays.asList(CardStatus.values()).stream().filter((status) -> {
            return status.code() == code;
        }).findFirst().orElseThrow(() -> new IllegalArgumentException("unknown card status " + code));
    }
}
