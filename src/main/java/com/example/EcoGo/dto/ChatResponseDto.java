package com.example.EcoGo.dto;

public class ChatResponseDto {
    private String reply;

    public ChatResponseDto() {}

    public ChatResponseDto(String reply) {
        this.reply = reply;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }
}
