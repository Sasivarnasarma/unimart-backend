package varna.mit.kln.unimart.chat.dto;

import jakarta.validation.constraints.NotNull;

public class ConversationRequestDto {

    @NotNull(message = "Listing ID is required")
    private Integer listingId;

    public ConversationRequestDto() {}

    public ConversationRequestDto(Integer listingId) {
        this.listingId = listingId;
    }

    public Integer getListingId() {
        return listingId;
    }

    public void setListingId(Integer listingId) {
        this.listingId = listingId;
    }
}
