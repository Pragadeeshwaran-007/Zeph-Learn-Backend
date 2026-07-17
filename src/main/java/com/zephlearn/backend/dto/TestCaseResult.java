package com.zephlearn.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestCaseResult {
    private String input;
    private String expectedOutput;
    private String actualOutput;
    private boolean passed;
    private String executionTime;
}
