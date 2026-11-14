package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.ChatRequest;
import com.example.demo.dto.ChatResponse;
import com.example.demo.dto.ChatSummary;
import com.example.demo.dto.MessageResponse;
import com.example.demo.entity.Chat;
import com.example.demo.entity.Document;
import com.example.demo.entity.Message;
import com.example.demo.repository.Chatrepository;
import com.example.demo.repository.Documentrepository;
import com.example.demo.repository.Messagerepository;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

import jakarta.annotation.PostConstruct;

@Service
public class ChatService {

    // =========================================================
    // FIELDS
    // =========================================================

    private final Client client;

    private final Chatrepository chatRepository;

    private final Messagerepository messageRepository;

    private final ImageGenerationService imageGenerationService;

    private final MemoryService memoryService;

    private final Documentrepository documentRepository;

    @Value("${gemini.model}")
    private String model;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public ChatService(
            Client client,
            Chatrepository chatRepository,
            Messagerepository messageRepository,
            ImageGenerationService imageGenerationService,
            MemoryService memoryService,
            Documentrepository documentRepository
    ) {

        this.client = client;

        this.chatRepository = chatRepository;

        this.messageRepository = messageRepository;

        this.imageGenerationService =
                imageGenerationService;

        this.memoryService =
                memoryService;

        this.documentRepository =
                documentRepository;
    }


    // =========================================================
    // INITIALIZATION
    // =========================================================

    @PostConstruct
    public void init() {

        System.out.println(
                "=================================="
        );

        System.out.println(
                "Using Gemini Chat Model: " + model
        );

        System.out.println(
                "Multiple PDF Upload: ENABLED"
        );

        System.out.println(
                "Long-Term Memory: ENABLED"
        );

        System.out.println(
                "=================================="
        );
    }


    // =========================================================
    // CREATE NEW CHAT
    // =========================================================

    private Chat createNewChat(
            String prompt
    ) {

        Chat chat = new Chat();

        String title = prompt;

        if (
                title == null ||
                title.isBlank()
        ) {

            title = "New Chat";
        }

        title = title.trim();

        if (title.length() > 40) {

            title =
                    title.substring(0, 40)
                            + "...";
        }

        chat.setTitle(title);

        return chatRepository.save(chat);
    }


    // =========================================================
    // BUILD MULTIPLE PDF CONTEXT
    // =========================================================

    private String buildMultiplePdfContext(
            List<String> pdfTexts
    ) {

        if (
                pdfTexts == null ||
                pdfTexts.isEmpty()
        ) {

            return "";
        }

        StringBuilder pdfContext =
                new StringBuilder();

        int documentNumber = 1;

        for (String pdfText : pdfTexts) {

            if (
                    pdfText == null ||
                    pdfText.isBlank()
            ) {

                continue;
            }

            pdfContext.append(
                    "\n\n"
            );

            pdfContext.append(
                    "==============================\n"
            );

            pdfContext.append(
                    "PDF DOCUMENT "
            );

            pdfContext.append(
                    documentNumber
            );

            pdfContext.append(
                    "\n"
            );

            pdfContext.append(
                    "==============================\n\n"
            );

            pdfContext.append(
                    pdfText
            );

            documentNumber++;
        }

        return pdfContext.toString();
    }


    // =========================================================
    // SAVE UPLOADED DOCUMENT
    // =========================================================

    private void saveUploadedDocument(
            Chat chat,
            String fileName,
            String fileType,
            long fileSize
    ) {

        if (
                fileName == null ||
                fileName.isBlank()
        ) {

            fileName = "Uploaded Document";
        }

        Document document =
                new Document();

        document.setChat(chat);

        document.setFileName(fileName);

        document.setFileType(fileType);

        document.setFileSize(fileSize);

        documentRepository.save(document);
    }


    // =========================================================
    // SAVE MESSAGE
    // =========================================================

    private void saveMessage(
            Chat chat,
            String content,
            String sender
    ) {

        Message message =
                new Message();

        message.setChat(chat);

        message.setContent(content);

        message.setSender(sender);

        messageRepository.save(message);
    }


