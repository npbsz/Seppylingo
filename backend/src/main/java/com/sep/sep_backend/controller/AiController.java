package com.sep.sep_backend.controller;

import com.sep.sep_backend.dto.ChatRequestDto;
import com.sep.sep_backend.dto.FlashcardExplainRequestDto;
import com.sep.sep_backend.dto.FlashcardExplainResponseDto;
import com.sep.sep_backend.dto.LtAiListResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:4200"})
public class AiController {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/chat")
    public ResponseEntity<Object> getAiResponse(@RequestBody ChatRequestDto request) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-lite:generateContent?key=" + apiKey;

        String fullPrompt = String.format(
                "Anweisung: %s\nAktueller Bereich in der App: %s\nNutzer fragt: %s",
                request.systemPrompt(), request.context(), request.message()
        );

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", fullPrompt)
                        ))
                ),
                "generationConfig", Map.of("maxOutputTokens", 150, "temperature", 0.7)
        );

        try {
            System.out.println("Sende Anfrage an Gemini URL: " + url.replace(apiKey, "API_KEY_HIDDEN"));
            ResponseEntity<Object> response = restTemplate.postForEntity(url, body, Object.class);
            return ResponseEntity.ok(response.getBody());

        } catch (HttpClientErrorException e) {
            System.err.println("--- GEMINI API FEHLER (HTTP) ---");
            System.err.println("Status Code: " + e.getStatusCode());
            System.err.println("Response Body: " + e.getResponseBodyAsString());
            e.printStackTrace();
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());

        } catch (Exception e) {
            System.err.println("--- ALLGEMEINER FEHLER ---");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Interner Server Fehler: " + e.getMessage());
        }
    }


    // Flashcard erklären (stabile JSON Response)
    // Antwortsprache:
    // - en-de -> Antwort Englisch
    // - de-en -> Antwort Deutsch

    @PostMapping("/flashcards/explain")
    public ResponseEntity<?> explainFlashcard(@RequestBody FlashcardExplainRequestDto req) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-lite:generateContent?key=" + apiKey;

        // Level aus Score (optional)
        String level = "A1";
        if (req.userScore() != null && req.userScore() > 300) level = "A2/B1";
        if (req.userScore() != null && req.userScore() > 1000) level = "B2/C1";

        // Richtung
        String direction = (req.direction() == null) ? "en-de" : req.direction();
        boolean deEn = "de-en".equalsIgnoreCase(direction);

        // Antwortsprache abhängig von Richtung:
        // en-de => Englisch, de-en => Deutsch
        String replyLanguage = deEn ? "Deutsch" : "English";

        // Wir wollen JSON ohne Markdown, damit Frontend sicher parsen kann
        String systemInstruction =
                "You are a helpful language tutor. " +
                        "Return ONLY valid JSON. No markdown. No extra text outside JSON. " +
                        "Answer language MUST be: " + replyLanguage + ". " +
                        "JSON schema exactly: {\"explanation\": string, \"examples\": string[], \"usageTips\": string[]}." +
                        "Give exactly 2 examples. usageTips: 0-3 short tips. " +
                        "No modern emojis; only text emoticons like :)";

        String task;
        if (deEn) {
            // de-en: Front=DE -> Antwortsprache Deutsch
            task =
                    "Antworte auf Deutsch. " +
                            "Erkläre kurz die deutsche Vokabel/Redewendung. " +
                            "Gib genau 2 deutsche Beispielsätze, in denen die Vokabel/Redewendung vorkommt. " +
                            "Wenn sinnvoll, erwähne kurz die englische Übersetzung. " +
                            "Front (DE): " + req.front() + "\n" +
                            "Back (EN): " + req.back() + "\n";
        } else {
            // en-de: Front=EN -> Antwortsprache Englisch
            task =
                    "Answer in English. " +
                            "Explain briefly the English word/phrase. " +
                            "Give exactly 2 English example sentences that contain the word/phrase. " +
                            "If helpful, briefly mention the German translation. " +
                            "Front (EN): " + req.front() + "\n" +
                            "Back (DE): " + req.back() + "\n";
        }

        String fullPrompt =
                "User level: " + level + "\n" +
                        systemInstruction + "\n" +
                        task +
                        "Important: Examples must actually include the term from Front.";

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", fullPrompt)))
                ),
                "generationConfig", Map.of("maxOutputTokens", 220, "temperature", 0.6)
        );

        try {
            ResponseEntity<Object> response = restTemplate.postForEntity(url, body, Object.class);

            // Gemini Antworttext extrahieren
            String text = extractGeminiText(response.getBody());
            if (text == null || text.isBlank()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Leere KI-Antwort.");
            }

            // JSON parsen
            FlashcardExplainResponseDto parsed = parseExplainJson(text);
            return ResponseEntity.ok(parsed);

        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Interner Server Fehler: " + e.getMessage());
        }
    }

    private String extractGeminiText(Object geminiBody) {
        // Erwartete Struktur (vereinfacht):
        // { candidates: [ { content: { parts: [ { text: "..." } ] } } ] }
        if (!(geminiBody instanceof Map<?, ?> bodyMap)) return null;

        Object candidatesObj = bodyMap.get("candidates");
        if (!(candidatesObj instanceof List<?> candidates) || candidates.isEmpty()) return null;

        Object firstCandidate = candidates.get(0);
        if (!(firstCandidate instanceof Map<?, ?> candMap)) return null;

        Object contentObj = candMap.get("content");
        if (!(contentObj instanceof Map<?, ?> contentMap)) return null;

        Object partsObj = contentMap.get("parts");
        if (!(partsObj instanceof List<?> parts) || parts.isEmpty()) return null;

        Object firstPart = parts.get(0);
        if (!(firstPart instanceof Map<?, ?> partMap)) return null;

        Object textObj = partMap.get("text");
        return (textObj instanceof String s) ? s.trim() : null;
    }

    private FlashcardExplainResponseDto parseExplainJson(String text) {
        // Falls Gemini mal drumherum Text liefert, versuchen wir das JSON herauszuschneiden.
        String json = text.trim();

        int start = json.indexOf('{');
        int end = json.lastIndexOf('}');
        if (start >= 0 && end > start) {
            json = json.substring(start, end + 1);
        }

        try {
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode node = om.readTree(json);

            String explanation = node.path("explanation").asText("");
            List<String> examples = new ArrayList<>();
            node.path("examples").forEach(n -> examples.add(n.asText()));

            List<String> tips = new ArrayList<>();
            node.path("usageTips").forEach(n -> tips.add(n.asText()));

            return new FlashcardExplainResponseDto(explanation, examples, tips);
        } catch (Exception e) {
            // Fallback: wenn JSON kaputt ist, gib wenigstens Text als explanation zurück
            return new FlashcardExplainResponseDto(text, List.of(), List.of());
        }
    }

    @PostMapping("/lueckentext/newai")
    public ResponseEntity<?> generateGapTasks(@RequestBody Map<String, String> request) {
        String theme = request.getOrDefault("theme", "General daily life");
        String count= request.getOrDefault("count", "5");
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-lite:generateContent?key=" + apiKey;

        String systemInstruction =
                "You are an expert language teacher. Generate exactly"+count+"'Lueckentext' (gap-fill) tasks. " +
                        "Return ONLY valid JSON. No markdown. " +
                        "JSON schema: {\"tasks\": [{\"german\": string, \"englishWithGap\": string, \"missingWord\": string,\"options\": string[], \"synonyms\": string[]}]} " +
                        "Requirements: englishWithGap must use '____' for the gap. synonyms should be 2-3 alternative correct words always in english, " +
                        "options should be incorrect words for a multiple choice game.";

        String userPrompt = "Theme: " + theme + ". Generate"+ count +"tasks based on this theme.";

        String fullPrompt = systemInstruction + "\n" + userPrompt;

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", fullPrompt)))
                ),
                "generationConfig", Map.of(
                        "maxOutputTokens", 800,
                        "temperature", 0.8,
                        "responseMimeType", "application/json"
                )
        );

        try {
            ResponseEntity<Object> response = restTemplate.postForEntity(url, body, Object.class);
            String text = extractGeminiText(response.getBody());

            if (text == null || text.isBlank()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("AI response empty.");
            }

            LtAiListResponseDto parsed = parseGapTextJson(text);
            return ResponseEntity.ok(parsed);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    private LtAiListResponseDto parseGapTextJson(String text) {
        String json = text.trim();
        int start = json.indexOf('{');
        int end = json.lastIndexOf('}');
        if (start >= 0 && end > start) {
            json = json.substring(start, end + 1);
        }

        try {
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            return om.readValue(json, LtAiListResponseDto.class);
        } catch (Exception e) {
            return new LtAiListResponseDto(List.of());
        }
    }

}
