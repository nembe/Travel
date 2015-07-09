package nl.yellowbrick.admin.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import nl.yellowbrick.data.domain.CardOrder;
import nl.yellowbrick.data.domain.Customer;
import nl.yellowbrick.data.domain.CustomerAddress;

import static com.google.common.base.Strings.isNullOrEmpty;

@JsonPropertyOrder({
        "CUSTOMERID", "CUSTOMERNR", "GENDER", "HEADER", "HEADER1", "INITIALS", "INFIX", "LASTNAME", "BUSINESS",
        "BUSINESSNAME", "ATTN", "ADDRESS", "POBOX", "HOUSENR", "SUPPLEMENT", "ZIPCODE", "CITY", "COUNTRYNAME", "AANTAL"
})
public class SleeveOrderExportRecord {

    private static final String BUSINESS_ATTN = "T.a.v.";
    private static final String POBOX_PLACEHOLDER = "Postbus";

    private final CardOrder order;
    private final Customer customer;
    private final CustomerAddress address;
    private final String country;

    public SleeveOrderExportRecord(CardOrder order, Customer customer, CustomerAddress address, String country) {
        this.order = order;
        this.customer = customer;
        this.address = address;
        this.country = country;
    }

    @JsonIgnore
    public CardOrder getOrder() {
        return this.order;
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
        if(isNullOrEmpty(address.getPoBox()))
            return "";

        return POBOX_PLACEHOLDER + " " + address.getPoBox();
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

    @JsonProperty("AANTAL")
    public int amount() {
        return order.getAmount();
    }

    private static String removeLeadingDash(String text) {
        if(isNullOrEmpty(text))
            return "";

        return text.startsWith("-") ? text.replace("-", "") : text;
    }
}