    // =========================================================
    // BUILD CONVERSATION HISTORY
    // =========================================================

    private String buildConversationHistory(
            Chat chat
    ) {

        List<Message> messages =
                messageRepository
                        .findByChatIdOrderByCreatedAtAsc(
                                chat.getId()
                        );

        StringBuilder history =
                new StringBuilder();

        for (Message message : messages) {

            history.append(
                    message.getSender()
            );

            history.append(": ");

            history.append(
                    message.getContent()
            );

            history.append(
                    "\n\n"
            );
        }

        return history.toString();
    }


    // =========================================================
    // MAIN CHAT METHOD
    // =========================================================

    public ChatResponse chat(
            ChatRequest request
    ) {

        Chat chat;


        // =====================================================
        // LOAD EXISTING CHAT
        // =====================================================

        if (
                request.getChatId() != null
        ) {

            chat =
                    chatRepository
                            .findById(
                                    request.getChatId()
                            )
                            .orElseThrow(
                                    () ->
                                            new RuntimeException(
                                                    "Chat not found with ID: "
                                                            + request.getChatId()
                                            )
                            );

        }


        // =====================================================
        // CREATE NEW CHAT
        // =====================================================

        else {

            chat =
                    createNewChat(
                            request.getPrompt()
                    );
        }


        // =====================================================
        // GET PROMPT
        // =====================================================

        String prompt =
                request.getPrompt();

        if (prompt == null) {

            prompt = "";
        }


        // =====================================================
        // GET PREVIOUS CONVERSATION
        // =====================================================

        String previousHistory =
                buildConversationHistory(
                        chat
                );


        // =====================================================
        // GET LONG-TERM MEMORY
        // =====================================================

        String memoryContext =
                memoryService
                        .getMemoryContext();


        // =====================================================
        // SAVE USER MESSAGE
        // =====================================================

        saveMessage(
                chat,
                prompt,
                "USER"
        );


        // =====================================================
        // RESPONSE VARIABLE
        // =====================================================

        ChatResponse response;


        // =====================================================
        // MULTIPLE PDF CHAT
        // =====================================================

        if (
                request.getPdfTexts() != null &&
                !request.getPdfTexts().isEmpty()
        ) {

            // -------------------------------------------------
            // BUILD ALL PDF CONTENT
            // -------------------------------------------------

            String pdfContext =
                    buildMultiplePdfContext(
                            request.getPdfTexts()
                    );


            // -------------------------------------------------
            // SAVE EACH UPLOADED PDF
            // -------------------------------------------------

            if (
                    request.getPdfFileNames() != null &&
                    !request.getPdfFileNames().isEmpty()
            ) {

                for (
                        int i = 0;
                        i < request
                                .getPdfFileNames()
                                .size();
                        i++
                ) {

                    String fileName =
                            request
                                    .getPdfFileNames()
                                    .get(i);

                    saveUploadedDocument(
                            chat,
                            fileName,
                            "application/pdf",
                            0
                    );
                }
            }


            // -------------------------------------------------
            // BUILD GEMINI PROMPT
            // -------------------------------------------------

            String finalPrompt =

                    "You are Neon AI, "
                    + "an intelligent AI assistant.\n\n"

                    + "Long-Term User Memory:\n"
                    + "------------------------------\n"
                    + memoryContext
                    + "\n"
                    + "------------------------------\n\n"

                    + "Previous Conversation:\n"
                    + "------------------------------\n"
                    + previousHistory
                    + "\n"
                    + "------------------------------\n\n"

                    + "The user has uploaded "
                    + "multiple PDF documents.\n\n"

                    + "Uploaded PDF Documents:\n"

                    + pdfContext

                    + "\n\n"

                    + "Current User Question:\n"
                    + prompt

                    + "\n\n"

                    + "Instructions:\n"
                    + "1. Answer the user's current "
                    + "question using the uploaded "
                    + "PDF documents.\n"

                    + "2. Use previous conversation "
                    + "when relevant.\n"

                    + "3. Use long-term memory only "
                    + "when relevant.\n"

                    + "4. If information from multiple "
                    + "PDFs is relevant, combine or "
                    + "compare it clearly.\n"

                    + "5. Do not invent information "
                    + "that is not supported by the "
                    + "uploaded documents.\n"

                    + "6. If the answer cannot be found "
                    + "in the uploaded documents, "
                    + "clearly say that the information "
                    + "was not found in the documents.\n"

                    + "7. Answer naturally without "
                    + "mentioning internal memory "
                    + "instructions.";


            // -------------------------------------------------
            // GENERATE RESPONSE
            // -------------------------------------------------

            response =
                    generateTextResponse(
                            finalPrompt
                    );
        }


        // =====================================================
        // IMAGE CHAT
        // =====================================================

        else if (
                request.getImageData() != null &&
                request.getImageMimeType() != null
        ) {

            String conversationPrompt =

                    "You are Neon AI, "
                    + "an intelligent AI assistant.\n\n"

                    + "Long-Term User Memory:\n"
                    + "------------------------------\n"
                    + memoryContext
                    + "\n"
                    + "------------------------------\n\n"

                    + "Previous Conversation:\n\n"
                    + previousHistory

                    + "\n\n"

                    + "Current User Question:\n"
                    + prompt

                    + "\n\n"

                    + "Analyze the uploaded image "
                    + "carefully and answer the user's "
                    + "question using relevant memory "
                    + "and conversation context.";


            response =
                    generateImageResponse(
                            conversationPrompt,
                            request.getImageData(),
                            request.getImageMimeType()
                    );
        }


        // =====================================================
        // NORMAL TEXT CHAT
        // =====================================================

        else {

            String finalPrompt =

                    "You are Neon AI, "
                    + "an intelligent AI assistant.\n\n"

                    + "Long-Term User Memory:\n"
                    + "------------------------------\n"
                    + memoryContext
                    + "\n"
                    + "------------------------------\n\n"

                    + "Previous Conversation:\n\n"
                    + previousHistory

                    + "\n\n"

                    + "Current User Question:\n"
                    + prompt

                    + "\n\n"

                    + "Use long-term memory only when "
                    + "it is relevant to the current "
                    + "question. "

                    + "Do not mention that you received "
                    + "memory context unless the user "
                    + "asks. "

                    + "Answer naturally and use the "
                    + "previous conversation for context "
                    + "when relevant.";


            response =
                    generateTextResponse(
                            finalPrompt
                    );
        }


        // =====================================================
        // GET AI RESPONSE
        // =====================================================

        String aiResponse =
                response.getResponse();

        if (
                aiResponse == null ||
                aiResponse.isBlank()
        ) {

            aiResponse =
                    "Sorry, I could not generate "
                    + "a response.";
        }


        // =====================================================
        // SAVE AI RESPONSE
        // =====================================================

        saveMessage(
                chat,
                aiResponse,
                "AI"
        );


        // =====================================================
        // EXTRACT LONG-TERM MEMORY
        // =====================================================

        memoryService
                .extractAndSaveMemories(
                        prompt,
                        aiResponse
                );


        // =====================================================
        // UPDATE CHAT TIME
        // =====================================================

        chat.setUpdatedAt(
                LocalDateTime.now()
        );

        chatRepository.save(chat);


        // =====================================================
        // SEND CHAT ID
        // =====================================================

        response.setChatId(
                chat.getId()
        );


        return response;
    }


