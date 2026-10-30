package com.sep.sep_backend.dto;

public class AuthResponseDto {

    private String token;
    private String username;

    /**
     * true = User muss Intro-Prüfung noch machen (z.B. direkt nach erstem Login)
     * false = Intro-Prüfung ist bereits erledigt
     */
    private boolean introExamRequired;

    /**
     * Ergebnisdaten, falls Intro-Prüfung schon gemacht wurde
     * (kann null sein, wenn noch nicht gemacht)
     */
    private Double introExamPercentage;
    private String introExamLevel;

    // alter Konstruktor bleibt erhalten (Abwärtskompatibel)
    public AuthResponseDto(String token, String username) {
        this.token = token;
        this.username = username;
        this.introExamRequired = false; // default
        this.introExamPercentage = null;
        this.introExamLevel = null;
    }

    // Neuer Konstruktor für Login mit Intro-Status
    public AuthResponseDto(String token, String username, boolean introExamRequired) {
        this.token = token;
        this.username = username;
        this.introExamRequired = introExamRequired;
        this.introExamPercentage = null;
        this.introExamLevel = null;
    }

    // Voller Konstruktor (falls du direkt alle Werte setzen willst)
    public AuthResponseDto(String token,
                           String username,
                           boolean introExamRequired,
                           Double introExamPercentage,
                           String introExamLevel) {
        this.token = token;
        this.username = username;
        this.introExamRequired = introExamRequired;
        this.introExamPercentage = introExamPercentage;
        this.introExamLevel = introExamLevel;
    }

    public AuthResponseDto() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isIntroExamRequired() {
        return introExamRequired;
    }

    public void setIntroExamRequired(boolean introExamRequired) {
        this.introExamRequired = introExamRequired;
    }

    public Double getIntroExamPercentage() {
        return introExamPercentage;
    }

    public void setIntroExamPercentage(Double introExamPercentage) {
        this.introExamPercentage = introExamPercentage;
    }

    public String getIntroExamLevel() {
        return introExamLevel;
    }

    public void setIntroExamLevel(String introExamLevel) {
        this.introExamLevel = introExamLevel;
    }
}
