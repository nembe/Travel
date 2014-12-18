package nl.yellowbrick.data.domain;

public enum AddressType {
    MAIN(1), BILLING(2);

    private int code;

    AddressType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
