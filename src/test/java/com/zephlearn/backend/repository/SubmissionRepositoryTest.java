package com.zephlearn.backend.repository;

import com.zephlearn.backend.model.Submission;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class SubmissionRepositoryTest {

    @Autowired private SubmissionRepository submissionRepository;

    @Test
    @DisplayName("should_FindSubmissionsByUserId_OrderedBySubmittedAtDesc")
    void should_FindSubmissionsByUserId_OrderedBySubmittedAtDesc() {
        Submission sub1 = Submission.builder().userId(1L).problemId(10L).code("code1").verdict("Accepted").build();
        Submission sub2 = Submission.builder().userId(1L).problemId(20L).code("code2").verdict("Wrong Answer").build();
        Submission subOther = Submission.builder().userId(2L).problemId(10L).code("code3").verdict("Accepted").build();

        submissionRepository.save(sub1);
        submissionRepository.save(sub2);
        submissionRepository.save(subOther);

        List<Submission> userSubmissions = submissionRepository.findByUserIdOrderBySubmittedAtDesc(1L);

        assertThat(userSubmissions).hasSize(2);
        assertThat(userSubmissions).extracting(Submission::getProblemId).containsExactlyInAnyOrder(10L, 20L);
    }

    @Test
    @DisplayName("should_FindSubmissionsByUserIdAndProblemId")
    void should_FindSubmissionsByUserIdAndProblemId() {
        Submission sub1 = Submission.builder().userId(1L).problemId(10L).code("code1").verdict("Accepted").build();
        Submission sub2 = Submission.builder().userId(1L).problemId(20L).code("code2").verdict("Wrong Answer").build();

        submissionRepository.save(sub1);
        submissionRepository.save(sub2);

        List<Submission> filtered = submissionRepository.findByUserIdAndProblemIdOrderBySubmittedAtDesc(1L, 10L);

        assertThat(filtered).hasSize(1);
        assertThat(filtered.get(0).getCode()).isEqualTo("code1");
    }
}
