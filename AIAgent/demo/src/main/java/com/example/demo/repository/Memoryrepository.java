package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Memory;

public interface Memoryrepository
        extends JpaRepository<Memory, Long> {

    List<Memory> findAllByOrderByUpdatedAtDesc();

    Optional<Memory> findByCategoryAndMemoryKey(
            String category,
            String memoryKey
    );

}