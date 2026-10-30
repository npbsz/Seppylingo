package com.sep.sep_backend.service;

import com.sep.sep_backend.dto.AnswerCheckRequestDto;
import com.sep.sep_backend.dto.MultipleChoiceQuestionDto;
import com.sep.sep_backend.model.Vocabulary;
import com.sep.sep_backend.repository.VocabularyRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class QuestionService {

    private final VocabularyRepository vocabularyRepository;

    // Spring gibt uns das Repository automatisch über diesen Konstruktor
    public QuestionService(VocabularyRepository vocabularyRepository) {
        this.vocabularyRepository = vocabularyRepository;
    }

    // Gibt alle unterschiedlichen Themen zurück,

    public List<String> getAllThemes() {
        return vocabularyRepository.findAllDistinctThemes();
    }

    /**
     * Erstellt eine Multiple-Choice-Frage für das angegebene Thema.
     * - Nimmt alle Vokabeln dieses Themas
     * - mischt sie
     * - nimmt 4 als Antwortmöglichkeiten
     * - eine davon ist die richtige
     */
    public MultipleChoiceQuestionDto createMultipleChoiceQuestion(String theme) {

        // 1. Hole alle Wörter nur für das angeforderte Thema
        List<Vocabulary> wordsInTheme = vocabularyRepository.findByTheme(theme);

        // 2. Entferne Duplikate anhand der englischen Übersetzung (wordEN)
        List<Vocabulary> uniqueWordsInTheme = new ArrayList<>();
        Set<String> seenEnglishWords = new HashSet<>();

        for (Vocabulary v : wordsInTheme) {
            // add(...) liefert nur dann true, wenn das Wort noch nicht im Set ist
            if (seenEnglishWords.add(v.getWordEN())) {
                uniqueWordsInTheme.add(v);
            }
        }

        // 3. Prüfe, ob genug verschiedene Antworten vorhanden sind (mind. 4)
        if (uniqueWordsInTheme.size() < 4) {
            throw new RuntimeException(
                    "Das Thema '" + theme + "' hat nicht genügend unterschiedliche Vokabeln (mind. 4 benötigt).");
        }

        // 4. Mische die eindeutige Liste
        Collections.shuffle(uniqueWordsInTheme);

        // 5. Wähle die ersten 4 Vokabeln daraus
        List<Vocabulary> randomWords = uniqueWordsInTheme.subList(0, 4);

        // 6. Bestimme die richtige Antwort (erste Vokabel)
        Vocabulary correctWord = randomWords.get(0);

        // 7. Erstelle die Antwort-Optionen (englische Wörter)
        List<String> options = new ArrayList<>();
        for (Vocabulary word : randomWords) {
            options.add(word.getWordEN());
        }

        // 8. Antwort-Optionen nochmal durchmischen
        Collections.shuffle(options);

        int totalWords = uniqueWordsInTheme.size();

        // 9. DTO zurückgeben
        return new MultipleChoiceQuestionDto(
                correctWord.getId(),
                correctWord.getWordDE(),
                options, totalWords);
    }

    public MultipleChoiceQuestionDto createQuestionForId(Long id) {
        // 1. Das Wort laden, das wir wiederholen wollen (z.B. das, was falsch war)
        Vocabulary correctWord = vocabularyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vokabel nicht gefunden: " + id));

        // 2. Andere Wörter aus dem gleichen Thema holen, um die Buttons zu füllen
        List<Vocabulary> wordsInTheme = vocabularyRepository.findByTheme(correctWord.getTheme());

        // 3. Nur einzigartige englische Wörter sammeln
        List<Vocabulary> uniqueWords = new ArrayList<>();
        Set<String> seenEnglish = new HashSet<>();
        for (Vocabulary v : wordsInTheme) {
            if (seenEnglish.add(v.getWordEN())) {
                uniqueWords.add(v);
            }
        }

        // 4. Das richtige Wort aus der Liste der "Fake-Optionen" entfernen
        uniqueWords.removeIf(v -> v.getId().equals(correctWord.getId()));

        // Sicherstellen, dass wir genug Vokabeln für 4 Buttons haben
        if (uniqueWords.size() < 3) {
            // Falls das Thema extrem klein ist (< 4 Wörter), wirf einen Fehler

            throw new RuntimeException("Zu wenige Vokabeln im Thema in der DB, um 4 Buttons zu füllen.");
        }

        // 5. Drei zufällige "falsche" Wörter auswählen
        Collections.shuffle(uniqueWords);
        List<Vocabulary> optionsVocabs = uniqueWords.subList(0, 3);

        // 6. Alles zusammenpacken (Richtige + 3 Falsche)
        List<String> options = new ArrayList<>();
        options.add(correctWord.getWordEN());
        for (Vocabulary v : optionsVocabs) {
            options.add(v.getWordEN());
        }
        // Mischen, damit die richtige Antwort nicht immer oben steht
        Collections.shuffle(options);

        return new MultipleChoiceQuestionDto(
                correctWord.getId(),
                correctWord.getWordDE(),
                options,
                wordsInTheme.size());
    }

    /**
     * Prüft, ob die gewählte Antwort zur Vokabel-ID passt.
     * 
     * @return true = korrekt, false = falsch
     */
    public boolean checkAnswer(AnswerCheckRequestDto request) {

        // Vokabel mit der übergebenen ID aus der Datenbank laden
        Vocabulary vocabulary = vocabularyRepository.findById(request.getWordId())
                .orElseThrow(() -> new IllegalArgumentException("Ungültige Vokabel-ID: " + request.getWordId()));

        // Richtige englische Übersetzung aus der Entity
        String correctAnswer = vocabulary.getWordEN();

        // Antwort, die der User angeklickt hat
        String userAnswer = request.getSelectedAnswer();

        // Vergleich ohne Beachtung von Groß-/Kleinschreibung
        return correctAnswer.equalsIgnoreCase(userAnswer);
    }
}
