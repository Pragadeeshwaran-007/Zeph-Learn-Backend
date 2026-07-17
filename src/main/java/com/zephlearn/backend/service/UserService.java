package com.zephlearn.backend.service;

import com.zephlearn.backend.model.User;
import com.zephlearn.backend.repository.SubmissionRepository;
import com.zephlearn.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;
    private final StreakService streakService;

    public UserService(
            UserRepository userRepository,
            SubmissionRepository submissionRepository,
            StreakService streakService) {
        this.userRepository = userRepository;
        this.submissionRepository = submissionRepository;
        this.streakService = streakService;
    }

    @Transactional
    public Map<String, Object> getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user = streakService.updateStreak(user);

        List<Long> solvedProblemIds = getSolvedProblems(userId);
        long solvedCount = solvedProblemIds.size();

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("name", user.getName());
        profile.put("email", user.getEmail());
        profile.put("role", user.getRole());
        profile.put("streak", user.getStreak());
        profile.put("solvedCount", solvedCount);
        profile.put("solvedProblemIds", solvedProblemIds);
        profile.put("rank", calculateRank(userId, solvedCount));

        return profile;
    }

    private int calculateRank(Long userId, long solvedCount) {
        int rank = 1;
        for (User other : userRepository.findAll()) {
            if (!other.getId().equals(userId) && getSolvedProblems(other.getId()).size() > solvedCount) {
                rank++;
            }
        }
        return rank;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public List<Long> getSolvedProblems(Long userId) {
        return submissionRepository.findByUserIdOrderBySubmittedAtDesc(userId).stream()
                .filter(sub -> "Accepted".equalsIgnoreCase(sub.getVerdict()))
                .map(sub -> sub.getProblemId())
                .distinct()
                .collect(Collectors.toList());
    }
}
