package com.sep.sep_backend.dto;

import java.util.List;

public class MultipleChoiceQuestionDto {

    private long wordId; // Die ID des zu übersetzenden Wortes
    private String wordToTranslate; // Das deutsche Wort
    private List<String> options; // Die 4 Antwortmöglichkeiten
    private int totalWordsInTheme;
    // Konstruktoren, Getter und Setter
    // Für Spring Boot um die Klasse in JSON umzuwandeln

    public MultipleChoiceQuestionDto(long wordId,
            String wordToTranslate,
            List<String> options,
            int totalWordsInTheme) {
        this.wordId = wordId;
        this.wordToTranslate = wordToTranslate;
        this.options = options;
        this.totalWordsInTheme = totalWordsInTheme;
    }

    //Test Konstruktor
    public MultipleChoiceQuestionDto() {}

    public <T> MultipleChoiceQuestionDto(long l, String theDog, String derHund, List<T> list, String a1) {
    }


    public long getWordId() {
        return wordId;
    }

    public void setWordId(long wordId) {
        this.wordId = wordId;
    }

    public String getWordToTranslate() {
        return wordToTranslate;
    }

    public void setWordToTranslate(String wordToTranslate) {
        this.wordToTranslate = wordToTranslate;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public int getTotalWordsInTheme() {
        return totalWordsInTheme;
    }

    public void setTotalWordsInTheme(int totalWordsInTheme) {
        this.totalWordsInTheme = totalWordsInTheme;
    }
}
