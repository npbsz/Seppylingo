package com.sep.sep_backend.dto;

import java.util.List;

/**
 * List of LtAiDto so user can solve multiple instead of having to choose a theme each question
 * @param tasks List of LtAiDto items
 */
public record LtAiListResponseDto(
        List<LtAiDto> tasks
) {
}
