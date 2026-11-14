
package com.example.demo.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ImageGenerationService {

    @Value("${huggingface.api.key}")
    private String huggingFaceApiKey;

    @Value("${huggingface.image.model}")
    private String imageModel;

    @Value("${huggingface.provider}")
    private String provider;

    private final HttpClient httpClient;

    public ImageGenerationService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    // =========================================================
    // GENERATE IMAGE
    // =========================================================

    public byte[] generateImage(String prompt) {

        

    System.out.println("🔥🔥🔥 NEW IMAGE GENERATION SERVICE CODE IS RUNNING 🔥🔥🔥");

    if (prompt == null || prompt.trim().isEmpty()) {
        throw new RuntimeException("Image prompt cannot be empty.");
    }

    // rest of your code...

        if (prompt == null || prompt.trim().isEmpty()) {
            throw new RuntimeException("Image prompt cannot be empty.");
        }

        if (huggingFaceApiKey == null ||
                huggingFaceApiKey.trim().isEmpty()) {

            throw new RuntimeException(
                    "Hugging Face API key is missing."
            );
        }

        try {

            String cleanPrompt = prompt.trim();

            System.out.println();
            System.out.println("========================================");
            System.out.println("HUGGING FACE IMAGE GENERATION");
            System.out.println("========================================");

            System.out.println("MODEL = " + imageModel);
            System.out.println("PROVIDER = " + provider);
            System.out.println("PROMPT = " + cleanPrompt);

            // =====================================================
            // HUGGING FACE ROUTER
            // =====================================================

            String url =
                    "https://router.huggingface.co/"
                    + "fal-ai/v1/images/generations";

            System.out.println("REQUEST URL = " + url);

            // =====================================================
            // JSON BODY
            // =====================================================

            String jsonBody =
                    "{"
                    + "\"model\":\""
                    + escapeJson(imageModel)
                    + "\","
                    + "\"prompt\":\""
                    + escapeJson(cleanPrompt)
                    + "\""
                    + "}";

            System.out.println(
                    "REQUEST BODY = " + jsonBody
            );

            // =====================================================
            // HTTP REQUEST
            // =====================================================

            HttpRequest request =
                    HttpRequest.newBuilder()

                            .uri(
                                    URI.create(url)
                            )

                            .header(
                                    "Authorization",
                                    "Bearer "
                                    + huggingFaceApiKey
                            )

                            .header(
                                    "Content-Type",
                                    "application/json"
                            )

                            .header(
                                    "Accept",
                                    "image/*"
                            )

                            .POST(
                                    HttpRequest
                                            .BodyPublishers
                                            .ofString(
                                                    jsonBody,
                                                    StandardCharsets.UTF_8
                                            )
                            )

                            .build();

            // =====================================================
            // SEND REQUEST
            // =====================================================

            HttpResponse<byte[]> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofByteArray()
                    );

            int status =
                    response.statusCode();

            String contentType =
                    response.headers()
                            .firstValue("content-type")
                            .orElse("");

            System.out.println();
            System.out.println(
                    "HUGGING FACE STATUS = "
                    + status
            );

            System.out.println(
                    "CONTENT TYPE = "
                    + contentType
            );

            // =====================================================
            // ERROR
            // =====================================================

            if (status < 200 || status >= 300) {

                String error =
                        new String(
                                response.body(),
                                StandardCharsets.UTF_8
                        );

                System.err.println();
                System.err.println(
                        "========================================"
                );

                System.err.println(
                        "HUGGING FACE ERROR"
                );

                System.err.println(
                        "STATUS = " + status
                );

                System.err.println(
                        "ERROR = " + error
                );

                System.err.println(
                        "========================================"
                );

                throw new RuntimeException(
                        "Hugging Face API error "
                        + status
                        + ": "
                        + error
                );
            }

            // =====================================================
            // IMAGE RESPONSE
            // =====================================================

            if (
                    contentType
                            .toLowerCase()
                            .startsWith("image/")
            ) {

                System.out.println();
                System.out.println(
                        "========================================"
                );

                System.out.println(
                        "IMAGE GENERATED SUCCESSFULLY!"
                );

                System.out.println(
                        "IMAGE SIZE = "
                        + response.body().length
                        + " bytes"
                );

                System.out.println(
                        "========================================"
                );

                return response.body();
            }

            // =====================================================
            // UNEXPECTED RESPONSE
            // =====================================================

            String responseBody =
                    new String(
                            response.body(),
                            StandardCharsets.UTF_8
                    );

            System.err.println(
                    "UNEXPECTED RESPONSE:"
            );

            System.err.println(
                    responseBody
            );

            throw new RuntimeException(
                    "Hugging Face did not return an image. "
                    + "Content-Type: "
                    + contentType
                    + " Response: "
                    + responseBody
            );

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Image generation failed: "
                    + e.getMessage(),
                    e
            );
        }
    }

    // =========================================================
    // ESCAPE JSON
    // =========================================================

    private String escapeJson(String text) {

        return text
                .replace(
                        "\\",
                        "\\\\"
                )
                .replace(
                        "\"",
                        "\\\""
                )
                .replace(
                        "\n",
                        "\\n"
                )
                .replace(
                        "\r",
                        "\\r"
                )
                .replace(
                        "\t",
                        "\\t"
                );
    }
}

