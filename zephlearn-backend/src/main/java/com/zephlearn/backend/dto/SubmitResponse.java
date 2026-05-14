package com.zephlearn.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitResponse {
    private String verdict;
    private int passedCount;
    private int totalCount;
    private List<TestCaseResult> results;
    private String executionTime;
    private String memory;
}
