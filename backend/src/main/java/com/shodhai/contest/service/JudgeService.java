package com.shodhai.contest.service;

import com.shodhai.contest.entity.*;
import com.shodhai.contest.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.*;
import java.nio.file.*;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class JudgeService {

    private final SubmissionRepository submissionRepository;

    @Value("${judge.docker.image:shodh-judge:latest}")
    private String dockerImage;

    @Value("${judge.docker.memory:256m}")
    private String memoryLimit;

    @Value("${judge.docker.cpus:0.5}")
    private String cpuLimit;

    @Value("${judge.docker.timeout:10}")
    private int timeoutSeconds;

    @Async
    @Transactional
    public void judgeSubmission(Submission submission) {
        try {
            Submission freshSubmission = submissionRepository.findById(submission.getId()).orElse(submission);
            freshSubmission.setStatus(SubmissionStatus.RUNNING);
            submissionRepository.save(freshSubmission);
            String workDir = createWorkDirectory(freshSubmission);

            for (TestCase testCase : freshSubmission.getProblem().getTestCases()) {
                String result = executeCode(freshSubmission, testCase, workDir);
                if (!result.equals("ACCEPTED")) {
                    freshSubmission.setStatus(SubmissionStatus.valueOf(result));
                    freshSubmission.setVerdict(result);
                    submissionRepository.save(freshSubmission);
                    cleanupWorkDirectory(workDir);
                    return;
                }
            }

            freshSubmission.setStatus(SubmissionStatus.ACCEPTED);
            freshSubmission.setVerdict("All test cases passed");
            submissionRepository.save(freshSubmission);
            cleanupWorkDirectory(workDir);
        } catch (Exception e) {
            log.error("Error judging submission: {}", e.getMessage(), e);
            try {
                Submission freshSubmission = submissionRepository.findById(submission.getId()).orElse(submission);
                freshSubmission.setStatus(SubmissionStatus.RUNTIME_ERROR);
                freshSubmission.setVerdict("System error: " + e.getMessage());
                submissionRepository.save(freshSubmission);
            } catch (Exception saveError) {
                log.error("Failed to save error status: {}", saveError.getMessage());
            }
        }
    }

    private String createWorkDirectory(Submission submission) throws IOException {
        String workDir = "/tmp/shodh-code/" + UUID.randomUUID().toString();
        Files.createDirectories(Paths.get(workDir));
        String fileName = getFileName(submission.getLanguage());
        Path codePath = Paths.get(workDir, fileName);
        Files.write(codePath, submission.getCode().getBytes());
        return workDir;
    }

    private String executeCode(Submission submission, TestCase testCase, String workDir) {
        try {
            Path inputPath = Paths.get(workDir, "input.txt");
            Path outputPath = Paths.get(workDir, "output.txt");
            Files.write(inputPath, testCase.getInput().getBytes());

            String command = buildDockerCommand(submission, workDir);
            log.info("Executing: {}", command);

            ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", command);
            pb.directory(new File(workDir));

            Process process = pb.start();
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                return "TIME_LIMIT_EXCEEDED";
            }

            int exitCode = process.exitValue();

            String errorOutput = "";
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                errorOutput = reader.lines().reduce("", (a, b) -> a + "\n" + b);
            }

            if (exitCode != 0) {
                log.error("Process failed with exit code {}: {}", exitCode, errorOutput);
                return "RUNTIME_ERROR";
            }

            if (!Files.exists(outputPath)) {
                log.error("Output file not created. Error: {}", errorOutput);
                return "RUNTIME_ERROR";
            }

            String actualOutput = Files.readString(outputPath).trim();
            String expectedOutput = testCase.getExpectedOutput().trim();

            log.info("Expected: '{}', Got: '{}'", expectedOutput, actualOutput);

            if (actualOutput.equals(expectedOutput)) {
                return "ACCEPTED";
            } else {
                submission.setOutput("Expected: " + expectedOutput + "\nGot: " + actualOutput);
                return "WRONG_ANSWER";
            }

        } catch (Exception e) {
            log.error("Execution error: {}", e.getMessage(), e);
            return "RUNTIME_ERROR";
        }
    }

    private String buildDockerCommand(Submission submission, String workDir) {
        String baseCommand = String.format(
            "docker run --rm -v %s:/code -w /code --memory=%s --cpus=%s --network=none %s ",
            workDir, memoryLimit, cpuLimit, dockerImage
        );

        switch (submission.getLanguage().toLowerCase()) {
            case "java":
                // Java: compile and run with stdin/stdout redirection
                return baseCommand + "sh -c 'javac Main.java && java Main < input.txt > output.txt 2>&1'";

            case "python":
                // Python: run with stdin/stdout redirection
                return baseCommand + "sh -c 'python3 main.py < input.txt > output.txt 2>&1'";

            case "cpp":
                // C++: compile with execute permission, then run with stdin/stdout redirection
                return baseCommand + "sh -c 'g++ -std=c++17 -o /tmp/a.out main.cpp && /tmp/a.out < input.txt > output.txt 2>&1'";

            default:
                return baseCommand + "sh -c 'echo Unsupported language > output.txt'";
        }
    }

    private String getFileName(String language) {
        switch (language.toLowerCase()) {
            case "java": return "Main.java";
            case "python": return "main.py";
            case "cpp": return "main.cpp";
            default: return "code.txt";
        }
    }

    private void cleanupWorkDirectory(String workDir) {
        try {
            Files.walk(Paths.get(workDir))
                .sorted((a, b) -> b.compareTo(a))
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        log.warn("Failed to delete: {}", path);
                    }
                });
        } catch (IOException e) {
            log.error("Cleanup error: {}", e.getMessage());
        }
    }
}
