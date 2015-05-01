package nl.yellowbrick.admin.form;


import nl.yellowbrick.data.domain.CardOrder;

public class CardOrderValidationForm {

    private int amount;
    private double pricePerCard;
    private boolean export;

    public CardOrderValidationForm() {
    }

    public CardOrderValidationForm(CardOrder cardOrder) {
        this.amount = cardOrder.getAmount();
        this.pricePerCard = cardOrder.getPricePerCard() / 100;
        this.export = cardOrder.isExport();
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public double getPricePerCard() {
        return pricePerCard;
    }
    public double getPricePerCardCents() {
        return pricePerCard * 100;
    }

    public void setPricePerCard(double pricePerCard) {
        this.pricePerCard = pricePerCard;
    }

    public boolean isExport() {
        return export;
    }

    public void setExport(boolean export) {
        this.export = export;
    }
}
