package dev.thevindu.readless;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class ReadlessService {
    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public ReadlessService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        // Preconfigure WebClient with JSON content type; reuse the builder for connection pooling
        this.webClient = webClientBuilder
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.objectMapper = objectMapper;
    }

    public String processContent(ReadlessRequest request) {
        // 1) Build a strict, model-friendly prompt based on the requested operation
        String prompt = buildPrompt(request);

        // 2) Gemini request payload; include role for clearer turn-taking semantics
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "role", "user",
                                "parts", List.of(Map.of("text", prompt))
                        )
                )
        );

        // 3) Call model; bubble up error body so you can see exact server-side reason if it fails
        String response = webClient.post()
                .uri(geminiApiUrl + geminiApiKey)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, cr ->
                        cr.bodyToMono(String.class)
                                .flatMap(err -> Mono.error(new RuntimeException("Gemini error: " + err))))
                .bodyToMono(String.class)
                .block();

        // 4) Extract first candidate's first part text; tolerate missing/extra fields
        return extractTextFromResponse(response);
    }

    private String extractTextFromResponse(String responseJson) {
        try {
            ResponseGemini g = objectMapper.readValue(responseJson, ResponseGemini.class);

            if (g.getCandidates() != null && !g.getCandidates().isEmpty()) {
                ResponseGemini.Candidate c0 = g.getCandidates().get(0);
                if (c0.getContent() != null &&
                        c0.getContent().getParts() != null &&
                        !c0.getContent().getParts().isEmpty()) {
                    String text = c0.getContent().getParts().get(0).getText();
                    return text != null ? text : "";
                }
            }
            return "";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    /**
     * Operation catalog (string-based to keep your existing DTO):
     *
     *  summarize               → 2–4 sentence dense summary.
     *  suggest                 → related topics and further reading.
     *  bullets                 → 5–8 bullet summary, action-focused.
     *  outline                 → hierarchical outline with numbered sections.
     *  extractive              → top 3–5 verbatim quotes (<=120 chars each) that capture core points.
     *  keywords                → 10–15 comma-separated keywords; no sentences.
     *  tldr                    → single-sentence TL;DR (<=25 words).
     *  title                   → 3 super-concise titles; no subtitles.
     *  qa                      → 5 Q&A pairs derived from the text.
     *  expand                  → expand to 1–2 paragraphs, preserve factual content.
     *  shorten                 → reduce length by ~50% while keeping core meaning.
     *  rewrite:formal          → rewrite with formal, objective tone.
     *  rewrite:simple          → rewrite for grade-6 reading level.
     *  translate:<lang>        → translate to ISO code or name after colon, e.g., translate:si, translate:Spanish.
     *  detect-language         → return only the detected language name and ISO-639-1 code.
     */
    private String buildPrompt(ReadlessRequest request) {
        String op = request.getOperation() == null ? "summarize" : request.getOperation().trim().toLowerCase();
        String content = request.getContent() == null ? "" : request.getContent();

        // Handle parameterized ops like translate:<lang> or rewrite:<style>
        if (op.startsWith("translate:")) {
            String target = op.substring("translate:".length()).trim();
            return """
                    Translate the text into the target language exactly, preserving meaning and names.
                    Output only the translation; no preface, no notes.
                    Target language: %s

                    ---
                    %s
                    """.formatted(target, content);
        }

        if (op.startsWith("rewrite:")) {
            String mode = op.substring("rewrite:".length()).trim();
            if (mode.equals("formal")) {
                return """
                        Rewrite the text in a formal, objective, concise tone suitable for academic or official documents.
                        Remove slang, hedging, and verbosity. Output only the rewritten text.

                        ---
                        %s
                        """.formatted(content);
            }
            if (mode.equals("simple")) {
                return """
                        Rewrite the text for a grade-6 reading level.
                        Use short sentences, common words, and direct structure. Output only the rewritten text.

                        ---
                        %s
                        """.formatted(content);
            }
        }

        // Fixed-operation prompts
        return switch (op) {
            case "summarize" -> """
                    Summarize the following text in 2–4 concise sentences.
                    Keep only essential facts, results, and implications.
                    Remove filler, anecdotes, and subjective language.

                    ---
                    %s
                    """.formatted(content);

            case "suggest" -> """
                    Based on the material, list closely related topics and targeted further reading.
                    Focus on advanced areas and canonical sources. Avoid generic, low-value items.
                    Output as a short bulleted list.

                    ---
                    %s
                    """.formatted(content);

            case "bullets" -> """
                    Produce 5–8 crisp bullets capturing the core ideas and actions.
                    Each bullet <= 20 words. No preface, no summary line.

                    ---
                    %s
                    """.formatted(content);

            case "outline" -> """
                    Create a hierarchical outline with numbered headings and subpoints (max depth 2).
                    Use compact phrasing. No paragraphs.

                    ---
                    %s
                    """.formatted(content);

            case "extractive" -> """
                    Return the 3–5 most informative verbatim quotes (<=120 characters each) from the text.
                    Do not paraphrase. No commentary. One quote per line.

                    ---
                    %s
                    """.formatted(content);

            case "keywords" -> """
                    Return 10–15 comma-separated keywords that best index this text.
                    No sentences, no numbering, no hashtags.

                    ---
                    %s
                    """.formatted(content);

            case "tldr" -> """
                    Write a single-sentence TL;DR (<=25 words). No preface, no label.

                    ---
                    %s
                    """.formatted(content);

            case "title" -> """
                    Propose 3 ultra-concise titles (<=8 words each), line-separated.
                    No subtitles, no numbering.

                    ---
                    %s
                    """.formatted(content);

            case "qa" -> """
                    Generate 5 Q&A pairs derived strictly from the text.
                    Format:
                    Q: ...
                    A: ...
                    Keep each answer <= 30 words.

                    ---
                    %s
                    """.formatted(content);

            case "expand" -> """
                    Expand the text into 1–2 tight paragraphs.
                    Preserve meaning, remove redundancy, improve flow. No new facts.

                    ---
                    %s
                    """.formatted(content);

            case "shorten" -> """
                    Shorten the text by about 50%% while preserving all critical information.
                    Keep terminology intact. Output only the shortened version.

                    ---
                    %s
                    """.formatted(content);

            case "detect-language" -> """
                    Read the text and output only:
                    <Language Name> (<ISO-639-1 code>)
                    Nothing else.

                    ---
                    %s
                    """.formatted(content);

            // Fallback to dense summary
            default -> """
                    Summarize the following text in 2–4 concise sentences.
                    Keep only essential facts, results, and implications.
                    Remove filler, anecdotes, and subjective language.

                    ---
                    %s
                    """.formatted(content);
        };
    }
}