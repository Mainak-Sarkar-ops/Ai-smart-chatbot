
package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Memory;
import com.example.demo.repository.Memoryrepository;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

@Service
public class MemoryService {

    private final Memoryrepository memoryRepository;
    private final Client client;

    @Value("${gemini.model}")
    private String model;

    public MemoryService(
            Memoryrepository memoryRepository,
            Client client
    ) {
        this.memoryRepository = memoryRepository;
        this.client = client;
    }

    public String getMemoryContext() {

        List<Memory> memories =
                memoryRepository
                        .findAllByOrderByUpdatedAtDesc();

        if (memories.isEmpty()) {
            return "No long-term memory is available.";
        }

        StringBuilder context =
                new StringBuilder();

        for (Memory memory : memories) {

            context.append("- ");

            context.append(
                    memory.getCategory()
            );

            context.append(": ");

            context.append(
                    memory.getMemoryKey()
            );

            context.append(" = ");

            context.append(
                    memory.getMemoryValue()
            );

            context.append("\n");
        }

        return context.toString();
    }

    public void extractAndSaveMemories(
            String userMessage,
            String aiResponse
    ) {

        if (
                userMessage == null ||
                userMessage.isBlank()
        ) {
            return;
        }

        String extractionPrompt =
                """
                You are the long-term memory manager
                of an AI assistant.

                Analyze the user's latest message.

                Identify only useful, stable facts that
                should be remembered across future chats.

                Good examples:
                - preferred programming language
                - education
                - career goals
                - long-term project
                - preferred response style
                - hobbies
                - stable technical preferences

                Do NOT store:
                - passwords
                - API keys
                - authentication information
                - financial account information
                - highly sensitive personal information
                - temporary questions
                - temporary emotions
                - one-time tasks
                - ordinary conversation

                Return ONLY lines in this exact format:

                CATEGORY|KEY|VALUE

                If there is nothing useful to remember,
                return:

                NONE

                User message:
                """
                + userMessage
                + """

                AI response:
                """
                + aiResponse;

        try {

            GenerateContentResponse response =
                    client.models.generateContent(
                            model,
                            extractionPrompt,
                            null
                    );

            String result =
                    response.text();

            if (
                    result == null ||
                    result.isBlank() ||
                    result.trim()
                            .equalsIgnoreCase("NONE")
            ) {
                return;
            }

            processMemoryLines(result);

        } catch (Exception e) {

            System.err.println(
                    "Memory extraction failed: "
                    + e.getMessage()
            );
        }
    }

    private void processMemoryLines(
            String result
    ) {

        String[] lines =
                result.split("\\r?\\n");

        for (String line : lines) {

            line = line.trim();

            if (
                    line.isEmpty() ||
                    line.equalsIgnoreCase("NONE")
            ) {
                continue;
            }

            String[] parts =
                    line.split("\\|", 3);

            if (parts.length != 3) {
                continue;
            }

            String category =
                    parts[0].trim();

            String key =
                    parts[1].trim();

            String value =
                    parts[2].trim();

            if (
                    category.isEmpty() ||
                    key.isEmpty() ||
                    value.isEmpty()
            ) {
                continue;
            }

            saveOrUpdateMemory(
                    category,
                    key,
                    value
            );
        }
    }

    @Transactional
    public void saveOrUpdateMemory(
            String category,
            String key,
            String value
    ) {

        Memory memory =
                memoryRepository
                        .findByCategoryAndMemoryKey(
                                category,
                                key
                        )
                        .orElseGet(
                                Memory::new
                        );

        memory.setCategory(category);

        memory.setMemoryKey(key);

        memory.setMemoryValue(value);

        memory.setUpdatedAt(
                LocalDateTime.now()
        );

        memoryRepository.save(memory);
    }

    public List<Memory> getAllMemories() {

        return memoryRepository
                .findAllByOrderByUpdatedAtDesc();
    }

    @Transactional
    public void deleteMemory(
            Long id
    ) {

        memoryRepository.deleteById(id);
    }

    @Transactional
    public void clearMemories() {

        memoryRepository.deleteAll();
    }
}

