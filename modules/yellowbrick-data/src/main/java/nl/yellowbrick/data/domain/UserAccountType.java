package nl.yellowbrick.data.domain;

public enum UserAccountType {
    WEBSITE(0),
    APPLOGIN(1),
    PARTNER(2),
    RESTRICTED_SUBACCOUNT(3);

    int code;

    private UserAccountType(final int code) {
        this.code = code;
    }

    public int value() {
        return this.code;
    }
}
