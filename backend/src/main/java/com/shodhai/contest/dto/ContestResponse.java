package com.shodhai.contest.dto;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
@Data
public class ContestResponse {
    private String id;
    private String name;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<ProblemSummary> problems;
    @Data
    public static class ProblemSummary {
        private String id;
        private String title;
    }
}
