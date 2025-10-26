package com.shodhai.contest.dto;
import lombok.Data;
@Data
public class SubmissionRequest {
    private String username;
    private String contestId;
    private String problemId;
    private String code;
    private String language;
}
