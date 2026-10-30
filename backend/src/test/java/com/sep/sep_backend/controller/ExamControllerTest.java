// language: java
package com.sep.sep_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sep.sep_backend.dto.ExamResultDto;
import com.sep.sep_backend.dto.ExamStartResponseDto;
import com.sep.sep_backend.dto.ExamSubmissionDto;
import com.sep.sep_backend.dto.IntroExamResultDto;
import com.sep.sep_backend.security.JwtUtil;
import com.sep.sep_backend.service.ExamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ExamControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ExamService examService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private ExamController examController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String VALID_TOKEN = "valid.jwt.token";
    private final String TEST_USER = "testUser";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(examController).build();

        // lenient markieren, damit Tests ohne Token nicht wegen "unnecessary stubbing" fehlschlagen
        Mockito.lenient().when(jwtUtil.validateToken(VALID_TOKEN)).thenReturn(true);
        Mockito.lenient().when(jwtUtil.getUsernameFromToken(VALID_TOKEN)).thenReturn(TEST_USER);
    }

    @Test
    void startSimpleExam_Success() throws Exception {
        ExamStartResponseDto mockResponse = new ExamStartResponseDto();
        Mockito.when(examService.startSimpleExam()).thenReturn(mockResponse);

        mockMvc.perform(post("/api/exam/simple/start")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void submitExam_Success() throws Exception {
        ExamSubmissionDto submissionDto = new ExamSubmissionDto();
        ExamResultDto resultDto = new ExamResultDto();
        Mockito.when(examService.evaluateExam(any(ExamSubmissionDto.class))).thenReturn(resultDto);

        mockMvc.perform(post("/api/exam/submit")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submissionDto)))
                .andExpect(status().isOk());
    }

    @Test
    void startIntroExam_Success() throws Exception {
        ExamStartResponseDto mockResponse = new ExamStartResponseDto();
        Mockito.when(examService.startIntroExam(TEST_USER)).thenReturn(mockResponse);

        mockMvc.perform(post("/api/exam/intro/start")
                        .with(csrf())
                        .header("Authorization", "Bearer " + VALID_TOKEN))
                .andExpect(status().isOk());
    }

    @Test
    void startIntroExam_Unauthorized_NoToken() throws Exception {
        mockMvc.perform(post("/api/exam/intro/start")
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Kein/ungültiger Token"));
    }

    @Test
    void startIntroExam_Unauthorized_InvalidToken() throws Exception {
        String invalidToken = "invalid.token";
        Mockito.when(jwtUtil.validateToken(invalidToken)).thenReturn(false);

        mockMvc.perform(post("/api/exam/intro/start")
                        .with(csrf())
                        .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Kein/ungültiger Token"));
    }

    @Test
    void startIntroExam_Conflict_AlreadyTaken() throws Exception {
        Mockito.when(examService.startIntroExam(TEST_USER))
                .thenThrow(new IllegalStateException("Prüfung bereits absolviert"));

        mockMvc.perform(post("/api/exam/intro/start")
                        .with(csrf())
                        .header("Authorization", "Bearer " + VALID_TOKEN))
                .andExpect(status().isConflict())
                .andExpect(content().string("Prüfung bereits absolviert"));
    }

    @Test
    void submitIntroExam_Success() throws Exception {
        ExamSubmissionDto submissionDto = new ExamSubmissionDto();
        IntroExamResultDto resultDto = new IntroExamResultDto();

        Mockito.when(examService.evaluateIntroExam(eq(TEST_USER), any(ExamSubmissionDto.class)))
                .thenReturn(resultDto);

        mockMvc.perform(post("/api/exam/intro/submit")
                        .with(csrf())
                        .header("Authorization", "Bearer " + VALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submissionDto)))
                .andExpect(status().isOk());
    }

    @Test
    void submitIntroExam_BadRequest_InvalidSubmission() throws Exception {
        ExamSubmissionDto submissionDto = new ExamSubmissionDto();
        Mockito.when(examService.evaluateIntroExam(eq(TEST_USER), any(ExamSubmissionDto.class)))
                .thenThrow(new IllegalArgumentException("Ungültige Daten"));

        mockMvc.perform(post("/api/exam/intro/submit")
                        .with(csrf())
                        .header("Authorization", "Bearer " + VALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submissionDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Ungültige Daten"));
    }

    @Test
    void submitIntroExam_Conflict_AlreadyTaken() throws Exception {
        ExamSubmissionDto submissionDto = new ExamSubmissionDto();
        Mockito.when(examService.evaluateIntroExam(eq(TEST_USER), any(ExamSubmissionDto.class)))
                .thenThrow(new IllegalStateException("Prüfung bereits abgeschlossen"));

        mockMvc.perform(post("/api/exam/intro/submit")
                        .with(csrf())
                        .header("Authorization", "Bearer " + VALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submissionDto)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Prüfung bereits abgeschlossen"));
    }
}
