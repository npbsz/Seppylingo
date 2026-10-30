package com.sep.sep_backend.service;

import com.sep.sep_backend.dto.*;
import com.sep.sep_backend.model.ExerciseType;
import com.sep.sep_backend.model.User;
import com.sep.sep_backend.repository.UserRepository;
import com.sep.sep_backend.repository.VocabularyRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ExamService {

    private static final int TOTAL_QUESTIONS = 20;
    private static final int PASS_THRESHOLD_PERCENT = 80;

    private final QuestionService questionService;
    private final LtService ltService;
    private final ListExService listExService;
    private final VocabularyRepository vocabularyRepository;
    private final UserRepository userRepository;

    private final Random random = new Random();

    // In-Memory Exam Sessions
    private final Map<UUID, ExamSession> activeExams = new ConcurrentHashMap<>();

    public ExamService(QuestionService questionService,
                       LtService ltService,
                       ListExService listExService,
                       VocabularyRepository vocabularyRepository,
                       UserRepository userRepository) {
        this.questionService = questionService;
        this.ltService = ltService;
        this.listExService = listExService;
        this.vocabularyRepository = vocabularyRepository;
        this.userRepository = userRepository;
    }

    // START NORMAL EXAM

    public ExamStartResponseDto startSimpleExam() {
        return createExamSession(null, false);
    }

    // START INTRO EXAM (nur 1x)

    public ExamStartResponseDto startIntroExam(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User nicht gefunden"));

        if (user.isIntroExamDone()) {
            throw new IllegalStateException("Einführungsprüfung wurde bereits abgeschlossen.");
        }

        return createExamSession(username, true);
    }

    // SUBMIT NORMAL EXAM

    public ExamResultDto evaluateExam(ExamSubmissionDto submission) {
        ExamSession session = activeExams.get(submission.getExamId());
        if (session == null) {
            throw new IllegalArgumentException("Prüfung nicht gefunden oder abgelaufen.");
        }

        int correct = calculateCorrectAnswers(session, submission);
        int total = session.getQuestions().size();

        double percentage = total == 0 ? 0 : (correct * 100.0) / total;
        boolean passed = percentage >= PASS_THRESHOLD_PERCENT;

        activeExams.remove(submission.getExamId());

        return new ExamResultDto(total, correct, percentage, passed);
    }

    // SUBMIT INTRO EXAM (speichert User-Felder)

    public IntroExamResultDto evaluateIntroExam(String username, ExamSubmissionDto submission) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User nicht gefunden"));

        if (user.isIntroExamDone()) {
            throw new IllegalStateException("Einführungsprüfung wurde bereits abgeschlossen.");
        }

        ExamSession session = activeExams.get(submission.getExamId());
        if (session == null) {
            throw new IllegalArgumentException("Prüfung nicht gefunden oder abgelaufen.");
        }

        // Nur Intro-Sessions dürfen hier rein
        if (!session.isIntroExam() || session.getUsername() == null || !session.getUsername().equals(username)) {
            throw new IllegalArgumentException("Diese Prüfung gehört nicht zur Einführungsprüfung dieses Users.");
        }

        int correct = calculateCorrectAnswers(session, submission);
        int total = session.getQuestions().size();

        double percentage = total == 0 ? 0 : (correct * 100.0) / total;
        boolean passed = percentage >= PASS_THRESHOLD_PERCENT;

        // Level + Empfehlung berechnen
        IntroAssessment assessment = buildIntroAssessment(percentage);

        // User speichern (nur 1x)
        user.setIntroExamDone(true);
        user.setIntroExamPercentage(percentage);
        user.setIntroExamLevel(assessment.level);
        userRepository.save(user);

        activeExams.remove(submission.getExamId());

        return new IntroExamResultDto(
                total,
                correct,
                percentage,
                passed,
                assessment.level,
                assessment.recommendation
        );
    }

    // CORE: SESSION CREATION

    private ExamStartResponseDto createExamSession(String username, boolean introExam) {

        int mcTarget = TOTAL_QUESTIONS * 50 / 100;   // 10
        int gapTarget = TOTAL_QUESTIONS * 25 / 100;  // 5
        int listeningTarget = TOTAL_QUESTIONS - mcTarget - gapTarget; // 5

        UUID examId = UUID.randomUUID();

        List<ExamQuestionDto> publicQuestions = new ArrayList<>();
        List<ExamQuestionInternal> internalQuestions = new ArrayList<>();

        // MULTIPLE CHOICE
        int createdMc = 0;
        int mcTries = mcTarget * 10;

        while (createdMc < mcTarget && mcTries-- > 0) {
            try {
                String theme = pickRandomTheme();
                MultipleChoiceQuestionDto mcDto =
                        questionService.createMultipleChoiceQuestion(theme);

                UUID questionId = UUID.randomUUID();

                ExamQuestionDto publicDto = new ExamQuestionDto();
                publicDto.setQuestionId(questionId);
                publicDto.setType(ExerciseType.MULTIPLE_CHOICE);
                publicDto.setMultipleChoice(mcDto);
                publicQuestions.add(publicDto);

                ExamQuestionInternal internal = new ExamQuestionInternal();
                internal.setQuestionId(questionId);
                internal.setType(ExerciseType.MULTIPLE_CHOICE);
                internal.setWordId(mcDto.getWordId());
                internalQuestions.add(internal);

                createdMc++;
            } catch (RuntimeException e) {
                // Theme ungeeignet (z.B. <4 Vokabeln) → ignorieren
            }
        }

        // GAP TEXT
        fillGapTextQuestions(gapTarget, publicQuestions, internalQuestions);

        // LISTENING
        fillListeningQuestions(listeningTarget, publicQuestions, internalQuestions);

        if (internalQuestions.isEmpty()) {
            throw new IllegalStateException("Keine Fragen für die Prüfung generiert.");
        }

        ExamSession session = new ExamSession();
        session.setExamId(examId);
        session.setPassThreshold(PASS_THRESHOLD_PERCENT / 100.0);
        session.setQuestions(internalQuestions);
        session.setIntroExam(introExam);
        session.setUsername(username);

        activeExams.put(examId, session);

        ExamStartResponseDto response = new ExamStartResponseDto();
        response.setExamId(examId);
        response.setPassThresholdPercent(PASS_THRESHOLD_PERCENT);
        response.setQuestions(publicQuestions);

        return response;
    }

    // CHECKING

    private int calculateCorrectAnswers(ExamSession session, ExamSubmissionDto submission) {

        Map<UUID, ExamQuestionInternal> questionMap = new HashMap<>();
        for (ExamQuestionInternal q : session.getQuestions()) {
            questionMap.put(q.getQuestionId(), q);
        }

        int correct = 0;

        for (ExamAnswerDto answer : submission.getAnswers()) {

            ExamQuestionInternal internal = questionMap.get(answer.getQuestionId());
            if (internal == null) continue;

            boolean isCorrect = switch (internal.getType()) {
                case MULTIPLE_CHOICE -> checkMultipleChoice(internal, answer);
                case GAP_TEXT -> checkGapText(internal, answer);
                case LISTENING -> checkListening(internal, answer);
            };

            if (isCorrect) correct++;
        }

        return correct;
    }

    private boolean checkMultipleChoice(ExamQuestionInternal internal, ExamAnswerDto answer) {
        if (internal.getWordId() == null || answer.getAnswer() == null) return false;

        AnswerCheckRequestDto req = new AnswerCheckRequestDto();
        req.setWordId(internal.getWordId());
        req.setSelectedAnswer(answer.getAnswer());

        return questionService.checkAnswer(req);
    }

    private boolean checkGapText(ExamQuestionInternal internal, ExamAnswerDto answer) {
        if (internal.getGapSolution() == null || answer.getAnswer() == null) return false;
        return normalize(internal.getGapSolution()).equals(normalize(answer.getAnswer()));
    }

    private boolean checkListening(ExamQuestionInternal internal, ExamAnswerDto answer) {
        if (internal.getListeningSolution() == null || answer.getAnswer() == null) return false;
        return normalize(internal.getListeningSolution()).equals(normalize(answer.getAnswer()));
    }

    private String normalize(String s) {
        return s.trim()
                .replaceAll("[.,!?]", "")
                .toLowerCase(Locale.ROOT);
    }

    // GENERATION HELPERS

    private void fillGapTextQuestions(int target,
                                      List<ExamQuestionDto> publicQuestions,
                                      List<ExamQuestionInternal> internalQuestions) {

        int created = 0;
        int maxTries = target * 10;

        while (created < target && maxTries-- > 0) {
            LtItemDTO ltDto;
            try {
                ltDto = ltService.createNewLueckentext();
            } catch (Exception e) {
                continue;
            }

            if (ltDto == null || ltDto.getGameId() == null) continue;

            UUID questionId = UUID.randomUUID();
            String solution = ltDto.getAnswer(); // ✅ bleibt im DTO, damit Frontend 4 Buttons hat

            ExamQuestionDto publicDto = new ExamQuestionDto();
            publicDto.setQuestionId(questionId);
            publicDto.setType(ExerciseType.GAP_TEXT);
            publicDto.setGapText(ltDto);
            publicQuestions.add(publicDto);

            ExamQuestionInternal internal = new ExamQuestionInternal();
            internal.setQuestionId(questionId);
            internal.setType(ExerciseType.GAP_TEXT);
            internal.setGapSolution(solution);
            internalQuestions.add(internal);

            created++;
        }
    }

    private void fillListeningQuestions(int target,
                                        List<ExamQuestionDto> publicQuestions,
                                        List<ExamQuestionInternal> internalQuestions) {

        int created = 0;
        int maxTries = target * 10;

        while (created < target && maxTries-- > 0) {
            ListExDTO listExDto;
            try {
                listExDto = listExService.createNewListEx();
            } catch (Exception e) {
                continue;
            }

            if (listExDto == null || listExDto.getPhrases() == null || listExDto.getPhrases().length == 0) {
                continue;
            }

            UUID questionId = UUID.randomUUID();
            String correctPhrase = listExDto.getPhrases()[0];

            ExamQuestionDto publicDto = new ExamQuestionDto();
            publicDto.setQuestionId(questionId);
            publicDto.setType(ExerciseType.LISTENING);
            publicDto.setListening(listExDto);
            publicQuestions.add(publicDto);

            ExamQuestionInternal internal = new ExamQuestionInternal();
            internal.setQuestionId(questionId);
            internal.setType(ExerciseType.LISTENING);
            internal.setListeningSolution(correctPhrase);
            internalQuestions.add(internal);

            created++;
        }
    }

    private String pickRandomTheme() {
        List<String> themes = vocabularyRepository.findAllDistinctThemes();
        if (themes == null || themes.isEmpty()) {
            throw new IllegalStateException("Keine Themes für Multiple Choice vorhanden.");
        }
        return themes.get(random.nextInt(themes.size()));
    }

    // INTRO ASSESSMENT (Text + Level)

    private IntroAssessment buildIntroAssessment(double percentage) {

        if (percentage < 20) {
            return new IntroAssessment(
                    "BEGINNER_1",
                    "Du bist noch ganz am Anfang – aber kein Problem! Starte mit den Grundlagen (Flashcards + Lückentext) und übe täglich 10–15 Minuten. Du wirst sehr schnell Fortschritte merken."
            );
        }
        if (percentage < 40) {
            return new IntroAssessment(
                    "BEGINNER_2",
                    "Du kennst bereits einiges – stark! Als nächstes vertiefen wir Wortschatz und einfache Satzmuster. 15 Minuten pro Tag reichen, um schnell stabiler zu werden."
            );
        }
        if (percentage < 60) {
            return new IntroAssessment(
                    "INTERMEDIATE_1",
                    "Solide Basis! Du kannst schon gut mitgehen. Wir fokussieren jetzt auf mehr Sicherheit und weniger Fehler – besonders bei ähnlichen Wörtern und typischen Redewendungen."
            );
        }
        if (percentage < 80) {
            return new IntroAssessment(
                    "INTERMEDIATE_2",
                    "Richtig gut! Du bist schon auf einem starken Level. Mit gezieltem Training (Hörverstehen + anspruchsvollere Lückentexte) bekommst du schnell den Feinschliff."
            );
        }

        return new IntroAssessment(
                "ADVANCED",
                "Wow – du bist ja schon fast auf Muttersprachler-Niveau! 😄 Wir können direkt mit schwierigeren Phrasen, Hörverständnis und kniffligen Bedeutungsunterschieden arbeiten."
        );
    }

    private static class IntroAssessment {
        private final String level;
        private final String recommendation;

        private IntroAssessment(String level, String recommendation) {
            this.level = level;
            this.recommendation = recommendation;
        }
    }

    // INTERNAL CLASSES

    private static class ExamSession {
        private UUID examId;
        private double passThreshold;
        private List<ExamQuestionInternal> questions;

        private boolean introExam;
        private String username;

        public UUID getExamId() { return examId; }
        public void setExamId(UUID examId) { this.examId = examId; }

        public double getPassThreshold() { return passThreshold; }
        public void setPassThreshold(double passThreshold) { this.passThreshold = passThreshold; }

        public List<ExamQuestionInternal> getQuestions() { return questions; }
        public void setQuestions(List<ExamQuestionInternal> questions) { this.questions = questions; }

        public boolean isIntroExam() { return introExam; }
        public void setIntroExam(boolean introExam) { this.introExam = introExam; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }

    private static class ExamQuestionInternal {
        private UUID questionId;
        private ExerciseType type;
        private Long wordId;
        private String gapSolution;
        private String listeningSolution;

        public UUID getQuestionId() { return questionId; }
        public void setQuestionId(UUID questionId) { this.questionId = questionId; }

        public ExerciseType getType() { return type; }
        public void setType(ExerciseType type) { this.type = type; }

        public Long getWordId() { return wordId; }
        public void setWordId(Long wordId) { this.wordId = wordId; }

        public String getGapSolution() { return gapSolution; }
        public void setGapSolution(String gapSolution) { this.gapSolution = gapSolution; }

        public String getListeningSolution() { return listeningSolution; }
        public void setListeningSolution(String listeningSolution) { this.listeningSolution = listeningSolution; }
    }
}