    // =========================================================
    // GENERATE TEXT RESPONSE
    // =========================================================

    private ChatResponse generateTextResponse(
            String prompt
    ) {

        for (
                int attempt = 1;
                attempt <= 3;
                attempt++
        ) {

            try {

                GenerateContentResponse response =
                        client.models.generateContent(
                                model,
                                prompt,
                                null
                        );

                String text =
                        response.text();

                if (
                        text == null ||
                        text.isBlank()
                ) {

                    return new ChatResponse(
                            "Sorry, Gemini returned "
                            + "an empty response."
                    );
                }

                return new ChatResponse(
                        text
                );

            } catch (Exception e) {

                e.printStackTrace();

                if (attempt == 3) {

                    return new ChatResponse(
                            "Error connecting to Gemini: "
                            + e.getMessage()
                    );
                }

                try {

                    Thread.sleep(2000);

                } catch (
                        InterruptedException ignored
                ) {

                    Thread
                            .currentThread()
                            .interrupt();

                    return new ChatResponse(
                            "Request interrupted."
                    );
                }
            }
        }

        return new ChatResponse(
                "Unknown Gemini error."
        );
    }


    // =========================================================
    // GENERATE IMAGE RESPONSE
    // =========================================================

    private ChatResponse generateImageResponse(
            String prompt,
            byte[] imageData,
            String mimeType
    ) {

        try {

            // -------------------------------------------------
            // TEXT PART
            // -------------------------------------------------

            Part textPart =
                    Part.fromText(
                            prompt
                    );


            // -------------------------------------------------
            // IMAGE PART
            // -------------------------------------------------

            Part imagePart =
                    Part.fromBytes(
                            imageData,
                            mimeType
                    );


            // -------------------------------------------------
            // COMBINE TEXT + IMAGE
            // -------------------------------------------------

            Content content =
                    Content.fromParts(
                            textPart,
                            imagePart
                    );


            // -------------------------------------------------
            // SEND TO GEMINI
            // -------------------------------------------------

            GenerateContentResponse response =
                    client.models.generateContent(
                            model,
                            content,
                            null
                    );

            String text =
                    response.text();

            if (
                    text == null ||
                    text.isBlank()
            ) {

                return new ChatResponse(
                        "Sorry, Gemini could not "
                        + "analyze the image."
                );
            }

            return new ChatResponse(
                    text
            );

        } catch (Exception e) {

            e.printStackTrace();

            return new ChatResponse(
                    "Error analyzing image: "
                    + e.getMessage()
            );
        }
    }


