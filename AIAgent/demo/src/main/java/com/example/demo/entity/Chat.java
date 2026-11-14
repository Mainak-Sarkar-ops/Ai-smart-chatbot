package com.example.demo.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "chats")
public class Chat {

    // =========================
    // PRIMARY KEY
    // =========================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =========================
    // CHAT INFORMATION
    // =========================

    private String title;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


    // =========================
    // USER RELATIONSHIP
    // MANY CHATS -> ONE USER
    // =========================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    // =========================
    // MESSAGES RELATIONSHIP
    // ONE CHAT -> MANY MESSAGES
    // =========================

    @OneToMany(
        mappedBy = "chat",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<Message> messages = new ArrayList<>();


    // =========================
    // DOCUMENTS RELATIONSHIP
    // ONE CHAT -> MANY DOCUMENTS
    // =========================

    @OneToMany(
        mappedBy = "chat",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<Document> documents = new ArrayList<>();


    // =========================
    // CONSTRUCTOR
    // =========================

    public Chat() {

        this.createdAt = LocalDateTime.now();

        this.updatedAt = LocalDateTime.now();

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
    // TITLE GETTER AND SETTER
    // =========================

    public String getTitle() {

        return title;

    }

    public void setTitle(String title) {

        this.title = title;

    }


    // =========================
    // CREATED AT GETTER AND SETTER
    // =========================

    public LocalDateTime getCreatedAt() {

        return createdAt;

    }

    public void setCreatedAt(LocalDateTime createdAt) {

        this.createdAt = createdAt;

    }


    // =========================
    // UPDATED AT GETTER AND SETTER
    // =========================

    public LocalDateTime getUpdatedAt() {

        return updatedAt;

    }

    public void setUpdatedAt(LocalDateTime updatedAt) {

        this.updatedAt = updatedAt;

    }


    // =========================
    // USER GETTER AND SETTER
    // =========================

    public User getUser() {

        return user;

    }

    public void setUser(User user) {

        this.user = user;

    }


    // =========================
    // MESSAGES GETTER AND SETTER
    // =========================

    public List<Message> getMessages() {

        return messages;

    }

    public void setMessages(List<Message> messages) {

        this.messages = messages;

    }


    // =========================
    // DOCUMENTS GETTER AND SETTER
    // =========================

    public List<Document> getDocuments() {

        return documents;

    }

    public void setDocuments(List<Document> documents) {

        this.documents = documents;

    }

}