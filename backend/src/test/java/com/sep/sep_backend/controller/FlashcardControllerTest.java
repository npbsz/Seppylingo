package com.sep.sep_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sep.sep_backend.dto.AnswerDto;
import com.sep.sep_backend.dto.AnswerResultDto;
import com.sep.sep_backend.dto.FlashcardDto;
import com.sep.sep_backend.service.FlashcardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class FlashcardControllerTest {

    @Mock
    private FlashcardService flashcardService;

    @InjectMocks
    private FlashcardController flashcardController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(flashcardController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void next_shouldReturnFlashcard() throws Exception {
        // given
        FlashcardDto dto =
                new FlashcardDto(5L, "to eat", "essen", "en-de");
        when(flashcardService.next("en-de")).thenReturn(dto);

        // when + then
        mockMvc.perform(get("/api/flashcards/next")
                        .param("dir", "en-de"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.front").value("to eat"))
                .andExpect(jsonPath("$.back").value("essen"))
                .andExpect(jsonPath("$.dir").value("en-de"));
    }

    @Test
    void answer_shouldReturnResult() throws Exception {
        // given
        AnswerDto input = new AnswerDto(5L, true);
        AnswerResultDto result =
                new AnswerResultDto(true, 3, null);

        when(flashcardService.answer(any(AnswerDto.class)))
                .thenReturn(result);

        // when + then
        mockMvc.perform(post("/api/flashcards/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.points").value(3))
                .andExpect(jsonPath("$.mistakes").doesNotExist());
    }


}
