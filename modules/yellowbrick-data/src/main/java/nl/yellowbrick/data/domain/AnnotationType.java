package nl.yellowbrick.data.domain;

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
}
