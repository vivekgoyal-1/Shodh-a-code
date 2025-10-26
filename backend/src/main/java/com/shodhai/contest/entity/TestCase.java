package com.shodhai.contest.entity;
import jakarta.persistence.*;
import lombok.Data;
@Entity
@Data
public class TestCase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "problem_id")
    private Problem problem;
    @Column(length = 5000)
    private String input;
    @Column(length = 5000)
    private String expectedOutput;
    private boolean isSample;
}
