package com.zephlearn.backend.controller;

import com.zephlearn.backend.dto.ProblemDTO;
import com.zephlearn.backend.service.ProblemService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @GetMapping
    public ResponseEntity<List<ProblemDTO>> getAll() {
        return ResponseEntity.ok(problemService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProblemDTO> getById(@PathVariable Long id) {
        // Authenticated because SecurityConfig requires it for any request that is not explicitly permitted
        return ResponseEntity.ok(problemService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProblemDTO> create(@RequestBody ProblemDTO dto) {
        return ResponseEntity.ok(problemService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProblemDTO> update(@PathVariable Long id, @RequestBody ProblemDTO dto) {
        return ResponseEntity.ok(problemService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        problemService.delete(id);
        return ResponseEntity.ok().build();
    }
}
