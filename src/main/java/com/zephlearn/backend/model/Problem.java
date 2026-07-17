package com.zephlearn.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "problems")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    
    private String difficulty; // "Easy", "Medium", "Hard"
    
    private String category;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String inputFormat;
    
    @Column(columnDefinition = "TEXT")
    private String outputFormat;
    
    @Column(columnDefinition = "TEXT")
    private String constraints;
    
    @Builder.Default
    private Double acceptanceRate = 0.0;
    
    @Builder.Default
    private int totalSubmissions = 0;
    
    @Builder.Default
    private int acceptedSubmissions = 0;
    
    private String tags; // comma separated e.g. "Array,DP"
    
    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TestCase> testCases = new ArrayList<>();
}
