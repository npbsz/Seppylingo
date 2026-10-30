package com.sep.sep_backend.dto;

import java.util.List;
import java.util.UUID;

public class ExamStartResponseDto {

    private UUID examId;
    private int passThresholdPercent;
    private List<ExamQuestionDto> questions;

    public UUID getExamId() {
        return examId;
    }

    public void setExamId(UUID examId) {
        this.examId = examId;
    }

    public int getPassThresholdPercent() {
        return passThresholdPercent;
    }

    public void setPassThresholdPercent(int passThresholdPercent) {
        this.passThresholdPercent = passThresholdPercent;
    }

    public List<ExamQuestionDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<ExamQuestionDto> questions) {
        this.questions = questions;
    }
}
