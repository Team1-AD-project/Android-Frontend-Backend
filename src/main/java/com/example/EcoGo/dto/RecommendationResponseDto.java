package com.example.EcoGo.dto;

public class RecommendationResponseDto {
    private String text;
    private String tag;

    public RecommendationResponseDto() {}

    public RecommendationResponseDto(String text, String tag) {
        this.text = text;
        this.tag = tag;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
