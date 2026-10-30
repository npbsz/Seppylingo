package com.sep.sep_backend.dto;

import java.util.List;

public record FlashcardExplainResponseDto(
        String explanation,
        List<String> examples,
        List<String> usageTips
) {}
