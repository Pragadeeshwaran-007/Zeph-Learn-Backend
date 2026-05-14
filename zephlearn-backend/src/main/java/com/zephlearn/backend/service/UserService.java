package com.zephlearn.backend.service;

import com.zephlearn.backend.model.User;
import com.zephlearn.backend.repository.SubmissionRepository;
import com.zephlearn.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;

    public UserService(UserRepository userRepository, SubmissionRepository submissionRepository) {
        this.userRepository = userRepository;
        this.submissionRepository = submissionRepository;
    }

    public Map<String, Object> getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        long solvedCount = getSolvedProblems(userId).size();

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("name", user.getName());
        profile.put("email", user.getEmail());
        profile.put("role", user.getRole());
        profile.put("streak", user.getStreak());
        profile.put("solvedCount", solvedCount);
        
        return profile;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public List<Long> getSolvedProblems(Long userId) {
        return submissionRepository.findByUserId(userId).stream()
                .filter(sub -> "Accepted".equalsIgnoreCase(sub.getVerdict()))
                .map(sub -> sub.getProblemId())
                .distinct()
                .collect(Collectors.toList());
    }
}
