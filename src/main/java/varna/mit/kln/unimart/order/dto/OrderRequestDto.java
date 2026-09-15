package varna.mit.kln.unimart.order.dto;

import jakarta.validation.constraints.NotNull;

public class OrderRequestDto {

    @NotNull(message = "Listing ID is required")
    private Integer listingId;

    private String paymentMethod = "CARD";

    public OrderRequestDto() {}

    public OrderRequestDto(Integer listingId, String paymentMethod) {
        this.listingId = listingId;
        this.paymentMethod = paymentMethod != null ? paymentMethod : "CARD";
    }

    public Integer getListingId() {
        return listingId;
    }

    public void setListingId(Integer listingId) {
        this.listingId = listingId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
