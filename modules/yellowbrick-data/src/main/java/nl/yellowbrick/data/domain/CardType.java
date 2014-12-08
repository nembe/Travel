package nl.yellowbrick.data.domain;

public enum CardType {
    UNKNOWN_CARD(0), TRANSPONDER_CARD(1), RTP_CARD(2), QPARK_CARD(3), SLEEVE(4);

    private final int code;

    CardType(int code) {
        this.code = code;
    }

    public String code() {
        return code + "";
    }

    public static CardType fromDescription(String description) {
        String desc = description.toLowerCase();

        if(desc.startsWith("transponder")) return TRANSPONDER_CARD;
        if(desc.startsWith("rtp")) return RTP_CARD;
        if(desc.startsWith("qcard")) return QPARK_CARD;

        return UNKNOWN_CARD;
    }
}
