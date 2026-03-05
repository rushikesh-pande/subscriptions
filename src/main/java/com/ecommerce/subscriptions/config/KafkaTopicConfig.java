package com.ecommerce.subscriptions.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${kafka.topic.subscription-created}")
    private String subscriptionCreatedTopic;

    @Value("${kafka.topic.subscription-renewed}")
    private String subscriptionRenewedTopic;

    @Value("${kafka.topic.subscription-paused}")
    private String subscriptionPausedTopic;

    @Bean
    public NewTopic subscriptionCreatedTopic() {
        return TopicBuilder.name(subscriptionCreatedTopic).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic subscriptionRenewedTopic() {
        return TopicBuilder.name(subscriptionRenewedTopic).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic subscriptionPausedTopic() {
        return TopicBuilder.name(subscriptionPausedTopic).partitions(3).replicas(1).build();
    }
}
