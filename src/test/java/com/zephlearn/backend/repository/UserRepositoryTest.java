package com.zephlearn.backend.repository;

import com.zephlearn.backend.model.AuthProvider;
import com.zephlearn.backend.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired private UserRepository userRepository;

    @Test
    @DisplayName("should_FindUserByEmail_When_UserExistsInDatabase")
    void should_FindUserByEmail_When_UserExistsInDatabase() {
        User user = User.builder()
                .name("Charlie")
                .email("charlie@example.com")
                .password("hashedpass")
                .authProvider(AuthProvider.LOCAL)
                .role("USER")
                .streak(0)
                .build();

        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("charlie@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Charlie");
    }

    @Test
    @DisplayName("should_ThrowDataIntegrityViolationException_When_EmailIsNotUnique")
    void should_ThrowDataIntegrityViolationException_When_EmailIsNotUnique() {
        User user1 = User.builder()
                .name("User 1")
                .email("duplicate@example.com")
                .authProvider(AuthProvider.LOCAL)
                .role("USER")
                .build();

        User user2 = User.builder()
                .name("User 2")
                .email("duplicate@example.com")
                .authProvider(AuthProvider.LOCAL)
                .role("USER")
                .build();

        userRepository.save(user1);

        assertThatThrownBy(() -> userRepository.saveAndFlush(user2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
