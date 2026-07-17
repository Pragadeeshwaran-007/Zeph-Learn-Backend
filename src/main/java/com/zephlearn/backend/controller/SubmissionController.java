package com.zephlearn.backend.controller;

import com.zephlearn.backend.dto.RunResponse;
import com.zephlearn.backend.dto.SubmissionDTO;
import com.zephlearn.backend.dto.SubmissionRequest;
import com.zephlearn.backend.dto.SubmitResponse;
import com.zephlearn.backend.service.SubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.zephlearn.backend.model.User;
import com.zephlearn.backend.repository.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;
    private final UserRepository userRepository;

    public SubmissionController(SubmissionService submissionService, UserRepository userRepository) {
        this.submissionService = submissionService;
        this.userRepository = userRepository;
    }

    @PostMapping("/run")
    public ResponseEntity<RunResponse> run(@RequestBody SubmissionRequest request) {
        return ResponseEntity.ok(submissionService.run(request));
    }

    @PostMapping("/submit")
    public ResponseEntity<SubmitResponse> submit(@RequestBody SubmissionRequest request) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(submissionService.submit(userId, request));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SubmissionDTO>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(submissionService.getByUser(userId));
    }

    @GetMapping("/problem/{problemId}")
    public ResponseEntity<List<SubmissionDTO>> getByProblem(@PathVariable Long problemId) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(submissionService.getByProblem(userId, problemId));
    }
    
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
