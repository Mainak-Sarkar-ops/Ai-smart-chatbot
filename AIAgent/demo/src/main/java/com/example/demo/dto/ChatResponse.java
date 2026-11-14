package com.example.demo.dto;

public class ChatResponse {

    private String response;

    private Long chatId;

    private String image;


    // ==========================================
    // DEFAULT CONSTRUCTOR
    // ==========================================

    public ChatResponse() {

    }


    // ==========================================
    // RESPONSE CONSTRUCTOR
    // ==========================================

    public ChatResponse(String response) {

        this.response = response;

    }


    // ==========================================
    // RESPONSE + CHAT ID CONSTRUCTOR
    // ==========================================

    public ChatResponse(
            String response,
            Long chatId
    ) {

        this.response = response;

        this.chatId = chatId;

    }


    // ==========================================
    // RESPONSE
    // ==========================================

    public String getResponse() {

        return response;

    }


    public void setResponse(String response) {

        this.response = response;

    }


    // ==========================================
    // CHAT ID
    // ==========================================

    public Long getChatId() {

        return chatId;

    }


    public void setChatId(Long chatId) {

        this.chatId = chatId;

    }


    // ==========================================
    // IMAGE
    // ==========================================

    public String getImage() {

        return image;

    }


    public void setImage(String image) {

        this.image = image;

    }

}