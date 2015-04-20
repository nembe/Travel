package nl.yellowbrick.admin.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import nl.yellowbrick.data.domain.*;

import static com.google.common.base.Strings.isNullOrEmpty;

@JsonPropertyOrder({
        "CARDTYPE", "CARDNR", "QPARKCARDNR", "CUSTOMERID", "CUSTOMERNR", "GENDER", "HEADER", "HEADER1",
        "INITIALS", "INFIX", "LASTNAME", "BUSINESS", "BUSINESSNAME", "ATTN", "ADDRESS", "POBOX",
        "HOUSENR", "SUPPLEMENT", "ZIPCODE", "CITY", "COUNTRYNAME", "BRIEFCODE", "TRACK2"
})
public class CardOrderExportRecord {

    private static final String BUSINESS_ATTN = "T.a.v";
    private static final String POBOX_PLACEHOLDER = "Postbus";

    private final CardOrder order;
    private final ProductGroup productGroup;
    private final Customer customer;
    private final CustomerAddress address;
    private final String locale;
    private final String country;
    private final String qparkCode;

    private CardOrderExportRecord(Builder builder) {
        this.order = builder.order;
        this.productGroup = builder.productGroup;
        this.customer = builder.customer;
        this.address = builder.address;
        this.locale = builder.locale;
        this.country = builder.country;
        this.qparkCode = builder.qparkCode;
    }

    @JsonIgnore
    public CardOrderExportTarget target() {
        if(order.getCardType().equals(CardType.SLEEVE) || order.getCardType().equals(CardType.UNKNOWN_CARD))
            return CardOrderExportTarget.OTHER;

        // external provisioning: orders go to a combined file
        if(!productGroup.isInternalCardProvisioning())
            return CardOrderExportTarget.TRANSPONDER_CARDS_FILE;

        if(order.getCardType().equals(CardType.TRANSPONDER_CARD))
            return CardOrderExportTarget.TRANSPONDER_CARDS_FILE;
        else if(order.getCardType().equals(CardType.QPARK_CARD))
            return CardOrderExportTarget.QPARK_CARDS_FILE;
        else if(order.getCardType().equals(CardType.RTP_CARD))
            return CardOrderExportTarget.RTP_CARDS_FILE;
        else
            return CardOrderExportTarget.OTHER;
    }

    @JsonIgnore
    public CardOrder getOrder() {
        return this.order;
    }

    @JsonProperty("CARDTYPE")
    public String getCardType() {
        if(order.getCardType().equals(CardType.RTP_CARD))
            return productGroup.getId().toString();
        else if(order.getCardType().equals(CardType.TRANSPONDER_CARD))
            return productGroup.getId().toString() + ".6";
        else
            return productGroup.getId().toString() + ".10";
    }

    @JsonProperty("CARDNR")
    public String getCardNr() {
        return order.getCardNumber();
    }

    @JsonProperty("QPARKCARDNR")
    public String qParkCardNr() {
        return qparkCode;
    }

    @JsonProperty("CUSTOMERID")
    public String customerId() {
        return String.valueOf(customer.getCustomerId());
    }

    @JsonProperty("CUSTOMERNR")
    public String customerNr() {
        return customer.getCustomerNr();
    }

    @JsonProperty("GENDER")
    public String gender() {
        return customer.getGender();
    }

    @JsonProperty("HEADER")
    public String header() {
        return "M".equalsIgnoreCase(gender()) ? "Dhr." : "Mevr.";
    }

    @JsonProperty("HEADER1")
    public String header1() {
        return "M".equalsIgnoreCase(gender()) ? "heer" : "mevrouw";
    }

    @JsonProperty("INITIALS")
    public String initials() {
        return removeLeadingDash(customer.getInitials());
    }

    @JsonProperty("INFIX")
    public String infix() {
        return removeLeadingDash(customer.getInfix());
    }

    @JsonProperty("LASTNAME")
    public String lastName() {
        return removeLeadingDash(customer.getLastName());
    }

    @JsonProperty("BUSINESS")
    public String business() {
        return customer.getBusiness();
    }

    @JsonProperty("BUSINESSNAME")
    public String businessName() {
        return customer.getBusinessName();
    }

    @JsonProperty("ATTN")
    public String attn() {
        return isNullOrEmpty(businessName()) ? "" : BUSINESS_ATTN;
    }

    @JsonProperty("ADDRESS")
    public String address() {
        return address.getAddress();
    }

    @JsonProperty("POBOX")
    public String poBox() {
        return isNullOrEmpty(address.getPoBox()) ? "" : POBOX_PLACEHOLDER;
    }

    @JsonProperty("HOUSENR")
    public String houseNr() {
        return address.getHouseNr();
    }

    @JsonProperty("SUPPLEMENT")
    public String supplement() {
        return removeLeadingDash(address.getSupplement());
    }

    @JsonProperty("ZIPCODE")
    public String zipCode() {
        return address.getZipCode();
    }

    @JsonProperty("CITY")
    public String city() {
        return address.getCity();
    }

    @JsonProperty("COUNTRYNAME")
    public String countryName() {
        return country;
    }

    @JsonProperty("BRIEFCODE")
    public String briefCode() {
        String langSuffix = isNullOrEmpty(locale) ? "" : "/".concat(locale);

        return order.getBriefCode() + langSuffix;
    }

    @JsonProperty("TRACK2")
    public String track2() {
        if(isNullOrEmpty(qparkCode))
            return "";

        return String.format("$$%s$$", new QParkTrackTwoCode(qparkCode));
    }

    private static String removeLeadingDash(String text) {
        if(isNullOrEmpty(text))
            return "";

        return text.startsWith("-") ? text.replace("-", "") : text;
    }

    public static class Builder {

        private CardOrder order;
        private ProductGroup productGroup;
        private Customer customer;
        private CustomerAddress address;
        private String locale;
        private String country;
        private String qparkCode;

        public Builder(CardOrder order) {
            this.order = order;
        }

        public Builder productGroup(ProductGroup pg) {
            this.productGroup = pg;
            return this;
        }

        public Builder customer(Customer c) {
            this.customer = c;
            return this;
        }

        public Builder address(CustomerAddress ca) {
            this.address = ca;
            return this;
        }

        public Builder locale(String locale) {
            this.locale = locale;
            return this;
        }

        public Builder country(String c) {
            this.country = c;
            return this;
        }

        public Builder qparkCode(String code) {
            this.qparkCode = code;
            return this;
        }

        public CardOrderExportRecord build() {
            return new CardOrderExportRecord(this);
        }
    }
}