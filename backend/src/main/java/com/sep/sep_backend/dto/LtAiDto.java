package com.sep.sep_backend.dto;

import java.util.List;

/**
 * DTO file "record" to avoid boilerplate code
 * one instance of Lueckentext Task
 * @param german German phrase
 * @param englishWithGap english translation with gap
 * @param missingWord correct word to be put into the gap
 * @param options options for multiple choice mode
 * @param synonyms for missingWord
 */
public record LtAiDto(
        String german,
        String englishWithGap,
        String missingWord,
        List<String> options,
        List<String> synonyms){}
