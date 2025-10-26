package com.shodhai.contest.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
@Entity
@Data
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne
    @JoinColumn(name = "problem_id")
    private Problem problem;
    @Column(length = 10000)
    private String code;
    private String language;
    @Enumerated(EnumType.STRING)
    private SubmissionStatus status;
    private String verdict;
    @Column(length = 5000)
    private String output;
    private LocalDateTime submittedAt;
    private Integer executionTime;
    private Integer memoryUsed;
}
