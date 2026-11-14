package com.example.demo.dto;

import java.util.List;

public class ChatRequest {

    private String prompt;

    // Existing single PDF support
    private String pdfText;

    // New multiple PDF support
    private List<String> pdfTexts;

    private List<String> pdfFileNames;

    private byte[] imageData;

    private String imageMimeType;

    private Long chatId;

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getPdfText() {
        return pdfText;
    }

    public void setPdfText(String pdfText) {
        this.pdfText = pdfText;
    }

    public List<String> getPdfTexts() {
        return pdfTexts;
    }

    public void setPdfTexts(List<String> pdfTexts) {
        this.pdfTexts = pdfTexts;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public String getImageMimeType() {
        return imageMimeType;
    }

    public void setImageMimeType(String imageMimeType) {
        this.imageMimeType = imageMimeType;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public List<String> getPdfFileNames() {
    return pdfFileNames;
}

public void setPdfFileNames(List<String> pdfFileNames) {
    this.pdfFileNames = pdfFileNames;
}
}