package varna.mit.kln.unimart.chat.dto;

import jakarta.validation.constraints.NotBlank;

public class MessageRequestDto {

    @NotBlank(message = "Message text cannot be blank")
    private String messageText;

    public MessageRequestDto() {}

    public MessageRequestDto(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }
}
