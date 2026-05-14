package com.zephlearn.backend.service;

import com.zephlearn.backend.dto.AuthRequest;
import com.zephlearn.backend.dto.AuthResponse;
import com.zephlearn.backend.model.User;
import com.zephlearn.backend.repository.UserRepository;
import com.zephlearn.backend.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse signup(AuthRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .streak(0)
                .lastActiveDate(LocalDate.now())
                .build();
        
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        return mapToAuthResponse(user, token);
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        updateStreak(user);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        return mapToAuthResponse(user, token);
    }

    private void updateStreak(User user) {
        LocalDate today = LocalDate.now();
        if (user.getLastActiveDate() == null) {
            user.setStreak(1);
        } else if (user.getLastActiveDate().equals(today.minusDays(1))) {
            user.setStreak(user.getStreak() + 1);
        } else if (!user.getLastActiveDate().equals(today)) {
            user.setStreak(1);
        }
        user.setLastActiveDate(today);
    }

    private AuthResponse mapToAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .token(token)
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .streak(user.getStreak())
                .build();
    }
}
