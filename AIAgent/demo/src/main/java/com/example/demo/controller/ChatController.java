package com.example.demo.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.ChatRequest;
import com.example.demo.dto.ChatResponse;
import com.example.demo.dto.ChatSummary;
import com.example.demo.dto.MessageResponse;
import com.example.demo.dto.RenameChatRequest;
import com.example.demo.service.ChatService;
import com.example.demo.service.ImageGenerationService;
import com.example.demo.service.PdfService;

@RestController
@RequestMapping("/")
@CrossOrigin(origins = "http://localhost:5173")
public class ChatController {

    private final ChatService chatService;

    private final PdfService pdfService;

    private final ImageGenerationService imageGenerationService;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public ChatController(
            ChatService chatService,
            PdfService pdfService,
            ImageGenerationService imageGenerationService
    ) {

        this.chatService = chatService;

        this.pdfService = pdfService;

        this.imageGenerationService =
                imageGenerationService;
    }


    // =========================================================
    // CHAT
    // =========================================================

    @PostMapping("/chat")
    public ChatResponse chat(
            @RequestParam(required = false) String prompt,
            @RequestParam(required = false) List<MultipartFile> files,
            @RequestParam(required = false) Long chatId
    ) throws IOException {

        ChatRequest request =
                new ChatRequest();


        // =====================================================
        // BASIC CHAT DATA
        // =====================================================

        request.setPrompt(prompt);

        request.setChatId(chatId);


        // =====================================================
        // MULTIPLE FILE SUPPORT
        // =====================================================

        if (
                files != null &&
                !files.isEmpty()
        ) {

            // -------------------------------------------------
            // LIST OF EXTRACTED PDF TEXTS
            // -------------------------------------------------

            List<String> pdfTexts =
                    new ArrayList<>();


            // -------------------------------------------------
            // LIST OF PDF FILE NAMES
            // -------------------------------------------------

            List<String> pdfFileNames =
                    new ArrayList<>();


            // -------------------------------------------------
            // PROCESS EVERY UPLOADED FILE
            // -------------------------------------------------

            for (MultipartFile file : files) {

                // Skip empty files
                if (
                        file == null ||
                        file.isEmpty()
                ) {

                    continue;
                }


                // Get file type
                String contentType =
                        file.getContentType();


                // =================================================
                // PDF FILE
                // =================================================

                if (
                        contentType != null &&
                        contentType.equalsIgnoreCase(
                                "application/pdf"
                        )
                ) {

                    // ---------------------------------------------
                    // EXTRACT PDF TEXT
                    // ---------------------------------------------

                    String extractedText =
                            pdfService.extractText(
                                    file
                            );


                    // ---------------------------------------------
                    // STORE PDF TEXT + FILE NAME
                    // ---------------------------------------------

                    if (
                            extractedText != null &&
                            !extractedText.isBlank()
                    ) {

                        pdfTexts.add(
                                extractedText
                        );

                        pdfFileNames.add(
                                file.getOriginalFilename()
                        );
                    }
                }


                // =================================================
                // IMAGE FILE
                // =================================================

                else if (
                        contentType != null &&
                        contentType.startsWith("image/")
                ) {

                    /*
                     * Keep the existing image upload
                     * functionality working.
                     *
                     * At the moment ChatRequest supports
                     * one image per request.
                     */

                    request.setImageData(
                            file.getBytes()
                    );

                    request.setImageMimeType(
                            contentType
                    );
                }
            }


            // =====================================================
            // SET MULTIPLE PDF DATA
            // =====================================================

            if (!pdfTexts.isEmpty()) {

                /*
                 * Store all extracted PDF texts.
                 */

                request.setPdfTexts(
                        pdfTexts
                );


                /*
                 * Store all original PDF file names.
                 */

                request.setPdfFileNames(
                        pdfFileNames
                );


                /*
                 * Keep the old single-PDF field populated.
                 *
                 * This keeps compatibility with the
                 * previous PDF implementation.
                 */

                request.setPdfText(
                        pdfTexts.get(0)
                );
            }
        }


        // =====================================================
        // SEND REQUEST TO CHAT SERVICE
        // =====================================================

        return chatService.chat(
                request
        );
    }


    // =========================================================
    // GET ALL CHATS
    // =========================================================

    @GetMapping("/chats")
    public List<ChatSummary> getAllChats() {

        return chatService.getAllChats();
    }


    // =========================================================
    // GET CHAT MESSAGES
    // =========================================================

    @GetMapping("/chat/{id}")
    public List<MessageResponse> getChatMessages(
            @PathVariable Long id
    ) {

        return chatService.getChatMessages(
                id
        );
    }


    // =========================================================
    // DELETE CHAT
    // =========================================================

    @DeleteMapping("/chats/{id}")
    public ResponseEntity<?> deleteChat(
            @PathVariable Long id
    ) {

        chatService.deleteChat(
                id
        );

        return ResponseEntity.ok(
                "Chat deleted successfully."
        );
    }


    // =========================================================
    // RENAME CHAT
    // =========================================================

    @PutMapping("/chats/{id}/title")
    public ResponseEntity<?> renameChat(
            @PathVariable Long id,
            @RequestBody RenameChatRequest request
    ) {

        chatService.renameChat(
                id,
                request.getTitle()
        );

        return ResponseEntity.ok(
                "Chat renamed successfully."
        );
    }


    // =========================================================
    // IMAGE GENERATION
    // =========================================================

    @PostMapping("/generate-image")
    public ResponseEntity<?> generateImage(
            @RequestParam String prompt
    ) {

        try {

            byte[] image =
                    imageGenerationService
                            .generateImage(
                                    prompt
                            );

            return ResponseEntity.ok(
                    image
            );

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body(
                            "Image generation failed: "
                            + e.getMessage()
                    );
        }
    }
}