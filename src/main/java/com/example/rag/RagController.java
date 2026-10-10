package com.example.rag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class RagController {

    @Autowired
    private RagService ragService;

    @Autowired
    private DocumentLoader documentLoader;

    @PostMapping("/chat")
    public ResponseEntity<String> ask(@RequestBody String question) {
        String answer = ragService.getAnswer(question.replace("\"", "").trim());
        return ResponseEntity.ok(answer);
    }

    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Upload a non-empty PDF file");
        }
        documentLoader.loadPdf(file.getInputStream());
        return ResponseEntity.ok("Indexed " + file.getOriginalFilename());
    }
}
