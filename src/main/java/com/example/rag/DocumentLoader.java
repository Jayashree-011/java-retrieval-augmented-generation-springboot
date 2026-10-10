package com.example.rag;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class DocumentLoader {

    private static final int CHUNK_SIZE = 800;
    private static final int CHUNK_OVERLAP = 150;

    @Autowired
    private AiService aiService;

    @Autowired
    private VectorStore vectorStore;

    public void loadPdf(File pdfFile) throws Exception {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            indexText(extractText(document));
        }
    }

    public void loadPdf(InputStream inputStream) throws Exception {
        try (PDDocument document = PDDocument.load(inputStream)) {
            indexText(extractText(document));
        }
    }

    private String extractText(PDDocument document) throws Exception {
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(document);
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("The PDF did not contain extractable text");
        }
        return text;
    }

    private void indexText(String text) {
        for (String chunk : chunk(text)) {
            float[] embedding = aiService.embedDocument(chunk);
            vectorStore.store(UUID.randomUUID().toString(), chunk, embedding);
        }
    }

    private List<String> chunk(String text) {
        String normalized = text.replaceAll("\\s+", " ").trim();
        List<String> chunks = new ArrayList<>();
        if (normalized.length() <= CHUNK_SIZE) {
            chunks.add(normalized);
            return chunks;
        }
        int start = 0;
        while (start < normalized.length()) {
            int end = Math.min(start + CHUNK_SIZE, normalized.length());
            chunks.add(normalized.substring(start, end));
            if (end == normalized.length()) {
                break;
            }
            start = end - CHUNK_OVERLAP;
        }
        return chunks;
    }
}
