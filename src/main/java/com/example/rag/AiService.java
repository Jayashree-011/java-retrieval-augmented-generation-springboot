package com.example.rag;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class AiService {

    private static final int EMBEDDING_DIMENSIONS = 768;

    private final RestClient gemini;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String embeddingModel;
    private final String chatModel;

    public AiService(
            RestClient.Builder builder,
            ObjectMapper objectMapper,
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.embedding-model}") String embeddingModel,
            @Value("${gemini.chat-model}") String chatModel
    ) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.embeddingModel = embeddingModel;
        this.chatModel = chatModel;
        this.gemini = builder.baseUrl("https://generativelanguage.googleapis.com/v1beta").build();
    }

    public float[] embed(String text) {
        return embed(text, "RETRIEVAL_QUERY");
    }

    public float[] embedDocument(String text) {
        return embed(text, "RETRIEVAL_DOCUMENT");
    }

    public String generateAnswer(String question, List<String> docs) {
        if (docs == null || docs.isEmpty()) {
            return "No matching documents were found. Upload a PDF to /api/documents first.";
        }
        String context = docs.stream().collect(Collectors.joining("\n\n"));
        String prompt = """
                Use only the documents below to answer the question.
                If the answer is not in the documents, say you do not know.

                Documents:
                %s

                Question: %s
                """.formatted(context, question);

        JsonNode root = post("/models/" + chatModel + ":generateContent", Map.of(
                "system_instruction", Map.of(
                        "parts", List.of(Map.of("text", "You are a helpful assistant that answers from retrieved documents."))
                ),
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                )
        ));

        StringBuilder answer = new StringBuilder();
        JsonNode parts = root.path("candidates").path(0).path("content").path("parts");
        if (parts.isArray()) {
            for (JsonNode part : parts) {
                answer.append(part.path("text").asText(""));
            }
        }
        if (answer.isEmpty()) {
            throw new IllegalStateException("Gemini did not return an answer: " + root);
        }
        return answer.toString().trim();
    }

    private float[] embed(String text, String taskType) {
        JsonNode root = post("/models/" + embeddingModel + ":embedContent", Map.of(
                "model", "models/" + embeddingModel,
                "content", Map.of("parts", List.of(Map.of("text", text))),
                "taskType", taskType,
                "outputDimensionality", EMBEDDING_DIMENSIONS
        ));
        JsonNode values = root.path("embedding").path("values");
        if (!values.isArray() || values.isEmpty()) {
            throw new IllegalStateException("Gemini did not return an embedding: " + root);
        }
        float[] embedding = new float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            embedding[i] = (float) values.get(i).asDouble();
        }
        return embedding;
    }

    private JsonNode post(String path, Map<String, Object> body) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Set GEMINI_API_KEY to use the Gemini API");
        }
        try {
            String response = gemini.post()
                    .uri(uriBuilder -> uriBuilder.path(path).queryParam("key", apiKey).build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);
            return objectMapper.readTree(response);
        } catch (Exception ex) {
            throw new IllegalStateException("Gemini request failed: " + path, ex);
        }
    }
}
