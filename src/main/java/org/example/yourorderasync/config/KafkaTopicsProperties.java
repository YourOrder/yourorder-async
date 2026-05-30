package org.example.yourorderasync.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "kafka.topics")
public class KafkaTopicsProperties {
    private String orderCreated;
    private String stockReserved;
    private String paymentCompleted;
    private String paymentFailed;
    private String notificationSent;
    private String reportGenerated;
}