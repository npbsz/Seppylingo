package com.sep.sep_backend.dto;
import java.util.UUID;
/**
 * DTO that is sent to frontend, when creating a new game
 * Format {UUID id, String textEN, String textDE, String answer, String[] dummyWords}
 */
public class LtItemDTO {
    private UUID id;
    private String textEN;
    private String textDE;
    private String answer;
    private String[] dummyWords;

    //Getter, Setter for variables
    public void setAnswer(String answer) {this.answer = answer;}
    public String getAnswer() {return answer;}

    public String[] getDummyWords() {return dummyWords;}
    public void setDummyWords(String[] dummyWords) {this.dummyWords = dummyWords;}

    public UUID getGameId() {
        return id;
    }
    public void setGameId(UUID id) {
        this.id = id;
    }

    public String getTextWithBlank() {
        return textEN;
    }
    public void setTextWithBlank(String textWithBlank) {
        this.textEN = textWithBlank;
    }

    public String getTranslation(){return textDE;}
    public void setTranslation(String textDE){this.textDE=textDE;}
}
