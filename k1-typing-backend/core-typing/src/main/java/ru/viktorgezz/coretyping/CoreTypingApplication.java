package ru.viktorgezz.coretyping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "ru.viktorgezz")
@EnableScheduling
@EnableAsync
public class CoreTypingApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreTypingApplication.class, args);
    }

}
