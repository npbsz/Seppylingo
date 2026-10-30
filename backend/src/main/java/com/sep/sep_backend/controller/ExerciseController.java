package com.sep.sep_backend.controller;

import com.sep.sep_backend.dto.AnswerCheckRequestDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.sep.sep_backend.dto.MultipleChoiceQuestionDto;
import com.sep.sep_backend.model.Vocabulary;
import com.sep.sep_backend.repository.VocabularyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import jakarta.validation.Valid;

import com.sep.sep_backend.service.QuestionService;

import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api/exercises")
@CrossOrigin(origins = "http://localhost:3000")
public class ExerciseController {

    // Dependency Injection des VocabularyRepository
    @Autowired
    private VocabularyRepository vocabularyRepository;
    @Autowired
    private QuestionService questionService;
    /* NOTE:Dependency injections muessen hier hinzugefuegt werden wenn weitere Endpoints hinzugefuegt werden */

    @GetMapping("/all")
    public List<Vocabulary> getAllVocabulary(){
        return vocabularyRepository.findAll();
    }

    @GetMapping("/random")
    public ResponseEntity<Vocabulary> getRandomVocabulary() {
        // alle Vokabeln aus Datenbank
        List<Vocabulary> allVocabulary = vocabularyRepository.findAll();

        // falls Datenbank leer ist, 404 zurückgeben
        if (allVocabulary.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // zufällige Vokabel auswählen
        Random rand = new Random();
        Vocabulary randomVocab = allVocabulary.get(rand.nextInt(allVocabulary.size()));

        //
        return ResponseEntity.ok(randomVocab);
    }


    // TODO: Weitere Endpunkte für Übungen hinzufügen
    // z.B. Multiple Choice, Lückentexte, etc.


    /**
     * Endpunkt, um alle verfügbaren Themen abzurufen.
     * URL: GET /api/exercises/themes
     */
    @GetMapping("/themes")
    public ResponseEntity<List<String>> getAllThemes() {
        // Benutzt jetzt den "questionService", den du oben hinzugefügt hast
        List<String> themes = questionService.getAllThemes();
        return ResponseEntity.ok(themes);
    }

    /**
     * Endpunkt, um eine Frage für ein bestimmtes Thema abzurufen.
     * URL: GET /api/exercises/multiple-choice/Food
     */
    @GetMapping("/multiple-choice/{theme}")
    public ResponseEntity<MultipleChoiceQuestionDto> getMultipleChoiceQuestion(
            @PathVariable String theme) {

        MultipleChoiceQuestionDto question = questionService.createMultipleChoiceQuestion(theme);
        return ResponseEntity.ok(question);
    }

    /**
     * NEU: Endpunkt, um eine gezielte Frage zu einer ID zu holen (fürs
     * Wiederholen).
     * Aufrufbar unter: GET /api/exercises/question/123
     */
    @GetMapping("/question/{id}")
    public ResponseEntity<MultipleChoiceQuestionDto> getQuestionById(@PathVariable Long id) {
        MultipleChoiceQuestionDto question = questionService.createQuestionForId(id);
        return ResponseEntity.ok(question);
    }





    @PostMapping
    public ResponseEntity<Vocabulary> addVocabulary(@Valid @RequestBody Vocabulary vocabulary) {
        // @RequestBody wandelt JSON-Request-Body in Vocabulary-Objekt um
        Vocabulary savedVocab = vocabularyRepository.save(vocabulary);

        // 201 Created mit dem gespeicherten Vokabelobjekt zurückgeben
        return new ResponseEntity<>(savedVocab, HttpStatus.CREATED);
    }

    /**
     * Endpunkt, um die Antwort des Benutzers zu prüfen.
     * URL: POST /api/exercises/multiple-choice/check
     * Empfängt das DTO und gibt {true} oder {false} zurück.
     */
    @PostMapping("/multiple-choice/check")
    public ResponseEntity<Boolean> checkMultipleChoiceAnswer(@RequestBody AnswerCheckRequestDto request) {

        // Delegiere die Prüfung an den Service.
        boolean isCorrect = questionService.checkAnswer(request);

        // Sende das Ergebnis als einfachen booleschen Wert (true/false) zurück.
        return ResponseEntity.ok(isCorrect);
    }
}















