package com.example.rag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RagService {

    @Autowired
    private AiService aiService;

    @Autowired
    private VectorStore vectorStore;

    public String getAnswer(String question) {
        float[] embedding = aiService.embed(question);
        List<String> docs = vectorStore.search(embedding);
        return aiService.generateAnswer(question, docs);
    }
}
