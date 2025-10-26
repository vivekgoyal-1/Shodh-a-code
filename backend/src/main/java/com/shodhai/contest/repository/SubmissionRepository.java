package com.shodhai.contest.repository;
import com.shodhai.contest.entity.Submission;
import com.shodhai.contest.entity.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByStatus(SubmissionStatus status);
    @Query("SELECT s FROM Submission s WHERE s.problem.contest.id = :contestId AND s.status = 'ACCEPTED'")
    List<Submission> findAcceptedSubmissionsByContest(String contestId);
}
