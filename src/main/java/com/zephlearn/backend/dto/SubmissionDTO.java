package com.zephlearn.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionDTO {
    private Long id;
    private Long userId;
    private Long problemId;
    private String problem;
    private String code;
    private String language;
    private String verdict;
    private String executionTime;
    private String memory;
    private LocalDateTime submittedAt;
}
