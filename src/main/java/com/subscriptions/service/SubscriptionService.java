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

@Service @RequiredArgsConstructor @Slf4j
public class SubscriptionService {
    private final SubscriptionRepository repo;
    private final SubscriptionEventProducer producer;
    private static final BigDecimal DISCOUNT = new BigDecimal("10.00");
    private static final int DEFAULT_SKIPS = 3;

    @Transactional
    public SubscriptionResponse create(SubscriptionRequest req) {
        Subscription s = Subscription.builder()
            .customerId(req.getCustomerId()).productId(req.getProductId())
            .quantity(req.getQuantity()).frequency(req.getFrequency())
            .status(Subscription.SubscriptionStatus.ACTIVE)
            .price(BigDecimal.ZERO).discountPercent(DISCOUNT)
            .nextDeliveryDate(nextDate(LocalDate.now(), req.getFrequency()))
            .skipsRemaining(DEFAULT_SKIPS)
            .paymentMethodId(req.getPaymentMethodId())
            .deliveryAddressId(req.getDeliveryAddressId())
            .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        s = repo.save(s);
        log.info("Subscription created id={} customer={} freq={}", s.getId(), s.getCustomerId(), s.getFrequency());
        producer.publishCreated(s);
        return SubscriptionResponse.from(s);
    }

    @Transactional
    public SubscriptionResponse pause(Long id) {
        Subscription s = find(id);
        if (s.getStatus()!=Subscription.SubscriptionStatus.ACTIVE)
            throw new SubscriptionException("Can only pause ACTIVE subscriptions");
        s.setStatus(Subscription.SubscriptionStatus.PAUSED);
        s.setPausedAt(LocalDateTime.now()); s.setUpdatedAt(LocalDateTime.now());
        s = repo.save(s); producer.publishPaused(s);
        return SubscriptionResponse.from(s);
    }

    @Transactional
    public SubscriptionResponse resume(Long id) {
        Subscription s = find(id);
        if (s.getStatus()!=Subscription.SubscriptionStatus.PAUSED)
            throw new SubscriptionException("Can only resume PAUSED subscriptions");
        s.setStatus(Subscription.SubscriptionStatus.ACTIVE);
        s.setResumedAt(LocalDateTime.now());
        s.setNextDeliveryDate(nextDate(LocalDate.now(), s.getFrequency()));
        s.setUpdatedAt(LocalDateTime.now());
        return SubscriptionResponse.from(repo.save(s));
    }

    @Transactional
    public SubscriptionResponse skipDelivery(Long id) {
        Subscription s = find(id);
        if (s.getStatus()!=Subscription.SubscriptionStatus.ACTIVE)
            throw new SubscriptionException("Can only skip ACTIVE subscriptions");
        if (s.getSkipsRemaining()<=0) throw new SubscriptionException("No skips remaining");
        s.setSkipsRemaining(s.getSkipsRemaining()-1);
        s.setNextDeliveryDate(nextDate(s.getNextDeliveryDate(), s.getFrequency()));
        s.setUpdatedAt(LocalDateTime.now());
        return SubscriptionResponse.from(repo.save(s));
    }

    @Transactional
    public SubscriptionResponse cancel(Long id) {
        Subscription s = find(id);
        s.setStatus(Subscription.SubscriptionStatus.CANCELLED); s.setUpdatedAt(LocalDateTime.now());
        return SubscriptionResponse.from(repo.save(s));
    }

    public SubscriptionResponse get(Long id) { return SubscriptionResponse.from(find(id)); }
    public List<SubscriptionResponse> getByCustomer(String cid) {
        return repo.findByCustomerId(cid).stream().map(SubscriptionResponse::from).collect(Collectors.toList());
    }

    @Scheduled(cron="0 0 8 * * *")
    @Transactional
    public void processRenewals() {
        List<Subscription> due = repo.findByStatusAndNextDeliveryDateLessThanEqual(
            Subscription.SubscriptionStatus.ACTIVE, LocalDate.now());
        log.info("Processing {} renewals", due.size());
        for (Subscription s : due) {
            try {
                s.setLastDeliveryDate(s.getNextDeliveryDate());
                s.setNextDeliveryDate(nextDate(s.getNextDeliveryDate(), s.getFrequency()));
                s.setUpdatedAt(LocalDateTime.now()); repo.save(s);
                producer.publishRenewed(s);
                log.info("Renewed subscription id={} next={}", s.getId(), s.getNextDeliveryDate());
            } catch (Exception ex) { log.error("Renewal failed id={}: {}", s.getId(), ex.getMessage()); }
        }
    }

    private Subscription find(Long id) {
        return repo.findById(id).orElseThrow(()->new SubscriptionException("Subscription not found: "+id));
    }
    private LocalDate nextDate(LocalDate from, Subscription.Frequency freq) {
        return switch(freq) {
            case WEEKLY    -> from.plusWeeks(1);
            case BIWEEKLY  -> from.plusWeeks(2);
            case MONTHLY   -> from.plusMonths(1);
            case QUARTERLY -> from.plusMonths(3);
        };
    }
}
