package tn.example.backdeclitech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@org.springframework.scheduling.annotation.EnableAsync
@org.springframework.cache.annotation.EnableCaching
public class BackDeclitechApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackDeclitechApplication.class, args);
    }

}
