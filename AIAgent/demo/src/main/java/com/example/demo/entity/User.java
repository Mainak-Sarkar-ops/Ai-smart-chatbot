package com.example.demo.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    // =========================
    // PRIMARY KEY
    // =========================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =========================
    // USER INFORMATION
    // =========================

    private String name;

    private String email;


    // =========================
    // CHAT RELATIONSHIP
    // ONE USER -> MANY CHATS
    // =========================

    @OneToMany(
        mappedBy = "user",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<Chat> chats = new ArrayList<>();


    // =========================
    // CONSTRUCTOR
    // =========================

    public User() {
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
    // NAME GETTER AND SETTER
    // =========================

    public String getName() {

        return name;

    }

    public void setName(String name) {

        this.name = name;

    }


    // =========================
    // EMAIL GETTER AND SETTER
    // =========================

    public String getEmail() {

        return email;

    }

    public void setEmail(String email) {

        this.email = email;

    }


    // =========================
    // CHAT GETTER
    // =========================

    public List<Chat> getChats() {

        return chats;

    }


    // =========================
    // CHAT SETTER
    // =========================

    public void setChats(List<Chat> chats) {

        this.chats = chats;

    }


    // =========================
    // ADD CHAT
    // =========================
    // This helper method maintains
    // the relationship between
    // User and Chat.

    public void addChat(Chat chat) {

        chats.add(chat);

        chat.setUser(this);

    }


    // =========================
    // REMOVE CHAT
    // =========================

    public void removeChat(Chat chat) {

        chats.remove(chat);

        chat.setUser(null);

    }

}