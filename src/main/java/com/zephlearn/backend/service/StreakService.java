package com.zephlearn.backend.service;

import com.zephlearn.backend.model.User;
import com.zephlearn.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class StreakService {

    private final UserRepository userRepository;

    public StreakService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User updateStreak(User user) {
        LocalDate today = LocalDate.now();
        LocalDate lastActiveDate = user.getLastActiveDate();
        int streak = user.getStreak();

        if (lastActiveDate == null) {
            streak = 1;
        } else if (lastActiveDate.equals(today)) {
            // already active today — keep streak unchanged
        } else if (lastActiveDate.equals(today.minusDays(1))) {
            streak = streak + 1;
        } else {
            // missed one or more days (or clock skew) — reset
            streak = 1;
        }

        user.setStreak(streak);
        user.setLastActiveDate(today);
        return userRepository.save(user);
    }
}
