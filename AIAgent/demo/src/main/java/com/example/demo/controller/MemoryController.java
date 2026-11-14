package com.example.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.Memory;
import com.example.demo.service.MemoryService;

@RestController
@RequestMapping("/memories")
@CrossOrigin(origins = "http://localhost:5173")
public class MemoryController {

    private final MemoryService memoryService;

    public MemoryController(
            MemoryService memoryService
    ) {
        this.memoryService = memoryService;
    }

    @GetMapping
    public List<Memory> getMemories() {

        return memoryService.getAllMemories();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMemory(
            @PathVariable Long id
    ) {

        memoryService.deleteMemory(id);

        return ResponseEntity.ok(
                "Memory deleted successfully."
        );
    }

    @DeleteMapping
    public ResponseEntity<?> clearMemories() {

        memoryService.clearMemories();

        return ResponseEntity.ok(
                "All memories deleted successfully."
        );
    }
}