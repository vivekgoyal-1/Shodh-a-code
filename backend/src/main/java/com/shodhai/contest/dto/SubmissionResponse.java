package com.shodhai.contest.dto;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class SubmissionResponse {
    private Long submissionId;
    private String status;
    private String verdict;
    private String output;
    private LocalDateTime submittedAt;
    private Integer executionTime;
    private Integer memoryUsed;
}
