package com.sep.sep_backend.controller;

import com.sep.sep_backend.dto.AnswerDto;
import com.sep.sep_backend.dto.AnswerResultDto;
import com.sep.sep_backend.dto.FlashcardDto;
import com.sep.sep_backend.service.FlashcardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/flashcards")
@CrossOrigin(origins = "http://localhost:3000") // für dein Angular-Dev-Server
public class FlashcardController {

    private final FlashcardService flashcardService;

    public FlashcardController(FlashcardService flashcardService) {
        this.flashcardService = flashcardService;
    }

    // normale Karte
    @GetMapping("/next")
    public FlashcardDto next(
            @RequestParam(defaultValue = "en-de") String dir
    ) {
        return flashcardService.next(dir);
    }

    // Fehlerkarte (nur wenn Mistakes vorhanden)
    @GetMapping("/next-error")
    public ResponseEntity<FlashcardDto> nextError(
            @RequestParam(defaultValue = "en-de") String dir
    ) {
        try {
            FlashcardDto dto = flashcardService.nextErrorCard(dir);
            return ResponseEntity.ok(dto);
        } catch (NoSuchElementException ex) {
            // keine Fehler mehr vorhanden
            return ResponseEntity.noContent().build();
        }
    }

    // Antwort im normalen Modus
    @PostMapping("/answer")
    public AnswerResultDto answer(@RequestBody AnswerDto dto) {
        return flashcardService.answer(dto);
    }

    // Antwort im Fehlermodus
    @PostMapping("/answer-error")
    public AnswerResultDto answerError(@RequestBody AnswerDto dto) {
        return flashcardService.answerError(dto);
    }

    // Reset für Demo
    @DeleteMapping("/reset")
    public String reset() {
        flashcardService.resetMistakes();
        return "ok";
    }
}
