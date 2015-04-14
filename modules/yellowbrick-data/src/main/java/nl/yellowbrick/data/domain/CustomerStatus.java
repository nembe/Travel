package nl.yellowbrick.data.domain;

import java.util.Arrays;

public enum CustomerStatus {

    ACTIVATION_FAILED(0), REGISTERED(1), ACTIVE(2), BLACKLISTED(3), UNREGISTERED(99), IRRECOVERABLE(98);

    private int code;

    private CustomerStatus(int c) {
        code = c;
    }

    public int code() {
        return code;
    }

    public static CustomerStatus byCode(int code) {
        return Arrays.asList(CustomerStatus.values()).stream().filter((status) -> {
            return status.code() == code;
        }).findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
