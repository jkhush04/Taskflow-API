package com.taskflow.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TaskflowApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskflowApiApplication.class, args);
    }

}
