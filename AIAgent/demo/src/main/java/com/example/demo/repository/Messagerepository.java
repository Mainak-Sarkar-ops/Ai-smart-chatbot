package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Message;

public interface Messagerepository
        extends JpaRepository<Message, Long> {

    List<Message>
    findByChatIdOrderByCreatedAtAsc(
            Long chatId
    );


    @Transactional
    void deleteByChat_Id(
            Long chatId
    );

}