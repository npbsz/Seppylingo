package com.sep.sep_backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Entity that contains a Phrase with its translation
 */
@Entity
public class PhraseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String phrase_EN;
    private String phrase_DE;
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getPhrase_EN() {
        return phrase_EN;
    }
    public String getPhrase_DE() {
        return phrase_DE;
    }
    public void setPhrase_EN(String textEN) {
        this.phrase_EN = textEN;
    }
    public void setPhrase_DE(String textDE) {
        this.phrase_DE = textDE;
    }
}
