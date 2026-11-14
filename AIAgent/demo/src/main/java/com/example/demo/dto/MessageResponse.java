package com.example.demo.dto;

public class MessageResponse {

    private String sender;
    private String text;

    public MessageResponse() {
    }

    public MessageResponse(String sender, String text) {
        this.sender = sender;
        this.text = text;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}