package com.zephlearn.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @Column(nullable = true)
    @JsonIgnore
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) not null default 'LOCAL'")
    @Builder.Default
    private AuthProvider authProvider = AuthProvider.LOCAL;

    @Column(nullable = true)
    private String googleId;

    private String role; // "USER" or "ADMIN"

    @Builder.Default
    private int streak = 0;

    private LocalDate lastActiveDate;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
