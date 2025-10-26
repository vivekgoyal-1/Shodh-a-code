package com.shodhai.contest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
@SpringBootApplication
@EnableAsync
public class ContestApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContestApplication.class, args);
    }
}
