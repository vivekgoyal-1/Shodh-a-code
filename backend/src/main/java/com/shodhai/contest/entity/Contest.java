package com.shodhai.contest.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity
@Data
public class Contest {
    @Id
    private String id;
    private String name;
    @Column(length = 1000)
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    @OneToMany(mappedBy = "contest", cascade = CascadeType.ALL)
    private List<Problem> problems = new ArrayList<>();
}
