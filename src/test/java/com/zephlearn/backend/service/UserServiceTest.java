package com.zephlearn.backend.service;

import com.zephlearn.backend.model.Submission;
import com.zephlearn.backend.model.User;
import com.zephlearn.backend.repository.SubmissionRepository;
import com.zephlearn.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private SubmissionRepository submissionRepository;
    @Mock private StreakService streakService;

    @InjectMocks private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .name("Alice")
                .email("alice@example.com")
                .role("USER")
                .streak(3)
                .build();
    }

    @Test
    @DisplayName("should_ReturnUserProfileWithRankAndSolvedCount_When_UserExists")
    void should_ReturnUserProfileWithRankAndSolvedCount_When_UserExists() {
        Submission sub1 = Submission.builder().id(101L).userId(1L).problemId(10L).verdict("Accepted").build();
        Submission sub2 = Submission.builder().id(102L).userId(1L).problemId(20L).verdict("Wrong Answer").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(streakService.updateStreak(sampleUser)).thenReturn(sampleUser);
        when(submissionRepository.findByUserIdOrderBySubmittedAtDesc(1L)).thenReturn(List.of(sub1, sub2));
        when(userRepository.findAll()).thenReturn(List.of(sampleUser));

        Map<String, Object> profile = userService.getProfile(1L);

        assertThat(profile.get("id")).isEqualTo(1L);
        assertThat(profile.get("email")).isEqualTo("alice@example.com");
        assertThat(profile.get("solvedCount")).isEqualTo(1L);
        assertThat((List<Long>) profile.get("solvedProblemIds")).containsExactly(10L);
        assertThat(profile.get("rank")).isEqualTo(1);
    }

    @Test
    @DisplayName("should_ThrowException_When_UserNotFoundOnGetProfile")
    void should_ThrowException_When_UserNotFoundOnGetProfile() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("should_DeleteUser_When_IdGiven")
    void should_DeleteUser_When_IdGiven() {
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }
}
