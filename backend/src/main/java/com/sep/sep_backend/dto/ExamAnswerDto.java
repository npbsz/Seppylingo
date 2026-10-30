package com.sep.sep_backend.dto;

import com.sep.sep_backend.model.ExerciseType;

import java.util.UUID;

public class ExamAnswerDto {

    private UUID questionId;
    private ExerciseType type;

    // Für alle Typen nutzen wir erstmal einen String:
    // - MC: ausgewählte Option (englisches Wort)
    // - GAP_TEXT: eingegebenes Wort
    // - LISTENING: gewählte Phrase
    private String answer;

    public UUID getQuestionId() {
        return questionId;
    }

    public void setQuestionId(UUID questionId) {
        this.questionId = questionId;
    }

    public ExerciseType getType() {
        return type;
    }

    public void setType(ExerciseType type) {
        this.type = type;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
