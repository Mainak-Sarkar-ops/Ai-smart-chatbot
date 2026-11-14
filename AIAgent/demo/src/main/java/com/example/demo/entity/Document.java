package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "documents")
public class Document {

    // =========================
    // PRIMARY KEY
    // =========================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =========================
    // FILE INFORMATION
    // =========================

    private String fileName;

    private String fileType;

    private Long fileSize;

    private LocalDateTime uploadedAt;


    // =========================
    // CHAT RELATIONSHIP
    // MANY DOCUMENTS -> ONE CHAT
    // =========================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id")
    private Chat chat;


    // =========================
    // CONSTRUCTOR
    // =========================

    public Document() {

        this.uploadedAt = LocalDateTime.now();

    }


    // =========================
    // ID GETTER AND SETTER
    // =========================

    public Long getId() {

        return id;

    }

    public void setId(Long id) {

        this.id = id;

    }


    // =========================
    // FILE NAME GETTER AND SETTER
    // =========================

    public String getFileName() {

        return fileName;

    }

    public void setFileName(String fileName) {

        this.fileName = fileName;

    }


    // =========================
    // FILE TYPE GETTER AND SETTER
    // =========================

    public String getFileType() {

        return fileType;

    }

    public void setFileType(String fileType) {

        this.fileType = fileType;

    }


    // =========================
    // FILE SIZE GETTER AND SETTER
    // =========================

    public Long getFileSize() {

        return fileSize;

    }

    public void setFileSize(Long fileSize) {

        this.fileSize = fileSize;

    }


    // =========================
    // UPLOADED AT GETTER AND SETTER
    // =========================

    public LocalDateTime getUploadedAt() {

        return uploadedAt;

    }

    public void setUploadedAt(LocalDateTime uploadedAt) {

        this.uploadedAt = uploadedAt;

    }


    // =========================
    // CHAT GETTER AND SETTER
    // =========================

    public Chat getChat() {

        return chat;

    }

    public void setChat(Chat chat) {

        this.chat = chat;

    }

}