package com.sep.sep_backend.service;

import com.sep.sep_backend.dto.AnswerDto;
import com.sep.sep_backend.model.Mistake;
import com.sep.sep_backend.model.Vocabulary;
import com.sep.sep_backend.repository.MistakeRepository;
import com.sep.sep_backend.repository.VocabularyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FlashcardServiceTest {

    private VocabularyRepository vocabRepo;
    private MistakeRepository mistakeRepo;
    private FlashcardService service;

    @BeforeEach
    void setup() {
        vocabRepo = Mockito.mock(VocabularyRepository.class);
        mistakeRepo = Mockito.mock(MistakeRepository.class);
        service = new FlashcardService(vocabRepo, mistakeRepo);
    }

    @Test
    void answer_wrong_savesMistake() {
        // Arrange
        Vocabulary v = new Vocabulary("Hund", "dog", "Nomen", "Animals", "level");
        v.setId(1L);

        when(vocabRepo.findById(1L)).thenReturn(Optional.of(v));
        when(mistakeRepo.findByUserIdAndVocab(1L, v)).thenReturn(Optional.empty());
        when(mistakeRepo.countByUserId(1L)).thenReturn(1L);

        AnswerDto dto = new AnswerDto(1L, false);

        // Act
        var result = service.answer(dto);

        // Assert
        assertThat(result.mistakes()).isEqualTo(1);
        verify(mistakeRepo).save(any(Mistake.class));
    }
}
