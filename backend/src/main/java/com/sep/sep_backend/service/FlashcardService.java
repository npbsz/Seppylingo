package com.sep.sep_backend.service;

import com.sep.sep_backend.dto.AnswerDto;
import com.sep.sep_backend.dto.AnswerResultDto;
import com.sep.sep_backend.dto.FlashcardDto;
import com.sep.sep_backend.model.Mistake;
import com.sep.sep_backend.model.Vocabulary;
import com.sep.sep_backend.repository.MistakeRepository;
import com.sep.sep_backend.repository.VocabularyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class FlashcardService {

    private final VocabularyRepository vocabRepo;
    private final MistakeRepository mistakeRepo;

    // Demo-User
    private static final Long DEMO_USER = 1L;

    // Punkte im Speicher
    private int points = 0;

    public FlashcardService(VocabularyRepository vocabRepo,
                            MistakeRepository mistakeRepo) {
        this.vocabRepo = vocabRepo;
        this.mistakeRepo = mistakeRepo;
    }

    // normale Karte

    public FlashcardDto next(String dir) {
        Vocabulary v = vocabRepo.findRandom().orElseThrow();
        return toDto(v, dir);
    }

    // Fehlerkarte (aus Mistakes)

    public FlashcardDto nextErrorCard(String dir) {
        Page<Mistake> page = mistakeRepo.findRandomMistake(
                DEMO_USER,
                PageRequest.of(0, 1)
        );

        if (page.isEmpty()) {
            throw new NoSuchElementException("no mistakes left");
        }

        Mistake m = page.getContent().getFirst();
        Vocabulary v = m.getVocab();
        return toDto(v, dir);
    }

    // Hilfsfunktion für beide Modi
    private FlashcardDto toDto(Vocabulary v, String dir) {
        if ("de-en".equalsIgnoreCase(dir)) {
            return new FlashcardDto(
                    v.getId(),
                    v.getWordDE(),
                    v.getWordEN(),
                    "de-en"
            );
        } else {
            return new FlashcardDto(
                    v.getId(),
                    v.getWordEN(),
                    v.getWordDE(),
                    "en-de"
            );
        }
    }

    // Antwort im normalen Modus

    @Transactional
    public AnswerResultDto answer(AnswerDto dto) {
        Vocabulary vocab = vocabRepo.findById(dto.cardId())
                .orElseThrow();

        if (dto.isRight()) {
            // Nur Punkte erhöhen, Fehler bleiben
            points += 1;
        } else {
            // Antwort ist falsch -> Mistake speichern / hochzählen
            Mistake m = mistakeRepo.findByUserIdAndVocab(DEMO_USER, vocab)
                    .orElse(null);
            if (m == null) {
                m = new Mistake(DEMO_USER, vocab);
            } else {
                m.bump();
            }
            mistakeRepo.save(m);
        }

        long mistakes = mistakeRepo.countByUserId(DEMO_USER);
        return new AnswerResultDto(true, points, (int) mistakes);
    }

    // Antwort im Fehler-Modus

    @Transactional
    public AnswerResultDto answerError(AnswerDto dto) {
        Vocabulary vocab = vocabRepo.findById(dto.cardId())
                .orElseThrow();

        boolean isCorrect = dto.isRight();

        if (isCorrect) {
            // richtige Antwort im Fehlermodus -> Mistake löschen
            mistakeRepo.deleteByUserIdAndVocab(DEMO_USER, vocab);
            points += 1;
        } else {
            // immer noch falsch -> Mistake bleibt / wird erhöht
            Mistake m = mistakeRepo.findByUserIdAndVocab(DEMO_USER, vocab)
                    .orElse(null);
            if (m == null) {
                m = new Mistake(DEMO_USER, vocab);
            } else {
                m.bump();
            }
            mistakeRepo.save(m);
        }

        long mistakes = mistakeRepo.countByUserId(DEMO_USER);

        return new AnswerResultDto(isCorrect, points, (int) mistakes);
    }


    // Reset für Demo

    @Transactional
    public void resetMistakes() {
        mistakeRepo.deleteAllByUserId(DEMO_USER);
        points = 0;
    }
}
