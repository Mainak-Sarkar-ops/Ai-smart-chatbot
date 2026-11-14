package com.example.demo.dto;

public class ChatSummary {

    private Long id;
    private String title;

    public ChatSummary() {
    }

    public ChatSummary(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}