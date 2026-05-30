package org.example.yourorderasync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class YourorderAsyncApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourorderAsyncApplication.class, args);
    }
}
