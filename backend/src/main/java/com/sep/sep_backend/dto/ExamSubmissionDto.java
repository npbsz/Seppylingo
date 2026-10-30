package com.sep.sep_backend.dto;

import java.util.List;
import java.util.UUID;

public class ExamSubmissionDto {

    private UUID examId;
    private List<ExamAnswerDto> answers;

    public UUID getExamId() {
        return examId;
    }

    public void setExamId(UUID examId) {
        this.examId = examId;
    }

    public List<ExamAnswerDto> getAnswers() {
        return answers;
    }

    public void setAnswers(List<ExamAnswerDto> answers) {
        this.answers = answers;
    }
}
