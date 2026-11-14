package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Document;

public interface Documentrepository extends JpaRepository<Document, Long> {

}