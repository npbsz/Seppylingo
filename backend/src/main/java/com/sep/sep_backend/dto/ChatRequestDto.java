package com.sep.sep_backend.dto;

public record ChatRequestDto(String message, String systemPrompt, String context) {}