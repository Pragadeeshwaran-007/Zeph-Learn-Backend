package com.zephlearn.backend.config;

import com.zephlearn.backend.dto.ProblemDTO;
import com.zephlearn.backend.dto.TestCaseDTO;
import com.zephlearn.backend.model.User;
import com.zephlearn.backend.repository.UserRepository;
import com.zephlearn.backend.service.ProblemService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProblemService problemService;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder, ProblemService problemService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.problemService = problemService;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (userRepository.findByEmail("admin@zephlearn.com").isEmpty()) {
            userRepository.save(User.builder()
                    .name("Admin")
                    .email("admin@zephlearn.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role("ADMIN")
                    .build());
        }

        if (userRepository.findByEmail("user@zephlearn.com").isEmpty()) {
            userRepository.save(User.builder()
                    .name("Test User")
                    .email("user@zephlearn.com")
                    .password(passwordEncoder.encode("user123"))
                    .role("USER")
                    .build());
        }

        if (problemService.getAll().isEmpty()) {
            seedProblems();
        }
    }

    private void seedProblems() {
        // 1. Two Sum
        ProblemDTO twoSum = new ProblemDTO();
        twoSum.setTitle("Two Sum");
        twoSum.setDifficulty("Easy");
        twoSum.setCategory("Array");
        twoSum.setTags("Array,Hash Table");
        twoSum.setDescription("Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target.");
        twoSum.setTestCases(Arrays.asList(
                new TestCaseDTO("4\n2 7 11 15\n9", "0 1", false),
                new TestCaseDTO("3\n3 2 4\n6", "1 2", false),
                new TestCaseDTO("2\n3 3\n6", "0 1", true),
                new TestCaseDTO("5\n-1 -2 -3 -4 -5\n-8", "2 4", true),
                new TestCaseDTO("4\n0 4 3 0\n0", "0 3", true)
        ));
        problemService.create(twoSum);

        // 2. Reverse String
        ProblemDTO revString = new ProblemDTO();
        revString.setTitle("Reverse String");
        revString.setDifficulty("Easy");
        revString.setCategory("String");
        revString.setTags("String,Two Pointers");
        revString.setDescription("Write a function that reverses a string. The input string is given as an array of characters s.");
        revString.setTestCases(Arrays.asList(
                new TestCaseDTO("hello", "olleh", false),
                new TestCaseDTO("Hannah", "hannaH", false),
                new TestCaseDTO("a", "a", true),
                new TestCaseDTO("ab", "ba", true),
                new TestCaseDTO("racecar", "racecar", true)
        ));
        problemService.create(revString);

        // 3. Valid Parentheses
        ProblemDTO validParentheses = new ProblemDTO();
        validParentheses.setTitle("Valid Parentheses");
        validParentheses.setDifficulty("Easy");
        validParentheses.setCategory("Stack");
        validParentheses.setTags("String,Stack");
        validParentheses.setDescription("Given a string s containing just the characters '(', ')', '{', '}', '[' and ']', determine if the input string is valid.");
        validParentheses.setTestCases(Arrays.asList(
                new TestCaseDTO("()", "true", false),
                new TestCaseDTO("()[]{}", "true", false),
                new TestCaseDTO("(]", "false", true),
                new TestCaseDTO("([)]", "false", true),
                new TestCaseDTO("{[]}", "true", true)
        ));
        problemService.create(validParentheses);

        // 4. Longest Substring Without Repeating Characters
        ProblemDTO longestSub = new ProblemDTO();
        longestSub.setTitle("Longest Substring Without Repeating Characters");
        longestSub.setDifficulty("Medium");
        longestSub.setCategory("Sliding Window");
        longestSub.setTags("Hash Table,String,Sliding Window");
        longestSub.setDescription("Given a string s, find the length of the longest substring without repeating characters.");
        longestSub.setTestCases(Arrays.asList(
                new TestCaseDTO("abcabcbb", "3", false),
                new TestCaseDTO("bbbbb", "1", false),
                new TestCaseDTO("pwwkew", "3", true),
                new TestCaseDTO("", "0", true),
                new TestCaseDTO("dvdf", "3", true)
        ));
        problemService.create(longestSub);

        // 5. Binary Search
        ProblemDTO binarySearch = new ProblemDTO();
        binarySearch.setTitle("Binary Search");
        binarySearch.setDifficulty("Easy");
        binarySearch.setCategory("Binary Search");
        binarySearch.setTags("Array,Binary Search");
        binarySearch.setDescription("Given an array of integers nums which is sorted in ascending order, and an integer target, write a function to search target in nums. If target exists, then return its index. Otherwise, return -1.");
        binarySearch.setTestCases(Arrays.asList(
                new TestCaseDTO("6\n-1 0 3 5 9 12\n9", "4", false),
                new TestCaseDTO("6\n-1 0 3 5 9 12\n2", "-1", false),
                new TestCaseDTO("1\n5\n5", "0", true),
                new TestCaseDTO("1\n5\n-5", "-1", true),
                new TestCaseDTO("2\n2 5\n5", "1", true)
        ));
        problemService.create(binarySearch);
    }
}
