package com.zephlearn.backend.service;

import com.zephlearn.backend.dto.ProblemDTO;
import com.zephlearn.backend.dto.TestCaseDTO;
import com.zephlearn.backend.model.Problem;
import com.zephlearn.backend.model.TestCase;
import com.zephlearn.backend.repository.ProblemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProblemService {

    private final ProblemRepository problemRepository;

    public ProblemService(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }

    @Transactional
    public List<ProblemDTO> getAll() {
        return problemRepository.findAll().stream()
                .map(this::mapToDTOWithoutHiddenTestCases)
                .collect(Collectors.toList());
    }

    public long countAll() {
        return problemRepository.count();
    }

    public ProblemDTO getById(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Problem not found"));
        return mapToDTOWithoutHiddenTestCases(problem);
    }

    public ProblemDTO create(ProblemDTO dto) {
        Problem problem = new Problem();
        copyFields(dto, problem);
        
        if (dto.getTestCases() != null) {
            List<TestCase> testCases = dto.getTestCases().stream().map(tc -> {
                TestCase testCase = new TestCase();
                testCase.setInput(tc.getInput());
                testCase.setExpectedOutput(tc.getExpectedOutput());
                testCase.setHidden(tc.isHidden());
                testCase.setProblem(problem);
                return testCase;
            }).collect(Collectors.toList());
            problem.setTestCases(testCases);
        }

        Problem saved = problemRepository.save(problem);
        return mapToDTO(saved);
    }

    public ProblemDTO update(Long id, ProblemDTO dto) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Problem not found"));
        
        copyFields(dto, problem);
        
        problem.getTestCases().clear();
        if (dto.getTestCases() != null) {
            for (TestCaseDTO tc : dto.getTestCases()) {
                TestCase testCase = new TestCase();
                testCase.setInput(tc.getInput());
                testCase.setExpectedOutput(tc.getExpectedOutput());
                testCase.setHidden(tc.isHidden());
                testCase.setProblem(problem);
                problem.getTestCases().add(testCase);
            }
        }
        
        Problem saved = problemRepository.save(problem);
        return mapToDTO(saved);
    }

    public void delete(Long id) {
        problemRepository.deleteById(id);
    }

    private void copyFields(ProblemDTO dto, Problem problem) {
        problem.setTitle(dto.getTitle());
        problem.setDifficulty(dto.getDifficulty());
        problem.setCategory(dto.getCategory());
        problem.setDescription(dto.getDescription());
        problem.setInputFormat(dto.getInputFormat());
        problem.setOutputFormat(dto.getOutputFormat());
        problem.setConstraints(dto.getConstraints());
        problem.setTags(dto.getTags());
    }

    @Transactional
    private ProblemDTO mapToDTO(Problem problem) {
        ProblemDTO dto = new ProblemDTO();
        dto.setId(problem.getId());
        dto.setTitle(problem.getTitle());
        dto.setDifficulty(problem.getDifficulty());
        dto.setCategory(problem.getCategory());
        dto.setDescription(problem.getDescription());
        dto.setInputFormat(problem.getInputFormat());
        dto.setOutputFormat(problem.getOutputFormat());
        dto.setConstraints(problem.getConstraints());
        dto.setAcceptanceRate(problem.getAcceptanceRate());
        dto.setTotalSubmissions(problem.getTotalSubmissions());
        dto.setAcceptedSubmissions(problem.getAcceptedSubmissions());
        dto.setTags(problem.getTags());
        
        List<TestCaseDTO> tcDTOs = problem.getTestCases().stream().map(tc -> {
            TestCaseDTO tcDto = new TestCaseDTO();
            tcDto.setInput(tc.getInput());
            tcDto.setExpectedOutput(tc.getExpectedOutput());
            tcDto.setHidden(tc.isHidden());
            return tcDto;
        }).collect(Collectors.toList());
        dto.setTestCases(tcDTOs);
        
        return dto;
    }

    @Transactional
    private ProblemDTO mapToDTOWithoutHiddenTestCases(Problem problem) {
        ProblemDTO dto = mapToDTO(problem);
        List<TestCaseDTO> visibleOnly = dto.getTestCases().stream()
                .filter(tc -> !tc.isHidden())
                .collect(Collectors.toList());
        dto.setTestCases(visibleOnly);
        return dto;
    }
}
