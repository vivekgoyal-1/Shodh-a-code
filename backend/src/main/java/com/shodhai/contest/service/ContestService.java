package com.shodhai.contest.service;
import com.shodhai.contest.dto.*;
import com.shodhai.contest.entity.*;
import com.shodhai.contest.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class ContestService {
    private final ContestRepository contestRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;
    private final JudgeService judgeService;
    public ContestResponse getContest(String contestId) {
        Contest contest = contestRepository.findById(contestId).orElseThrow(() -> new RuntimeException("Contest not found"));
        ContestResponse response = new ContestResponse();
        response.setId(contest.getId());
        response.setName(contest.getName());
        response.setDescription(contest.getDescription());
        response.setStartTime(contest.getStartTime());
        response.setEndTime(contest.getEndTime());
        List<ContestResponse.ProblemSummary> problems = contest.getProblems().stream().map(p -> {
            ContestResponse.ProblemSummary ps = new ContestResponse.ProblemSummary();
            ps.setId(p.getId());
            ps.setTitle(p.getTitle());
            return ps;
        }).collect(Collectors.toList());
        response.setProblems(problems);
        return response;
    }
    public ProblemResponse getProblem(String problemId) {
        Problem problem = problemRepository.findById(problemId).orElseThrow(() -> new RuntimeException("Problem not found"));
        ProblemResponse response = new ProblemResponse();
        response.setId(problem.getId());
        response.setContestId(problem.getContest().getId());
        response.setTitle(problem.getTitle());
        response.setStatement(problem.getStatement());
        response.setInputFormat(problem.getInputFormat());
        response.setOutputFormat(problem.getOutputFormat());
        List<ProblemResponse.SampleTestCase> samples = problem.getTestCases().stream().filter(TestCase::isSample).map(tc -> {
            ProblemResponse.SampleTestCase stc = new ProblemResponse.SampleTestCase();
            stc.setInput(tc.getInput());
            stc.setExpectedOutput(tc.getExpectedOutput());
            return stc;
        }).collect(Collectors.toList());
        response.setSampleTestCases(samples);
        return response;
    }
    @Transactional
    public SubmissionResponse submitCode(SubmissionRequest request) {
        User user = userRepository.findByUsername(request.getUsername()).orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername(request.getUsername());
            return userRepository.save(newUser);
        });
        Problem problem = problemRepository.findById(request.getProblemId()).orElseThrow(() -> new RuntimeException("Problem not found"));
        Submission submission = new Submission();
        submission.setUser(user);
        submission.setProblem(problem);
        submission.setCode(request.getCode());
        submission.setLanguage(request.getLanguage());
        submission.setStatus(SubmissionStatus.PENDING);
        submission.setSubmittedAt(java.time.LocalDateTime.now());
        submission = submissionRepository.save(submission);
        judgeService.judgeSubmission(submission);
        SubmissionResponse response = new SubmissionResponse();
        response.setSubmissionId(submission.getId());
        response.setStatus(submission.getStatus().toString());
        response.setSubmittedAt(submission.getSubmittedAt());
        return response;
    }
    public SubmissionResponse getSubmissionStatus(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId).orElseThrow(() -> new RuntimeException("Submission not found"));
        SubmissionResponse response = new SubmissionResponse();
        response.setSubmissionId(submission.getId());
        response.setStatus(submission.getStatus().toString());
        response.setVerdict(submission.getVerdict());
        response.setOutput(submission.getOutput());
        response.setSubmittedAt(submission.getSubmittedAt());
        response.setExecutionTime(submission.getExecutionTime());
        response.setMemoryUsed(submission.getMemoryUsed());
        return response;
    }
    public List<LeaderboardEntry> getLeaderboard(String contestId) {
        List<Submission> acceptedSubmissions = submissionRepository.findAcceptedSubmissionsByContest(contestId);
        Map<String, LeaderboardStats> userStats = new HashMap<>();
        for (Submission sub : acceptedSubmissions) {
            String username = sub.getUser().getUsername();
            userStats.putIfAbsent(username, new LeaderboardStats());
            LeaderboardStats stats = userStats.get(username);
            stats.addProblem(sub.getProblem().getId());
            stats.updateLastSubmission(sub.getSubmittedAt());
        }
        List<LeaderboardEntry> leaderboard = userStats.entrySet().stream().map(entry -> new LeaderboardEntry(
            entry.getKey(), entry.getValue().getProblemsSolved() * 100, entry.getValue().getProblemsSolved(), 0, entry.getValue().getLastSubmissionTime()
        )).sorted(Comparator.comparing(LeaderboardEntry::getScore).reversed().thenComparing(LeaderboardEntry::getLastSubmissionTime)).collect(Collectors.toList());
        for (int i = 0; i < leaderboard.size(); i++) {
            leaderboard.get(i).setRank(i + 1);
        }
        return leaderboard;
    }
    private static class LeaderboardStats {
        private Set<String> solvedProblems = new HashSet<>();
        private java.time.LocalDateTime lastSubmissionTime;
        void addProblem(String problemId) {
            solvedProblems.add(problemId);
        }
        void updateLastSubmission(java.time.LocalDateTime time) {
            if (lastSubmissionTime == null || time.isAfter(lastSubmissionTime)) {
                lastSubmissionTime = time;
            }
        }
        int getProblemsSolved() {
            return solvedProblems.size();
        }
        java.time.LocalDateTime getLastSubmissionTime() {
            return lastSubmissionTime;
        }
    }
}
