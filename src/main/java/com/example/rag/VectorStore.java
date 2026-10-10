package com.example.rag;

import java.util.List;

public interface VectorStore {
    void store(String id, String content, float[] embedding);

    List<String> search(float[] queryEmbedding);
}
