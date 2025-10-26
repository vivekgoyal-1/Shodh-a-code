package com.shodhai.contest.controller;
import com.shodhai.contest.dto.*;
import com.shodhai.contest.service.ContestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ContestController {
    private final ContestService contestService;
    @GetMapping("/contests/{contestId}")
    public ResponseEntity<ContestResponse> getContest(@PathVariable String contestId) {
        return ResponseEntity.ok(contestService.getContest(contestId));
    }
    @GetMapping("/problems/{problemId}")
    public ResponseEntity<ProblemResponse> getProblem(@PathVariable String problemId) {
        return ResponseEntity.ok(contestService.getProblem(problemId));
    }
    @PostMapping("/submissions")
    public ResponseEntity<SubmissionResponse> submitCode(@RequestBody SubmissionRequest request) {
        return ResponseEntity.ok(contestService.submitCode(request));
    }
    @GetMapping("/submissions/{submissionId}")
    public ResponseEntity<SubmissionResponse> getSubmissionStatus(@PathVariable Long submissionId) {
        return ResponseEntity.ok(contestService.getSubmissionStatus(submissionId));
    }
    @GetMapping("/contests/{contestId}/leaderboard")
    public ResponseEntity<List<LeaderboardEntry>> getLeaderboard(@PathVariable String contestId) {
        return ResponseEntity.ok(contestService.getLeaderboard(contestId));
    }
}
