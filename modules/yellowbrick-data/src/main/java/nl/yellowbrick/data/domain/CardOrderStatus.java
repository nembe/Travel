package nl.yellowbrick.data.domain;

public enum CardOrderStatus {

    INSERTED(1), ACCEPTED(2), EXPORTED(3);

    private final int code;

    CardOrderStatus(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
