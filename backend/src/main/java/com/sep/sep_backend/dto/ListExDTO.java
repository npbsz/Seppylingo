package com.sep.sep_backend.dto;

import java.util.UUID;

/**
 * DTO to be sent to Frontend
 * Format {UUID id, String[] phrases}
 */
public class ListExDTO {
    UUID id;
    String[] phrases;

    public ListExDTO() {
    }
    public ListExDTO(UUID id, String[] phrases) {
        this.id = id;
        this.phrases = phrases;
    }

    //Getter setter for variables
    public String[] getPhrases() {
        return phrases;
    }
    public void setPhrases(String[] phrases) {
        this.phrases = phrases;
    }
    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }
}
