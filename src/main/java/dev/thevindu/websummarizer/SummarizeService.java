package dev.thevindu.websummarizer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class SummarizeService {
    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final WebClient webClient;

    public SummarizeService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public String processContent(SummarizeRequest request) {
        // PROMPT
        String prompt = buildPrompt(request);
        // QUERY THE AI MODEL
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[] {
                        Map.of("parts", new Object[] {
                                Map.of("text", prompt)
                        })
                }
        );

        // PARSE RESPONSE
        String response = webClient.post()
                .uri(geminiApiUrl + geminiApiUrl)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // RETURN RESPONSE
        return extractTextFromResponse(response);
    }

    private String extractTextFromResponse(String response) {
        try {

        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    private String buildPrompt(SummarizeRequest request) {
        StringBuilder prompt = new StringBuilder();
        switch (request.getOperation()) {
            case "summarize":
                prompt.append("Summarize the following text in 2–4 concise sentences. " +
                        "Retain only the essential information and key outcomes. " +
                        "Remove filler, examples, and subjective language. " +
                        "Maintain high information density: \n\n");
                break;
            case "suggest":
                prompt.append("Based on the following material, suggest closely related topics and " +
                        "targeted further reading. " +
                        "Focus on the most relevant conceptual extensions, advanced areas, " +
                        "and key sources that deepen understanding. " +
                        "Exclude generic or low-value recommendations: \n\n");
                break;
            default:
                throw new IllegalArgumentException("Unknown Operation: " + request.getOperation());
        }

        return prompt.append(request.getContent()).toString();
    }
}
