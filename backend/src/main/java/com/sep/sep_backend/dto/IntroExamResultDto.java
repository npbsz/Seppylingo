package com.sep.sep_backend.dto;

public class IntroExamResultDto {

    private int totalQuestions;
    private int correctAnswers;
    private double percentage;
    private boolean passed;

    private String level;          // z.B. "BEGINNER", "INTERMEDIATE", ...
    private String recommendation; // Text für den User

    public IntroExamResultDto() {}

    public IntroExamResultDto(int totalQuestions, int correctAnswers, double percentage, boolean passed,
                              String level, String recommendation) {
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.percentage = percentage;
        this.passed = passed;
        this.level = level;
        this.recommendation = recommendation;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }
}
