package com.zephlearn.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RunResponse {
    private String stdout;
    private String stderr;
    private String verdict;
    private String executionTime;
    private String memory;
}
