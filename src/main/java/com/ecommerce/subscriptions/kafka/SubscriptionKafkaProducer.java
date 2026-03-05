package com.ecommerce.subscriptions.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionKafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topic.subscription-created}")
    private String subscriptionCreatedTopic;

    @Value("${kafka.topic.subscription-renewed}")
    private String subscriptionRenewedTopic;

    @Value("${kafka.topic.subscription-paused}")
    private String subscriptionPausedTopic;

    public void sendSubscriptionCreated(Long subscriptionId, Long customerId, Long productId) {
        String message = String.format(
            "{\"subscriptionId\":%d,\"customerId\":%d,\"productId\":%d,\"event\":\"SUBSCRIPTION_CREATED\"}",
            subscriptionId, customerId, productId);
        kafkaTemplate.send(subscriptionCreatedTopic, String.valueOf(subscriptionId), message);
        log.info("[Kafka] Sent subscription.created → subscriptionId={}", subscriptionId);
    }

    public void sendSubscriptionRenewed(Long subscriptionId, Long customerId, int renewalNumber) {
        String message = String.format(
            "{\"subscriptionId\":%d,\"customerId\":%d,\"renewalNumber\":%d,\"event\":\"SUBSCRIPTION_RENEWED\"}",
            subscriptionId, customerId, renewalNumber);
        kafkaTemplate.send(subscriptionRenewedTopic, String.valueOf(subscriptionId), message);
        log.info("[Kafka] Sent subscription.renewed → subscriptionId={}, renewal={}", subscriptionId, renewalNumber);
    }

    public void sendSubscriptionPaused(Long subscriptionId, Long customerId) {
        String message = String.format(
            "{\"subscriptionId\":%d,\"customerId\":%d,\"event\":\"SUBSCRIPTION_PAUSED\"}",
            subscriptionId, customerId);
        kafkaTemplate.send(subscriptionPausedTopic, String.valueOf(subscriptionId), message);
        log.info("[Kafka] Sent subscription.paused → subscriptionId={}", subscriptionId);
    }
}
