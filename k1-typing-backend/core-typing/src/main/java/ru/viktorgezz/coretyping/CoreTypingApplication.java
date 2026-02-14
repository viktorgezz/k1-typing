package ru.viktorgezz.coretyping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "ru.viktorgezz")
@EnableJpaRepositories(basePackages = "ru.viktorgezz")
@EntityScan(basePackages = "ru.viktorgezz")
@EnableScheduling
@EnableAsync
public class CoreTypingApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreTypingApplication.class, args);
    }

}
