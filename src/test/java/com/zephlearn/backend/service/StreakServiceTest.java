package com.zephlearn.backend.service;

import com.zephlearn.backend.model.User;
import com.zephlearn.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StreakServiceTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private StreakService streakService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Bob")
                .email("bob@example.com")
                .streak(5)
                .build();
    }

    @Test
    @DisplayName("should_SetStreakToOne_When_UserHasNoLastActiveDate")
    void should_SetStreakToOne_When_UserHasNoLastActiveDate() {
        user.setLastActiveDate(null);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User updated = streakService.updateStreak(user);

        assertThat(updated.getStreak()).isEqualTo(1);
        assertThat(updated.getLastActiveDate()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("should_KeepStreakUnchanged_When_UserAlreadyActiveToday")
    void should_KeepStreakUnchanged_When_UserAlreadyActiveToday() {
        user.setLastActiveDate(LocalDate.now());
        user.setStreak(5);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User updated = streakService.updateStreak(user);

        assertThat(updated.getStreak()).isEqualTo(5);
    }

    @Test
    @DisplayName("should_IncrementStreak_When_UserWasActiveYesterday")
    void should_IncrementStreak_When_UserWasActiveYesterday() {
        user.setLastActiveDate(LocalDate.now().minusDays(1));
        user.setStreak(5);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User updated = streakService.updateStreak(user);

        assertThat(updated.getStreak()).isEqualTo(6);
    }

    @Test
    @DisplayName("should_ResetStreakToOne_When_UserMissedDays")
    void should_ResetStreakToOne_When_UserMissedDays() {
        user.setLastActiveDate(LocalDate.now().minusDays(3));
        user.setStreak(5);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User updated = streakService.updateStreak(user);

        assertThat(updated.getStreak()).isEqualTo(1);
    }
}
