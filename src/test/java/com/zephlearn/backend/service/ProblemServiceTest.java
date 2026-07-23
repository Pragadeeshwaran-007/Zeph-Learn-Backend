package com.zephlearn.backend.service;

import com.zephlearn.backend.dto.ProblemDTO;
import com.zephlearn.backend.dto.TestCaseDTO;
import com.zephlearn.backend.model.Problem;
import com.zephlearn.backend.model.TestCase;
import com.zephlearn.backend.repository.ProblemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProblemServiceTest {

    @Mock private ProblemRepository problemRepository;
    @InjectMocks private ProblemService problemService;

    private Problem problem;

    @BeforeEach
    void setUp() {
        problem = Problem.builder()
                .id(1L)
                .title("Two Sum")
                .difficulty("Easy")
                .category("Array")
                .description("Find two numbers")
                .testCases(new ArrayList<>())
                .build();

        TestCase tc1 = new TestCase(10L, "1 2", "3", false, problem);
        TestCase tc2 = new TestCase(11L, "5 5", "10", true, problem);
        problem.getTestCases().add(tc1);
        problem.getTestCases().add(tc2);
    }

    @Test
    @DisplayName("should_ReturnAllProblemsWithoutHiddenTestCases_When_GetAll")
    void should_ReturnAllProblemsWithoutHiddenTestCases_When_GetAll() {
        when(problemRepository.findAll()).thenReturn(List.of(problem));

        List<ProblemDTO> result = problemService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTestCases()).hasSize(1); // hidden testcase filtered out
        assertThat(result.get(0).getTestCases().get(0).getInput()).isEqualTo("1 2");
    }

    @Test
    @DisplayName("should_ReturnProblemDTO_When_GetByIdExists")
    void should_ReturnProblemDTO_When_GetByIdExists() {
        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem));

        ProblemDTO dto = problemService.getById(1L);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getTitle()).isEqualTo("Two Sum");
        assertThat(dto.getTestCases()).hasSize(1); // hidden testcase filtered out
    }

    @Test
    @DisplayName("should_ThrowException_When_ProblemNotFoundById")
    void should_ThrowException_When_ProblemNotFoundById() {
        when(problemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> problemService.getById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Problem not found");
    }

    @Test
    @DisplayName("should_CreateAndSaveProblem_When_ValidDTOProvided")
    void should_CreateAndSaveProblem_When_ValidDTOProvided() {
        ProblemDTO inputDto = new ProblemDTO();
        inputDto.setTitle("Add Two Numbers");
        inputDto.setDifficulty("Medium");

        TestCaseDTO tcDto = new TestCaseDTO();
        tcDto.setInput("2 3");
        tcDto.setExpectedOutput("5");
        tcDto.setHidden(false);
        inputDto.setTestCases(List.of(tcDto));

        when(problemRepository.save(any(Problem.class))).thenAnswer(invocation -> {
            Problem p = invocation.getArgument(0);
            p.setId(2L);
            return p;
        });

        ProblemDTO created = problemService.create(inputDto);

        assertThat(created.getId()).isEqualTo(2L);
        assertThat(created.getTitle()).isEqualTo("Add Two Numbers");
        verify(problemRepository).save(any(Problem.class));
    }

    @Test
    @DisplayName("should_DeleteProblem_When_IdProvided")
    void should_DeleteProblem_When_IdProvided() {
        doNothing().when(problemRepository).deleteById(1L);

        problemService.delete(1L);

        verify(problemRepository).deleteById(1L);
    }
}
