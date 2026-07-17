package com.zephlearn.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProblemDTO {
    private Long id;
    private String title;
    private String difficulty;
    private String category;
    private String description;
    private String inputFormat;
    private String outputFormat;
    private String constraints;
    private Double acceptanceRate;
    private int totalSubmissions;
    private int acceptedSubmissions;
    private String tags;
    private List<TestCaseDTO> testCases;
}
