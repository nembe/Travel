package nl.yellowbrick.data.domain;

import java.util.Arrays;

public enum CardType {

    UNKNOWN_CARD(0, ""),
    TRANSPONDER_CARD(1, "Transponderkaart"),
    RTP_CARD(2, "RTP kaart"),
    QPARK_CARD(3, "QCARD"),
    SLEEVE(4, "Hoesje");

    private final int code;
    private final String description;

    CardType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public String code() {
        return code + "";
    }

    public String description() {
        return description;
    }

    public static CardType fromDescription(String description) {
        return Arrays.asList(CardType.values()).stream().filter((cardType) -> {
            return cardType.description().equalsIgnoreCase(description);
        }).findFirst().orElse(UNKNOWN_CARD);
    }
}
