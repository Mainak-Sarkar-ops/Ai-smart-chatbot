package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "messages")
public class Message {

    // =========================
    // PRIMARY KEY
    // =========================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =========================
    // MESSAGE CONTENT
    // =========================
    // Stores the actual message.
    // Example:
    // "Explain this PDF"
    // "This PDF contains information about..."
    
    @Column(columnDefinition = "TEXT")
    private String content;


    // =========================
    // MESSAGE SENDER
    // =========================
    // Stores who sent the message.
    // Possible values:
    // "USER"
    // "AI"

    @Column(nullable = false)
    private String sender;


    // =========================
    // MESSAGE TIMESTAMP
    // =========================

    private LocalDateTime createdAt;


    // =========================
    // CHAT RELATIONSHIP
    // =========================
    // Many messages belong to one chat.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "chat_id",
        nullable = false
    )
    private Chat chat;


    // =========================
    // DEFAULT CONSTRUCTOR
    // =========================

    public Message() {

        this.createdAt = LocalDateTime.now();

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
    // CONTENT GETTER AND SETTER
    // =========================

    public String getContent() {

        return content;

    }

    public void setContent(String content) {

        this.content = content;

    }


    // =========================
    // SENDER GETTER AND SETTER
    // =========================

    public String getSender() {

        return sender;

    }

    public void setSender(String sender) {

        this.sender = sender;

    }


    // =========================
    // CREATED AT GETTER AND SETTER
    // =========================

    public LocalDateTime getCreatedAt() {

        return createdAt;

    }

    public void setCreatedAt(
        LocalDateTime createdAt
    ) {

        this.createdAt = createdAt;

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