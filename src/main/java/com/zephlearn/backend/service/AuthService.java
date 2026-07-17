package com.zephlearn.backend.service;

import com.zephlearn.backend.dto.AuthRequest;
import com.zephlearn.backend.dto.AuthResponse;
import com.zephlearn.backend.dto.GoogleAuthRequest;
import com.zephlearn.backend.dto.LoginRequest;
import com.zephlearn.backend.model.AuthProvider;
import com.zephlearn.backend.model.User;
import com.zephlearn.backend.repository.UserRepository;
import com.zephlearn.backend.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StreakService streakService;
    private final GoogleTokenService googleTokenService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            StreakService streakService,
            GoogleTokenService googleTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.streakService = streakService;
        this.googleTokenService = googleTokenService;
    }

    public AuthResponse signup(AuthRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .authProvider(AuthProvider.LOCAL)
                .role("USER")
                .streak(0)
                .build();
        
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        return mapToAuthResponse(user, token);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (user.getAuthProvider() == AuthProvider.GOOGLE || user.getPassword() == null) {
            throw new RuntimeException("This account uses Google Sign-In. Please sign in with Google.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        User savedUser = streakService.updateStreak(user);

        String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole());

        return mapToAuthResponse(savedUser, token);
    }

    @Transactional
    public AuthResponse googleLogin(GoogleAuthRequest request) {
        GoogleTokenService.GoogleUserInfo googleUser = googleTokenService.verifyIdToken(request.getIdToken());

        User user = userRepository.findByEmail(googleUser.email())
                .map(existing -> linkGoogleAccount(existing, googleUser))
                .orElseGet(() -> createGoogleUser(googleUser));

        User savedUser = streakService.updateStreak(user);
        String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole());
        return mapToAuthResponse(savedUser, token);
    }

    private User linkGoogleAccount(User user, GoogleTokenService.GoogleUserInfo googleUser) {
        if (user.getGoogleId() == null) {
            user.setGoogleId(googleUser.googleId());
            return userRepository.save(user);
        }
        return user;
    }

    private User createGoogleUser(GoogleTokenService.GoogleUserInfo googleUser) {
        User user = User.builder()
                .name(googleUser.name())
                .email(googleUser.email())
                .password(null)
                .authProvider(AuthProvider.GOOGLE)
                .googleId(googleUser.googleId())
                .role("USER")
                .streak(0)
                .build();

        return userRepository.save(user);
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
