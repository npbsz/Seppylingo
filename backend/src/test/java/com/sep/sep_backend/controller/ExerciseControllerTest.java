package com.sep.sep_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sep.sep_backend.dto.AnswerCheckRequestDto;
import com.sep.sep_backend.dto.ListExDTO;
import com.sep.sep_backend.dto.MultipleChoiceQuestionDto;
import com.sep.sep_backend.model.Vocabulary;
import com.sep.sep_backend.repository.VocabularyRepository;
import com.sep.sep_backend.security.JwtUtil;
import com.sep.sep_backend.service.CustomUserDetailsService;
import com.sep.sep_backend.service.ListExService;
import com.sep.sep_backend.service.LtService;
import com.sep.sep_backend.service.QuestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// NEUER IMPORT (statt MockBean)
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExerciseController.class)
public class ExerciseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // --- MOCKS ---

    @MockitoBean
    private VocabularyRepository vocabularyRepository;

    @MockitoBean
    private QuestionService questionService;

    @MockitoBean
    private ListExService listExService;

    @MockitoBean
    private LtService ltService;

    // --- SECURITY MOCKS (NEU) ---
    // Diese werden benötigt, damit der JwtAuthenticationFilter (Teil der Security-Chain) starten kann.

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtUtil jwtUtil;

    // --- TESTS ---

    @Test
    void getAllVocabulary_returnsList_200() throws Exception {
        Vocabulary vocab1 = new Vocabulary("Tisch", "table", "Nomen", "Zuhause", "A1");
        List<Vocabulary> vocabList = Arrays.asList(vocab1);

        when(vocabularyRepository.findAll()).thenReturn(vocabList);

        mockMvc.perform(get("/api/exercises/all")
                        .with(user("testUser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].wordDE").value("Tisch"));
    }

    @Test
    void getRandomVocabulary_returnsRandomVocab_200() throws Exception {
        Vocabulary vocab = new Vocabulary("Test", "TestEN", "Nomen", "TestTheme", "A1");
        // findAll muss eine Liste zurückgeben, damit random picken kann
        when(vocabularyRepository.findAll()).thenReturn(Arrays.asList(vocab));

        mockMvc.perform(get("/api/exercises/random")
                        .with(user("testUser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wordDE").value("Test"));
    }

    //@Test
    //void getNewListExItem_returnsItem_200() throws Exception {
      //  ListExDTO dto = new ListExDTO(UUID.randomUUID(), new String[]{"Phrase"});
        //when(listExService.createNewListEx()).thenReturn(dto);
//
   //      mockMvc.perform(get("/api/exercises/listEx/new")
     //                   .with(user("testUser")))
       //         .andExpect(status().isOk())
         //       .andExpect(jsonPath("$.id").exists());
    //}

    @Test
    void getMultipleChoiceQuestion_returnsQuestion_200() throws Exception {
        MultipleChoiceQuestionDto question = new MultipleChoiceQuestionDto();
        question.setWordToTranslate("dog");
        question.setWordId(1L);
        question.setOptions(List.of("dog", "cat"));

        when(questionService.createMultipleChoiceQuestion("Tiere")).thenReturn(question);

        mockMvc.perform(get("/api/exercises/multiple-choice/Tiere")
                        .with(user("testUser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wordToTranslate").value("dog"));
    }

    @Test
    void addVocabulary_validVocab_returnsCreated_201() throws Exception {
        Vocabulary newVocab = new Vocabulary("neu", "new", "Adjektiv", "Beschreibung", "C1");
        when(vocabularyRepository.save(any(Vocabulary.class))).thenReturn(newVocab);

        mockMvc.perform(post("/api/exercises")
                        .with(csrf())
                        .with(user("testUser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newVocab)))
                .andExpect(status().isCreated());
    }

    @Test
    void checkMultipleChoiceAnswer_returnsCorrect_200() throws Exception {
        AnswerCheckRequestDto request = new AnswerCheckRequestDto(1L, "richtig");
        when(questionService.checkAnswer(any(AnswerCheckRequestDto.class))).thenReturn(true);

        mockMvc.perform(post("/api/exercises/multiple-choice/check")
                        .with(csrf())
                        .with(user("testUser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}