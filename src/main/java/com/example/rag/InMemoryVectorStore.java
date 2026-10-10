package com.example.rag;

import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class InMemoryVectorStore implements VectorStore {

    private record Entry(String id, String content, float[] embedding) {
    }

    private final List<Entry> documents = new CopyOnWriteArrayList<>();

    @Override
    public void store(String id, String content, float[] embedding) {
        documents.add(new Entry(id, content, embedding));
    }

    @Override
    public List<String> search(float[] queryEmbedding) {
        return documents.stream()
                .sorted(Comparator.comparingDouble((Entry entry) -> cosineSimilarity(queryEmbedding, entry.embedding())).reversed())
                .limit(3)
                .map(Entry::content)
                .toList();
    }

    private double cosineSimilarity(float[] a, float[] b) {
        double dot = 0;
        double normA = 0;
        double normB = 0;
        int length = Math.min(a.length, b.length);
        for (int i = 0; i < length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) {
            return 0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
