package com.shodhai.contest.dto;
import lombok.Data;
import java.util.List;
@Data
public class ProblemResponse {
    private String id;
    private String contestId;
    private String title;
    private String statement;
    private String inputFormat;
    private String outputFormat;
    private List<SampleTestCase> sampleTestCases;
    @Data
    public static class SampleTestCase {
        private String input;
        private String expectedOutput;
    }
}
