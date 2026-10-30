package com.sep.sep_backend.dto;

public record FlashcardExplainRequestDto(
        String direction, // "en-de" | "de-en"
        String front,
        String back,
        Integer userScore // optional (kann null sein)
) {}
