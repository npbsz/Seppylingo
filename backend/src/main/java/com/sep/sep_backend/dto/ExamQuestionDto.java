package com.sep.sep_backend.dto;

import com.sep.sep_backend.model.ExerciseType;

import java.util.UUID;

public class ExamQuestionDto {

    private UUID questionId;
    private ExerciseType type;

    // Nur eines dieser Felder ist pro Frage gefüllt:
    private MultipleChoiceQuestionDto multipleChoice;
    private LtItemDTO gapText;
    private ListExDTO listening;

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

    public MultipleChoiceQuestionDto getMultipleChoice() {
        return multipleChoice;
    }

    public void setMultipleChoice(MultipleChoiceQuestionDto multipleChoice) {
        this.multipleChoice = multipleChoice;
    }

    public LtItemDTO getGapText() {
        return gapText;
    }

    public void setGapText(LtItemDTO gapText) {
        this.gapText = gapText;
    }

    public ListExDTO getListening() {
        return listening;
    }

    public void setListening(ListExDTO listening) {
        this.listening = listening;
    }
}
