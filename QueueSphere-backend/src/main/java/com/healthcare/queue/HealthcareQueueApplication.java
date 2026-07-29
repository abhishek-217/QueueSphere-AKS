package com.healthcare.queue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HealthcareQueueApplication {
    public static void main(String[] args) {
        SpringApplication.run(HealthcareQueueApplication.class, args);
    }
}
