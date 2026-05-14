package com.zephlearn.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String role; // "USER" or "ADMIN"

    @Builder.Default
    private int streak = 0;

    private LocalDate lastActiveDate;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
