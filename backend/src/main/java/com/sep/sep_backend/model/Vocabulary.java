package com.sep.sep_backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;

@Entity
public class Vocabulary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "Das deutsche Wort darf nicht leer sein.")
    private String wordDE;

    @NotEmpty(message = "Das englische Wort darf nicht leer sein.")
    private String wordEN;

    @NotEmpty(message = "Der Worttyp darf nicht leer sein.")
    private String wordType; // Nomen, Verb, Adjektiv, etc.

    @NotEmpty(message = "Das Thema darf nicht leer sein.")
    private String theme; // Grundlagen, Tiere, Essen, etc.

    private String level; // A1, A2, B1, B2, C1, C2 nach dem Europäischen Referenzrahmen

    public Vocabulary() {
    }

    // Konstruktor für die Datenbank
    public Vocabulary(String wordDE, String wordEN, String wordType, String theme, String level) {
        this.wordDE = wordDE;
        this.wordEN = wordEN;
        this.wordType = wordType;
        this.theme = theme;
        this.level = level;
    }

    // Getter und Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWordDE() {
        return wordDE;
    }

    public void setWordDE(String wordDE) {
        this.wordDE = wordDE;
    }

    public String getWordEN() {
        return wordEN;
    }

    public void setWordEN(String wordEN) {
        this.wordEN = wordEN;
    }

    public String getWordType() {
        return wordType;
    }

    public void setWordType(String wordType) {
        this.wordType = wordType;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
}
