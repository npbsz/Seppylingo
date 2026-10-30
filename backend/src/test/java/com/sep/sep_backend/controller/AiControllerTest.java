package com.sep.sep_backend.controller;

import com.sep.sep_backend.dto.ChatRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class AiControllerTest {

    @InjectMocks
    private AiController aiController;

    @Mock
    private RestTemplate restTemplate;

    private final String apiKey = "dummy-api-key";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // TRICK: Da die Felder im Controller private sind und RestTemplate mit 'new'
        // erstellt wurde, injizieren wir unsere Mocks gewaltsam via Reflection.
        ReflectionTestUtils.setField(aiController, "apiKey", apiKey);
        ReflectionTestUtils.setField(aiController, "restTemplate", restTemplate);
    }

    @Test
    void getAiResponse_Success() {
        // Arrange
        ChatRequestDto requestDto = new ChatRequestDto("System Prompt", "Context", "User Message");

        // Simuliere eine Antwort von Google Gemini
        Map<String, Object> mockResponseBody = Map.of("candidates", "some content");
        ResponseEntity<Object> mockResponse = new ResponseEntity<>(mockResponseBody, HttpStatus.OK);

        // Wir sagen dem Mock: Wenn postForEntity aufgerufen wird, gib unsere Fake-Antwort zurück.
        when(restTemplate.postForEntity(any(String.class), any(Map.class), eq(Object.class)))
                .thenReturn(mockResponse);

        // Act
        ResponseEntity<Object> response = aiController.getAiResponse(requestDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponseBody, response.getBody());
    }

    @Test
    void getAiResponse_HttpClientError() {
        // Arrange: Simuliere Fehler von der Gemini API (z.B. falscher Key oder Bad Request)
        ChatRequestDto requestDto = new ChatRequestDto("System Prompt", "Context", "User Message");

        // KORREKTUR: Wir müssen den Body explizit als Byte-Array übergeben,
        // damit e.getResponseBodyAsString() ihn später zurückgeben kann.
        // Der einfache Konstruktor (Status, Text) setzt nur den StatusText.
        HttpClientErrorException exception = new HttpClientErrorException(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                "Bad Request Body".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );

        // Mock wirft Exception
        when(restTemplate.postForEntity(any(String.class), any(Map.class), eq(Object.class)))
                .thenThrow(exception);

        // Act
        ResponseEntity<Object> response = aiController.getAiResponse(requestDto);

        // Assert - Der Controller fängt die Exception und gibt den Status weiter
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad Request Body", response.getBody());
    }

    @Test
    void getAiResponse_InternalServerError() {
        // Arrange: Simuliere einen unerwarteten Fehler (z.B. NullPointer im Code)
        ChatRequestDto requestDto = new ChatRequestDto("System Prompt", "Context", "User Message");
        RuntimeException exception = new RuntimeException("Unexpected Error");

        // Mock wirft allgemeine Exception
        when(restTemplate.postForEntity(any(String.class), any(Map.class), eq(Object.class)))
                .thenThrow(exception);

        // Act
        ResponseEntity<Object> response = aiController.getAiResponse(requestDto);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Interner Server Fehler: Unexpected Error", response.getBody());
    }
}