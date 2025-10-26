package com.shodhai.contest.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;
@Entity
@Data
public class Problem {
    @Id
    private String id;
    @ManyToOne
    @JoinColumn(name = "contest_id")
    private Contest contest;
    private String title;
    @Column(length = 5000)
    private String statement;
    @Column(length = 1000)
    private String inputFormat;
    @Column(length = 1000)
    private String outputFormat;
    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL)
    private List<TestCase> testCases = new ArrayList<>();
}
