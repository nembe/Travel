package nl.yellowbrick.dao;


public enum ConfigSection {
    ACTIVATION("ACT_CUST"), TAXAMETER("TXM");

    private String sectionKey;

    ConfigSection(String sectionKey) {
        this.sectionKey = sectionKey;
    }

    public String getKey() {
        return sectionKey;
    }
}
