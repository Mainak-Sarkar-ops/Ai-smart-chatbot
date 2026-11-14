package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Chat;

public interface Chatrepository extends JpaRepository<Chat, Long> {

    List<Chat> findAllByOrderByUpdatedAtDesc();

}