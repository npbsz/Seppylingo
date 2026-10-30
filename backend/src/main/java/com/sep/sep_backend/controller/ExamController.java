package com.sep.sep_backend.controller;

import com.sep.sep_backend.dto.ExamResultDto;
import com.sep.sep_backend.dto.ExamStartResponseDto;
import com.sep.sep_backend.dto.ExamSubmissionDto;
import com.sep.sep_backend.dto.IntroExamResultDto;
import com.sep.sep_backend.security.JwtUtil;
import com.sep.sep_backend.service.ExamService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exam")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:4200" })
public class ExamController {

    private final ExamService examService;
    private final JwtUtil jwtUtil;

    public ExamController(ExamService examService, JwtUtil jwtUtil) {
        this.examService = examService;
        this.jwtUtil = jwtUtil;
    }

    // =========================================================
    // NORMAL EXAM (wie bisher)
    // =========================================================

    @PostMapping("/simple/start")
    public ResponseEntity<ExamStartResponseDto> startSimpleExam() {
        ExamStartResponseDto response = examService.startSimpleExam();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/submit")
    public ResponseEntity<ExamResultDto> submitExam(@RequestBody ExamSubmissionDto submission) {
        ExamResultDto result = examService.evaluateExam(submission);
        return ResponseEntity.ok(result);
    }

    // =========================================================
    // INTRO EXAM (nur 1x pro User)
    // =========================================================

    @PostMapping("/intro/start")
    public ResponseEntity<?> startIntroExam(HttpServletRequest request) {
        String username = extractUsernameOrNull(request);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Kein/ungültiger Token");
        }

        try {
            ExamStartResponseDto response = examService.startIntroExam(username);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            // z.B. schon gemacht
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/intro/submit")
    public ResponseEntity<?> submitIntroExam(HttpServletRequest request,
                                             @RequestBody ExamSubmissionDto submission) {
        String username = extractUsernameOrNull(request);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Kein/ungültiger Token");
        }

        try {
            IntroExamResultDto result = examService.evaluateIntroExam(username, submission);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            // z.B. schon gemacht
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    // =========================================================
    // HELPER
    // =========================================================

    private String extractUsernameOrNull(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) return null;

        String token = auth.substring(7);
        if (!jwtUtil.validateToken(token)) return null;

        return jwtUtil.getUsernameFromToken(token);
    }
}
