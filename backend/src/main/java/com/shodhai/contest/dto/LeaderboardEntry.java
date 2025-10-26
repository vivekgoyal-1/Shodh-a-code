package com.shodhai.contest.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
@Data
@AllArgsConstructor
public class LeaderboardEntry {
    private String username;
    private Integer score;
    private Integer problemsSolved;
    private Integer rank;
    private LocalDateTime lastSubmissionTime;
}
