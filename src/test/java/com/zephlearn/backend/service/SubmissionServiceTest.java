package com.zephlearn.backend.service;

import com.zephlearn.backend.dto.RunResponse;
import com.zephlearn.backend.dto.SubmissionDTO;
import com.zephlearn.backend.dto.SubmissionRequest;
import com.zephlearn.backend.dto.SubmitResponse;
import com.zephlearn.backend.model.Problem;
import com.zephlearn.backend.model.Submission;
import com.zephlearn.backend.model.TestCase;
import com.zephlearn.backend.repository.ProblemRepository;
import com.zephlearn.backend.repository.SubmissionRepository;
import com.zephlearn.backend.repository.TestCaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @Mock private Judge0Service judge0Service;
    @Mock private ProblemRepository problemRepository;
    @Mock private TestCaseRepository testCaseRepository;
    @Mock private SubmissionRepository submissionRepository;

    @InjectMocks private SubmissionService submissionService;

    private Problem sampleProblem;
    private TestCase sampleTestCase;

    @BeforeEach
    void setUp() {
        sampleProblem = Problem.builder()
                .id(1L)
                .title("Two Sum")
                .totalSubmissions(0)
                .acceptedSubmissions(0)
                .acceptanceRate(0.0)
                .build();

        sampleTestCase = new TestCase();
        sampleTestCase.setId(10L);
        sampleTestCase.setInput("1 2");
        sampleTestCase.setExpectedOutput("3");
        sampleTestCase.setProblem(sampleProblem);
    }

    @Test
    @DisplayName("should_ReturnAccepted_When_RunCodeMatchesExpectedOutput")
    void should_ReturnAccepted_When_RunCodeMatchesExpectedOutput() {
        SubmissionRequest request = new SubmissionRequest(1L, "print(1+2)", "python", "1 2", "3");
        RunResponse judgeResponse = new RunResponse("3", null, "Accepted", "0.05", "1024");

        when(judge0Service.getLanguageId("python")).thenReturn("71");
        when(judge0Service.execute("print(1+2)", "71", "1 2")).thenReturn(judgeResponse);

        RunResponse response = submissionService.run(request);

        assertThat(response.getVerdict()).isEqualTo("Accepted");
        assertThat(response.getStdout()).isEqualTo("3");
    }

    @Test
    @DisplayName("should_ReturnWrongAnswer_When_RunOutputDiffersFromExpected")
    void should_ReturnWrongAnswer_When_RunOutputDiffersFromExpected() {
        SubmissionRequest request = new SubmissionRequest(1L, "print(4)", "python", "1 2", "3");
        RunResponse judgeResponse = new RunResponse("4", null, "Accepted", "0.05", "1024");

        when(judge0Service.getLanguageId("python")).thenReturn("71");
        when(judge0Service.execute("print(4)", "71", "1 2")).thenReturn(judgeResponse);

        RunResponse response = submissionService.run(request);

        assertThat(response.getVerdict()).isEqualTo("Wrong Answer");
    }

    @Test
    @DisplayName("should_SubmitSuccessfully_When_AllTestCasesPass")
    void should_SubmitSuccessfully_When_AllTestCasesPass() {
        SubmissionRequest request = new SubmissionRequest(1L, "print(3)", "python", null, null);
        RunResponse judgeResponse = new RunResponse("3", null, "Accepted", "0.05", "1024");

        when(problemRepository.findById(1L)).thenReturn(Optional.of(sampleProblem));
        when(testCaseRepository.findByProblemId(1L)).thenReturn(List.of(sampleTestCase));
        when(judge0Service.getLanguageId("python")).thenReturn("71");
        when(judge0Service.execute("print(3)", "71", "1 2")).thenReturn(judgeResponse);

        SubmitResponse response = submissionService.submit(100L, request);

        assertThat(response.getVerdict()).isEqualTo("Accepted");
        assertThat(response.getPassedCount()).isEqualTo(1);
        assertThat(response.getTotalCount()).isEqualTo(1);
        verify(problemRepository).save(sampleProblem);
        verify(submissionRepository).save(any(Submission.class));
    }

    @Test
    @DisplayName("should_ThrowException_When_ProblemNotFoundOnSubmit")
    void should_ThrowException_When_ProblemNotFoundOnSubmit() {
        SubmissionRequest request = new SubmissionRequest(999L, "code", "java", null, null);
        when(problemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> submissionService.submit(100L, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Problem not found");
    }

    @Test
    @DisplayName("should_HandleEmptyTestCasesGracefully_When_Submitting")
    void should_HandleEmptyTestCasesGracefully_When_Submitting() {
        SubmissionRequest request = new SubmissionRequest(1L, "code", "java", null, null);

        when(problemRepository.findById(1L)).thenReturn(Optional.of(sampleProblem));
        when(testCaseRepository.findByProblemId(1L)).thenReturn(List.of());
        when(judge0Service.getLanguageId("java")).thenReturn("62");

        SubmitResponse response = submissionService.submit(100L, request);

        assertThat(response.getVerdict()).isEqualTo("Accepted");
        assertThat(response.getPassedCount()).isEqualTo(0);
        assertThat(response.getTotalCount()).isEqualTo(0);
    }
}
