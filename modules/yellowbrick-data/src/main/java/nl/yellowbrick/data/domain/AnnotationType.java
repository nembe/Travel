package nl.yellowbrick.data.domain;

import java.util.Arrays;

public enum AnnotationType {

    PHONE("lblPhone", "TEL"),
    TRANSPONDER_CARD("lblTransponderCard", "TRK"),
    P_PLUS_PASS("lblPPass", "PPS"),
    TRANSACTION("lblTransaction", "TRA"),
    CUSTOMER("none", "CUS");

    private final String labelName;
    private final String code;

    private AnnotationType(String name, String code) {
        this.labelName = name;
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public String getLabelName() {
        return labelName;
    }

    public static AnnotationType byCode(String code) {
        return Arrays.asList(AnnotationType.values()).stream().filter((type) -> {
            return type.getCode().equalsIgnoreCase(code);
        }).findFirst().get();
    }
}