    // =========================================================
    // GET ALL CHATS
    // =========================================================

    public List<ChatSummary> getAllChats() {

        return chatRepository
                .findAllByOrderByUpdatedAtDesc()

                .stream()

                .map(
                        chat ->
                                new ChatSummary(
                                        chat.getId(),
                                        chat.getTitle()
                                )
                )

                .collect(
                        Collectors.toList()
                );
    }


    // =========================================================
    // GET CHAT MESSAGES
    // =========================================================

    public List<MessageResponse> getChatMessages(
            Long chatId
    ) {

        return messageRepository

                .findByChatIdOrderByCreatedAtAsc(
                        chatId
                )

                .stream()

                .map(
                        message ->
                                new MessageResponse(
                                        message.getSender(),
                                        message.getContent()
                                )
                )

                .collect(
                        Collectors.toList()
                );
    }


    // =========================================================
    // RENAME CHAT
    // =========================================================

    @Transactional
    public void renameChat(
            Long chatId,
            String newTitle
    ) {

        Chat chat =
                chatRepository
                        .findById(chatId)
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "Chat not found with ID: "
                                                        + chatId
                                        )
                        );


        // -----------------------------------------------------
        // CHECK TITLE
        // -----------------------------------------------------

        if (
                newTitle == null ||
                newTitle.trim().isEmpty()
        ) {

            throw new RuntimeException(
                    "Chat name cannot be empty."
            );
        }


        newTitle =
                newTitle.trim();


        // -----------------------------------------------------
        // LIMIT TITLE
        // -----------------------------------------------------

        if (newTitle.length() > 40) {

            newTitle =
                    newTitle.substring(0, 40)
                            + "...";
        }


        // -----------------------------------------------------
        // UPDATE TITLE
        // -----------------------------------------------------

        chat.setTitle(
                newTitle
        );

        chat.setUpdatedAt(
                LocalDateTime.now()
        );

        chatRepository.save(
                chat
        );
    }


    // =========================================================
    // DELETE CHAT
    // =========================================================

    @Transactional
    public void deleteChat(
            Long chatId
    ) {

        // -----------------------------------------------------
        // DELETE ALL MESSAGES
        // -----------------------------------------------------

        messageRepository.deleteByChat_Id(
                chatId
        );


        // -----------------------------------------------------
        // DELETE CHAT
        // -----------------------------------------------------

        chatRepository.deleteById(
                chatId
        );
    }
}