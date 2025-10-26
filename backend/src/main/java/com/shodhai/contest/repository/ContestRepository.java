package com.shodhai.contest.repository;
import com.shodhai.contest.entity.Contest;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ContestRepository extends JpaRepository<Contest, String> {}
