package com.subscriptions.kafka;
import com.subscriptions.entity.Subscription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component @RequiredArgsConstructor @Slf4j
public class SubscriptionEventProducer {
    private final KafkaTemplate<String,Object> kafkaTemplate;
    public void publishCreated(Subscription s) { send("subscription.created", s, "SUBSCRIPTION_CREATED"); }
    public void publishRenewed(Subscription s) { send("subscription.renewed", s, "SUBSCRIPTION_RENEWED"); }
    public void publishPaused(Subscription s)  { send("subscription.paused",  s, "SUBSCRIPTION_PAUSED");  }
    private void send(String topic, Subscription s, String type) {
        Map<String,Object> e=new HashMap<>();
        e.put("eventType",type); e.put("subscriptionId",s.getId());
        e.put("customerId",s.getCustomerId()); e.put("productId",s.getProductId());
        e.put("quantity",s.getQuantity()); e.put("frequency",s.getFrequency().toString());
        e.put("nextDeliveryDate", s.getNextDeliveryDate()!=null ? s.getNextDeliveryDate().toString() : null);
        e.put("discountPercent",s.getDiscountPercent()); e.put("timestamp",System.currentTimeMillis());
        kafkaTemplate.send(topic, s.getCustomerId(), e);
        log.info("Published {} id={}", type, s.getId());
    }
}
