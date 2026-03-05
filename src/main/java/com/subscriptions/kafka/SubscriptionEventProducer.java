package com.subscriptions.kafka;

import com.subscriptions.entity.Subscription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishCreated(Subscription sub) {
        kafkaTemplate.send("subscription.created", sub.getCustomerId(), buildEvent(sub, "SUBSCRIPTION_CREATED"));
        log.info("Published subscription.created id={}", sub.getId());
    }

    public void publishRenewed(Subscription sub) {
        kafkaTemplate.send("subscription.renewed", sub.getCustomerId(), buildEvent(sub, "SUBSCRIPTION_RENEWED"));
        log.info("Published subscription.renewed id={}", sub.getId());
    }

    public void publishPaused(Subscription sub) {
        kafkaTemplate.send("subscription.paused", sub.getCustomerId(), buildEvent(sub, "SUBSCRIPTION_PAUSED"));
        log.info("Published subscription.paused id={}", sub.getId());
    }

    private Map<String, Object> buildEvent(Subscription sub, String type) {
        Map<String, Object> e = new HashMap<>();
        e.put("eventType", type);
        e.put("subscriptionId", sub.getId());
        e.put("customerId", sub.getCustomerId());
        e.put("productId", sub.getProductId());
        e.put("quantity", sub.getQuantity());
        e.put("frequency", sub.getFrequency());
        e.put("nextDeliveryDate", sub.getNextDeliveryDate() != null ? sub.getNextDeliveryDate().toString() : null);
        e.put("discountPercent", sub.getDiscountPercent());
        e.put("timestamp", System.currentTimeMillis());
        return e;
    }
}
