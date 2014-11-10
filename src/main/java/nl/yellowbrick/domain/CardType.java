package nl.yellowbrick.domain;

public enum CardType {
    UNKNOWN_CARD(0), TRANSPONDER_CARD(1), RTP_CARD(2), QPARK_CARD(3), SLEEVE(4);

    private final int code;

    CardType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
