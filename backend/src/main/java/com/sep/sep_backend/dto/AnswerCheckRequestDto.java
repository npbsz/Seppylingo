package com.sep.sep_backend.dto;

public class AnswerCheckRequestDto {

    private long wordId; // Die ID der gestellten Frage (aus dem DTO, das zurückgesendet wurde)
    private String selectedAnswer; // Die vom Benutzer gewählte englische Übersetzung


    //Test Konstruktor
    public AnswerCheckRequestDto() {
    }

    public AnswerCheckRequestDto(long wordId, String selectedAnswer) {

    }
    public long getWordId() {
        return wordId;
    }

    public void setWordId(long wordId) {
        this.wordId = wordId;
    }

    public String getSelectedAnswer() {
        return selectedAnswer;
    }

    public void setSelectedAnswer(String selectedAnswer) {
        this.selectedAnswer = selectedAnswer;
    }
}
