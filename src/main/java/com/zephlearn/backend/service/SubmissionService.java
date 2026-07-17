package com.zephlearn.backend.service;

import com.zephlearn.backend.dto.RunResponse;
import com.zephlearn.backend.dto.SubmissionDTO;
import com.zephlearn.backend.dto.SubmissionRequest;
import com.zephlearn.backend.dto.SubmitResponse;
import com.zephlearn.backend.dto.TestCaseResult;
import com.zephlearn.backend.model.Problem;
import com.zephlearn.backend.model.Submission;
import com.zephlearn.backend.model.TestCase;
import com.zephlearn.backend.repository.ProblemRepository;
import com.zephlearn.backend.repository.SubmissionRepository;
import com.zephlearn.backend.repository.TestCaseRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SubmissionService {

    private final Judge0Service judge0Service;
    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;
    private final SubmissionRepository submissionRepository;

    public SubmissionService(Judge0Service judge0Service, 
                             ProblemRepository problemRepository, 
                             TestCaseRepository testCaseRepository, 
                             SubmissionRepository submissionRepository) {
        this.judge0Service = judge0Service;
        this.problemRepository = problemRepository;
        this.testCaseRepository = testCaseRepository;
        this.submissionRepository = submissionRepository;
    }

    public RunResponse run(SubmissionRequest request) {
        String langId = judge0Service.getLanguageId(request.getLanguage());
        RunResponse runResponse = judge0Service.execute(request.getCode(), langId, request.getStdin());

        if ("Accepted".equals(runResponse.getVerdict())) {
            String actualOut = runResponse.getStdout() != null ? runResponse.getStdout().trim() : "";
            String expectedOut = request.getExpectedOutput() != null ? request.getExpectedOutput().trim() : "";
            if (!actualOut.equals(expectedOut)) {
                runResponse.setVerdict("Wrong Answer");
            }
        }
        return runResponse;
    }

    public SubmitResponse submit(Long userId, SubmissionRequest request) {
        Problem problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() -> new RuntimeException("Problem not found"));
        
        List<TestCase> testCases = testCaseRepository.findByProblemId(problem.getId());
        
        String langId = judge0Service.getLanguageId(request.getLanguage());
        
        List<TestCaseResult> results = new ArrayList<>();
        int passedCount = 0;
        String finalVerdict = "Accepted";
        double totalTime = 0.0;
        double maxMemory = 0.0;

        for (TestCase tc : testCases) {
            RunResponse runResponse = judge0Service.execute(request.getCode(), langId, tc.getInput());
            
            TestCaseResult result = new TestCaseResult();
            result.setInput(tc.getInput());
            result.setExpectedOutput(tc.getExpectedOutput());
            
            boolean passed = false;
            
            if ("Accepted".equals(runResponse.getVerdict())) {
                String actualOut = runResponse.getStdout() != null ? runResponse.getStdout().trim() : "";
                String expectedOut = tc.getExpectedOutput() != null ? tc.getExpectedOutput().trim() : "";
                
                if (actualOut.equals(expectedOut)) {
                    passed = true;
                } else {
                    runResponse.setVerdict("Wrong Answer");
                }
            }
            
            result.setActualOutput(runResponse.getStdout() != null ? runResponse.getStdout() : runResponse.getStderr());
            result.setPassed(passed);
            result.setExecutionTime(runResponse.getExecutionTime());
            
            results.add(result);
            
            if (passed) {
                passedCount++;
            } else if ("Accepted".equals(finalVerdict)) {
                // First failure dictates the verdict
                finalVerdict = runResponse.getVerdict();
            }

            try {
                if (runResponse.getExecutionTime() != null) {
                    totalTime += Double.parseDouble(runResponse.getExecutionTime());
                }
                if (runResponse.getMemory() != null) {
                    maxMemory = Math.max(maxMemory, Double.parseDouble(runResponse.getMemory()));
                }
            } catch (NumberFormatException ignored) {}
        }

        // Update Problem Stats
        problem.setTotalSubmissions(problem.getTotalSubmissions() + 1);
        if ("Accepted".equals(finalVerdict)) {
            problem.setAcceptedSubmissions(problem.getAcceptedSubmissions() + 1);
        }
        if (problem.getTotalSubmissions() > 0) {
            problem.setAcceptanceRate((double) problem.getAcceptedSubmissions() / problem.getTotalSubmissions() * 100);
        }
        problemRepository.save(problem);

        // Save Submission
        Submission submission = Submission.builder()
                .userId(userId)
                .problemId(problem.getId())
                .code(request.getCode())
                .language(request.getLanguage())
                .verdict(finalVerdict)
                .executionTime(String.format("%.3f", totalTime))
                .memory(String.valueOf(maxMemory))
                .build();
        submissionRepository.save(submission);

        SubmitResponse response = new SubmitResponse();
        response.setVerdict(finalVerdict);
        response.setPassedCount(passedCount);
        response.setTotalCount(testCases.size());
        response.setResults(results);
        response.setExecutionTime(String.format("%.3f", totalTime));
        response.setMemory(String.valueOf(maxMemory));
        
        return response;
    }

    public List<SubmissionDTO> getByUser(Long userId) {
        List<Submission> submissions = submissionRepository.findByUserIdOrderBySubmittedAtDesc(userId);
        if (submissions.isEmpty()) {
            return List.of();
        }

        Set<Long> problemIds = submissions.stream()
                .map(Submission::getProblemId)
                .collect(Collectors.toSet());

        Map<Long, String> problemTitles = problemRepository.findAllById(problemIds).stream()
                .collect(Collectors.toMap(Problem::getId, Problem::getTitle));

        return submissions.stream()
                .map(submission -> toDto(submission, problemTitles))
                .collect(Collectors.toList());
    }

    public List<SubmissionDTO> getByProblem(Long userId, Long problemId) {
        List<Submission> submissions = submissionRepository.findByUserIdAndProblemIdOrderBySubmittedAtDesc(userId, problemId);
        if (submissions.isEmpty()) {
            return List.of();
        }

        Problem problem = problemRepository.findById(problemId).orElse(null);
        String problemTitle = problem != null ? problem.getTitle() : "Unknown";
        Map<Long, String> problemTitles = Map.of(problemId, problemTitle);

        return submissions.stream()
                .map(submission -> toDto(submission, problemTitles))
                .collect(Collectors.toList());
    }

    private SubmissionDTO toDto(Submission submission, Map<Long, String> problemTitles) {
        Long problemId = submission.getProblemId();
        String problemTitle = problemId != null
                ? problemTitles.getOrDefault(problemId, "Unknown")
                : "Unknown";

        return SubmissionDTO.builder()
                .id(submission.getId())
                .userId(submission.getUserId())
                .problemId(problemId)
                .problem(problemTitle)
                .code(submission.getCode())
                .language(submission.getLanguage())
                .verdict(submission.getVerdict())
                .executionTime(submission.getExecutionTime())
                .memory(submission.getMemory())
                .submittedAt(submission.getSubmittedAt())
                .build();
    }
}
