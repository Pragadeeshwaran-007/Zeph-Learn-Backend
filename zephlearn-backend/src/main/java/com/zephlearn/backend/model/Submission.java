package com.zephlearn.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "submissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    
    private Long problemId;

    @Column(columnDefinition = "TEXT")
    private String code;
    
    private String language;
    
    private String verdict; // "Accepted", "Wrong Answer", "Runtime Error", "TLE", "Compilation Error"
    
    private String executionTime;
    
    private String memory;
    
    @Builder.Default
    private LocalDateTime submittedAt = LocalDateTime.now();
}
