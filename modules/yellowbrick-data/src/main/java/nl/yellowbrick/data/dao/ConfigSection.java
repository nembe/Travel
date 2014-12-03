package nl.yellowbrick.data.dao;


public enum ConfigSection {
    ACTIVATION("ACT_CUST"), TAXAMETER("TXM"), YB("YB");

    private String sectionKey;

    ConfigSection(String sectionKey) {
        this.sectionKey = sectionKey;
    }

    public String getKey() {
        return sectionKey;
    }
}
