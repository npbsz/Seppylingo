package com.sep.sep_backend.service;

import com.sep.sep_backend.dto.*;
import com.sep.sep_backend.model.ExerciseType;
import com.sep.sep_backend.model.User;
import com.sep.sep_backend.repository.UserRepository;
import com.sep.sep_backend.repository.VocabularyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamServiceTest {

    @Mock
    private QuestionService questionService;
    @Mock
    private LtService ltService;
    @Mock
    private ListExService listExService;
    @Mock
    private VocabularyRepository vocabularyRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ExamService examService;

    // Dummy Daten für die Tests
    private User testUser;
    private final String TEST_USERNAME = "testUser";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername(TEST_USERNAME);
        testUser.setIntroExamDone(false);

        // Standard-Mocks für die Generierung von Fragen, damit startSimpleExam() nicht abstürzt
        // Wir nutzen lenient(), da nicht jeder Test zwingend alle diese Mocks aufruft.
        lenient().when(vocabularyRepository.findAllDistinctThemes()).thenReturn(Arrays.asList("Animals", "Food", "Basics"));
    }

    // --- START EXAM TESTS ---

    @Test
    void startSimpleExam_Success() {
        // Arrange: Mocking der Fragen-Generierung
        mockQuestionGeneration();

        // Act
        ExamStartResponseDto response = examService.startSimpleExam();

        // Assert
        assertNotNull(response);
        assertNotNull(response.getExamId());
        assertFalse(response.getQuestions().isEmpty());
        // Standardmäßig 20 Fragen (10 MC + 5 Gap + 5 Listening)
        assertEquals(20, response.getQuestions().size());
    }

    @Test
    void startIntroExam_Success() {
        // Arrange
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        mockQuestionGeneration();

        // Act
        ExamStartResponseDto response = examService.startIntroExam(TEST_USERNAME);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getExamId());
    }

    @Test
    void startIntroExam_UserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                examService.startIntroExam("unknown")
        );
    }

    @Test
    void startIntroExam_AlreadyDone() {
        testUser.setIntroExamDone(true);
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));

        assertThrows(IllegalStateException.class, () ->
                examService.startIntroExam(TEST_USERNAME)
        );
    }

    // --- EVALUATE EXAM TESTS ---

    @Test
    void evaluateExam_Success_Passed() {
        // 1. Zuerst eine Prüfung starten, um eine Session zu erzeugen
        mockQuestionGeneration();
        ExamStartResponseDto startDto = examService.startSimpleExam();
        UUID examId = startDto.getExamId();

        // 2. Antworten vorbereiten (Wir simulieren alles Richtig)
        ExamSubmissionDto submission = createSubmissionForExam(startDto, true);

        // 3. Mocking der Checks (Services geben immer true zurück)
        when(questionService.checkAnswer(any())).thenReturn(true);
        // Für Gap & Listening erfolgt der Check im ExamService per String-Vergleich.
        // Da wir in 'mockQuestionGeneration' feste Lösungen definiert haben
        // und in 'createSubmissionForExam' diese Lösungen nutzen, passt das automatisch.

        // Act
        ExamResultDto result = examService.evaluateExam(submission);

        // Assert
        assertNotNull(result);
        assertEquals(20, result.getTotalQuestions());
        assertEquals(20, result.getCorrectAnswers());
        assertEquals(100.0, result.getPercentage());
        assertTrue(result.isPassed());
    }

    @Test
    void evaluateExam_Success_Failed() {
        // 1. Starten
        mockQuestionGeneration();
        ExamStartResponseDto startDto = examService.startSimpleExam();

        // 2. Falsche Antworten senden
        ExamSubmissionDto submission = createSubmissionForExam(startDto, false);

        // 3. Mocking (Checks failen)
        when(questionService.checkAnswer(any())).thenReturn(false);

        // Act
        ExamResultDto result = examService.evaluateExam(submission);

        // Assert
        assertEquals(20, result.getTotalQuestions());
        assertEquals(0, result.getCorrectAnswers());
        assertEquals(0.0, result.getPercentage());
        assertFalse(result.isPassed());
    }

    @Test
    void evaluateExam_SessionNotFound() {
        ExamSubmissionDto submission = new ExamSubmissionDto();
        submission.setExamId(UUID.randomUUID()); // Irgendeine ID

        assertThrows(IllegalArgumentException.class, () ->
                examService.evaluateExam(submission)
        );
    }

    // --- EVALUATE INTRO EXAM TESTS ---

    @Test
    void evaluateIntroExam_Success_SavesUser() {
        // 1. User & Session vorbereiten
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        mockQuestionGeneration();

        // Intro Exam starten
        ExamStartResponseDto startDto = examService.startIntroExam(TEST_USERNAME);

        // 2. Submission bauen (z.B. alles richtig -> Advanced)
        ExamSubmissionDto submission = createSubmissionForExam(startDto, true);

        // Mocks für Checks
        when(questionService.checkAnswer(any())).thenReturn(true);

        // Act
        IntroExamResultDto result = examService.evaluateIntroExam(TEST_USERNAME, submission);

        // Assert
        assertNotNull(result);
        assertEquals("ADVANCED", result.getLevel());

        // Verifiziere, dass der User gespeichert wurde
        verify(userRepository).save(argThat(u ->
                u.isIntroExamDone() &&
                        u.getIntroExamPercentage() == 100.0 &&
                        u.getIntroExamLevel().equals("ADVANCED")
        ));
    }

    @Test
    void evaluateIntroExam_WrongUserForSession() {
        // 1. Intro Exam für User A starten
        when(userRepository.findByUsername("UserA")).thenReturn(Optional.of(new User("UserA", "a@a.de", "pw")));
        mockQuestionGeneration();
        ExamStartResponseDto startDto = examService.startIntroExam("UserA");

        // 2. Versuchen, als User B abzugeben
        when(userRepository.findByUsername("UserB")).thenReturn(Optional.of(new User("UserB", "b@b.de", "pw")));

        ExamSubmissionDto submission = new ExamSubmissionDto();
        submission.setExamId(startDto.getExamId());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                examService.evaluateIntroExam("UserB", submission)
        );
    }

    // --- HELPER METHODS ---

    /**
     * Stellt sicher, dass die Generation-Schleifen im Service funktionieren
     * und gültige DTOs zurückbekommen.
     */
    private void mockQuestionGeneration() {
        // 1. Multiple Choice Mock
        MultipleChoiceQuestionDto mcDto = new MultipleChoiceQuestionDto();
        mcDto.setWordId(1L);
        // Wir nutzen lenient, da die Schleife mehrmals aufgerufen wird
        lenient().when(questionService.createMultipleChoiceQuestion(anyString())).thenReturn(mcDto);

        // 2. Gap Text Mock
        LtItemDTO gapDto = new LtItemDTO();
        gapDto.setGameId(UUID.randomUUID());
        gapDto.setAnswer("gapAnswer"); // Die korrekte Lösung für den internen Vergleich
        lenient().when(ltService.createNewLueckentext()).thenReturn(gapDto);

        // 3. Listening Mock
        ListExDTO listDto = new ListExDTO();
        listDto.setId(UUID.randomUUID());
        listDto.setPhrases(new String[]{"listeningAnswer"}); // Lösung an Index 0
        lenient().when(listExService.createNewListEx()).thenReturn(listDto);
    }

    /**
     * Erstellt basierend auf der gestarteten Prüfung eine Antwort-Liste.
     */
    private ExamSubmissionDto createSubmissionForExam(ExamStartResponseDto startDto, boolean correct) {
        ExamSubmissionDto submission = new ExamSubmissionDto();
        submission.setExamId(startDto.getExamId());

        List<ExamAnswerDto> answers = new ArrayList<>();

        for (ExamQuestionDto q : startDto.getQuestions()) {
            ExamAnswerDto ans = new ExamAnswerDto();
            ans.setQuestionId(q.getQuestionId());

            if (correct) {
                if (q.getType() == ExerciseType.MULTIPLE_CHOICE) {
                    ans.setAnswer("AnyString"); // Check passiert im Mock
                } else if (q.getType() == ExerciseType.GAP_TEXT) {
                    ans.setAnswer("gapAnswer"); // Muss mit Helper-Mock übereinstimmen
                } else if (q.getType() == ExerciseType.LISTENING) {
                    ans.setAnswer("listeningAnswer"); // Muss mit Helper-Mock übereinstimmen
                }
            } else {
                ans.setAnswer("wrongAnswer");
            }
            answers.add(ans);
        }
        submission.setAnswers(answers);
        return submission;
    }
}