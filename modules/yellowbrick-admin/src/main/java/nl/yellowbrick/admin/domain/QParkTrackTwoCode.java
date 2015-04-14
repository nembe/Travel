package nl.yellowbrick.admin.domain;

import nl.yellowbrick.admin.util.LuhnUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class QParkTrackTwoCode {

    private static final String TR2_START_SENTINEL = ";";
    private static final String TR2_ISO            = "600791";
    private static final String TR2_TRADEGROUP     = "37";
    private static final String TR2_MERCHANTNR     = "0600";
    private static final String TR2_SEPARATOR      = "=";
    private static final String TR2_SERVICE_CODE   = "501";
    private static final String TR2_PIN_OFFSET     = "0000";
    private static final String TR2_END_SENTINEL   = "?";

    private final String t2code;

    public QParkTrackTwoCode(String qparkCode) {
        if(qparkCode.length() != 6)
            throw new IllegalArgumentException("qpark code of unexpected length: " + qparkCode);
        
        this.t2code = determineTrackTwoCode(qparkCode); 
    }

    private static String determineTrackTwoCode(String qparkCode) {
        String expirationDate = LocalDateTime.now().plusYears(10).format(DateTimeFormatter.ofPattern("yyMM"));
        String pan = TR2_ISO + TR2_TRADEGROUP + TR2_MERCHANTNR + qparkCode;
        String checksum = String.valueOf(LuhnUtil.calculateLuhnCode(pan));

        return TR2_START_SENTINEL
                .concat(String.valueOf(checksum))
                .concat(TR2_SEPARATOR)
                .concat(expirationDate)
                .concat(TR2_SERVICE_CODE)
                .concat(TR2_PIN_OFFSET)
                .concat(qparkCode)
                .concat(TR2_END_SENTINEL);
    }

    @Override
    public String toString() {
        return t2code;
    }
}
