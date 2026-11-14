package com.example.demo.controller;

import java.util.Base64;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.ImageGenerationService;

@RestController
@RequestMapping("/image")
public class ImageController {

private final ImageGenerationService imageGenerationService;

public ImageController(
        ImageGenerationService imageGenerationService
) {
    this.imageGenerationService = imageGenerationService;
}


// =========================================================
// GENERATE IMAGE
// =========================================================

@PostMapping(
        value = "/generate",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
)
public ResponseEntity<ImageResponse> generateImage(
        @RequestBody ImageRequest request
) {

    if (
            request == null ||
            request.getPrompt() == null ||
            request.getPrompt().trim().isEmpty()
    ) {

        return ResponseEntity.badRequest()
                .body(
                        new ImageResponse(
                                null,
                                "Image prompt cannot be empty."
                        )
                );
    }


    byte[] image =
            imageGenerationService.generateImage(
                    request.getPrompt()
            );


    String base64Image =
            Base64.getEncoder()
                    .encodeToString(image);


    return ResponseEntity.ok(
            new ImageResponse(
                    base64Image,
                    null
            )
    );
}


// =========================================================
// REQUEST CLASS
// =========================================================

public static class ImageRequest {

    private String prompt;

    public ImageRequest() {
    }

    public String getPrompt() {

        return prompt;

    }

    public void setPrompt(String prompt) {

        this.prompt = prompt;

    }

}


// =========================================================
// RESPONSE CLASS
// =========================================================

public static class ImageResponse {

    private String image;

    private String error;


    public ImageResponse() {
    }


    public ImageResponse(
            String image,
            String error
    ) {

        this.image = image;

        this.error = error;

    }


    public String getImage() {

        return image;

    }


    public void setImage(String image) {

        this.image = image;

    }


    public String getError() {

        return error;

    }


    public void setError(String error) {

        this.error = error;

    }

}


}
