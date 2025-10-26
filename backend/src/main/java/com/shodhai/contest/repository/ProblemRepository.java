package com.shodhai.contest.repository;
import com.shodhai.contest.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ProblemRepository extends JpaRepository<Problem, String> {
    List<Problem> findByContestId(String contestId);
}
