package ru.viktorgezz.k1_typing_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class K1TypingBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(K1TypingBackendApplication.class, args);
    }

}
