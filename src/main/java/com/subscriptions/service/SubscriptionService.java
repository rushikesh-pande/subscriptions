package com.subscriptions.service;

import com.subscriptions.dto.*;
import com.subscriptions.entity.Subscription;
import com.subscriptions.exception.SubscriptionException;
import com.subscriptions.kafka.SubscriptionEventProducer;
import com.subscriptions.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionEventProducer eventProducer;

    private static final BigDecimal SUBSCRIPTION_DISCOUNT = new BigDecimal("10.00");
    private static final int DEFAULT_SKIPS = 3;

    @Transactional
    public SubscriptionResponse createSubscription(SubscriptionRequest request) {
        Subscription sub = Subscription.builder()
            .customerId(request.getCustomerId())
            .productId(request.getProductId())
            .quantity(request.getQuantity())
            .frequency(request.getFrequency())
            .status(Subscription.SubscriptionStatus.ACTIVE)
            .price(BigDecimal.ZERO)   // price fetched from product service in real impl
            .discountPercent(SUBSCRIPTION_DISCOUNT)
            .nextDeliveryDate(calculateNextDelivery(LocalDate.now(), request.getFrequency()))
            .skipsRemaining(DEFAULT_SKIPS)
            .paymentMethodId(request.getPaymentMethodId())
            .deliveryAddressId(request.getDeliveryAddressId())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        sub = subscriptionRepository.save(sub);
        log.info("Subscription created: id={}, customer={}, product={}, freq={}",
            sub.getId(), sub.getCustomerId(), sub.getProductId(), sub.getFrequency());
        eventProducer.publishCreated(sub);
        return SubscriptionResponse.from(sub);
    }

    @Transactional
    public SubscriptionResponse pauseSubscription(Long id) {
        Subscription sub = findById(id);
        if (sub.getStatus() != Subscription.SubscriptionStatus.ACTIVE)
            throw new SubscriptionException("Can only pause ACTIVE subscriptions");
        sub.setStatus(Subscription.SubscriptionStatus.PAUSED);
        sub.setPausedAt(LocalDateTime.now());
        sub.setUpdatedAt(LocalDateTime.now());
        sub = subscriptionRepository.save(sub);
        eventProducer.publishPaused(sub);
        log.info("Subscription paused: id={}", id);
        return SubscriptionResponse.from(sub);
    }

    @Transactional
    public SubscriptionResponse resumeSubscription(Long id) {
        Subscription sub = findById(id);
        if (sub.getStatus() != Subscription.SubscriptionStatus.PAUSED)
            throw new SubscriptionException("Can only resume PAUSED subscriptions");
        sub.setStatus(Subscription.SubscriptionStatus.ACTIVE);
        sub.setResumedAt(LocalDateTime.now());
        sub.setNextDeliveryDate(calculateNextDelivery(LocalDate.now(), sub.getFrequency()));
        sub.setUpdatedAt(LocalDateTime.now());
        sub = subscriptionRepository.save(sub);
        log.info("Subscription resumed: id={}", id);
        return SubscriptionResponse.from(sub);
    }

    @Transactional
    public SubscriptionResponse skipNextDelivery(Long id) {
        Subscription sub = findById(id);
        if (sub.getStatus() != Subscription.SubscriptionStatus.ACTIVE)
            throw new SubscriptionException("Can only skip ACTIVE subscriptions");
        if (sub.getSkipsRemaining() <= 0)
            throw new SubscriptionException("No skips remaining");
        sub.setSkipsRemaining(sub.getSkipsRemaining() - 1);
        sub.setNextDeliveryDate(calculateNextDelivery(sub.getNextDeliveryDate(), sub.getFrequency()));
        sub.setUpdatedAt(LocalDateTime.now());
        log.info("Subscription delivery skipped: id={}, skipsLeft={}", id, sub.getSkipsRemaining());
        return SubscriptionResponse.from(subscriptionRepository.save(sub));
    }

    @Transactional
    public SubscriptionResponse cancelSubscription(Long id) {
        Subscription sub = findById(id);
        sub.setStatus(Subscription.SubscriptionStatus.CANCELLED);
        sub.setUpdatedAt(LocalDateTime.now());
        return SubscriptionResponse.from(subscriptionRepository.save(sub));
    }

    public List<SubscriptionResponse> getCustomerSubscriptions(String customerId) {
        return subscriptionRepository.findByCustomerId(customerId)
            .stream().map(SubscriptionResponse::from).collect(Collectors.toList());
    }

    public SubscriptionResponse getSubscription(Long id) {
        return SubscriptionResponse.from(findById(id));
    }

    /** Scheduled: runs daily at 08:00, renews due subscriptions */
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void processRenewals() {
        List<Subscription> due = subscriptionRepository
            .findByStatusAndNextDeliveryDateLessThanEqual(
                Subscription.SubscriptionStatus.ACTIVE, LocalDate.now());
        log.info("Processing {} subscription renewals", due.size());
        for (Subscription sub : due) {
            try {
                sub.setLastDeliveryDate(sub.getNextDeliveryDate());
                sub.setNextDeliveryDate(calculateNextDelivery(sub.getNextDeliveryDate(), sub.getFrequency()));
                sub.setUpdatedAt(LocalDateTime.now());
                subscriptionRepository.save(sub);
                eventProducer.publishRenewed(sub);
                log.info("Subscription renewed: id={}, next={}", sub.getId(), sub.getNextDeliveryDate());
            } catch (Exception ex) {
                log.error("Failed to renew subscription id={}: {}", sub.getId(), ex.getMessage());
            }
        }
    }

    private Subscription findById(Long id) {
        return subscriptionRepository.findById(id)
            .orElseThrow(() -> new SubscriptionException("Subscription not found: " + id));
    }

    private LocalDate calculateNextDelivery(LocalDate from, Subscription.Frequency freq) {
        return switch (freq) {
            case WEEKLY    -> from.plusWeeks(1);
            case BIWEEKLY  -> from.plusWeeks(2);
            case MONTHLY   -> from.plusMonths(1);
            case QUARTERLY -> from.plusMonths(3);
        };
    }
}
