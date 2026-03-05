package com.ecommerce.subscriptions.service;

import com.ecommerce.subscriptions.dto.*;
import com.ecommerce.subscriptions.entity.*;
import com.ecommerce.subscriptions.exception.*;
import com.ecommerce.subscriptions.kafka.SubscriptionKafkaProducer;
import com.ecommerce.subscriptions.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionKafkaProducer kafkaProducer;

    @Value("${subscription.discount.percentage:10}")
    private BigDecimal defaultDiscountPercentage;

    @Value("${subscription.max-skip-count:3}")
    private int maxSkipCount;

    @Transactional
    public SubscriptionResponseDto createSubscription(SubscriptionRequestDto dto) {
        log.info("Creating subscription for customerId={}, productId={}", dto.getCustomerId(), dto.getProductId());

        DeliveryFrequency freq = dto.getFrequency() != null ? dto.getFrequency() : DeliveryFrequency.MONTHLY;
        LocalDate start = dto.getStartDate() != null ? dto.getStartDate() : LocalDate.now().plusDays(1);

        Subscription sub = Subscription.builder()
                .customerId(dto.getCustomerId())
                .productId(dto.getProductId())
                .productName(dto.getProductName())
                .quantity(dto.getQuantity() != null ? dto.getQuantity() : 1)
                .frequency(freq)
                .status(SubscriptionStatus.ACTIVE)
                .unitPrice(dto.getUnitPrice())
                .discountPercentage(defaultDiscountPercentage)
                .nextDeliveryDate(start)
                .paymentMethodId(dto.getPaymentMethodId())
                .createdAt(LocalDateTime.now())
                .build();

        Subscription saved = subscriptionRepository.save(sub);
        kafkaProducer.sendSubscriptionCreated(saved.getId(), saved.getCustomerId(), saved.getProductId());
        return mapToDto(saved);
    }

    public List<SubscriptionResponseDto> getCustomerSubscriptions(Long customerId) {
        return subscriptionRepository.findByCustomerId(customerId)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public List<SubscriptionResponseDto> getActiveSubscriptions(Long customerId) {
        return subscriptionRepository.findByCustomerIdAndStatus(customerId, SubscriptionStatus.ACTIVE)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public SubscriptionResponseDto getSubscription(Long id) {
        return mapToDto(findById(id));
    }

    @Transactional
    public SubscriptionResponseDto pauseSubscription(Long id) {
        Subscription sub = findById(id);
        if (sub.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new SubscriptionNotActiveException("Can only pause an ACTIVE subscription. Current status: " + sub.getStatus());
        }
        sub.setStatus(SubscriptionStatus.PAUSED);
        Subscription saved = subscriptionRepository.save(sub);
        kafkaProducer.sendSubscriptionPaused(saved.getId(), saved.getCustomerId());
        return mapToDto(saved);
    }

    @Transactional
    public SubscriptionResponseDto resumeSubscription(Long id) {
        Subscription sub = findById(id);
        if (sub.getStatus() != SubscriptionStatus.PAUSED) {
            throw new SubscriptionNotActiveException("Can only resume a PAUSED subscription. Current status: " + sub.getStatus());
        }
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.setNextDeliveryDate(LocalDate.now().plusDays(1));
        return mapToDto(subscriptionRepository.save(sub));
    }

    @Transactional
    public SubscriptionResponseDto cancelSubscription(Long id) {
        Subscription sub = findById(id);
        sub.setStatus(SubscriptionStatus.CANCELLED);
        sub.setCancelledAt(LocalDateTime.now());
        return mapToDto(subscriptionRepository.save(sub));
    }

    @Transactional
    public SubscriptionResponseDto skipDelivery(Long id) {
        Subscription sub = findById(id);
        if (sub.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new SubscriptionNotActiveException("Can only skip delivery for an ACTIVE subscription.");
        }
        if (sub.getSkipCount() >= maxSkipCount) {
            throw new SubscriptionNotActiveException("Maximum skip count (" + maxSkipCount + ") reached.");
        }
        sub.setSkipCount(sub.getSkipCount() + 1);
        sub.setNextDeliveryDate(calculateNextDeliveryDate(sub.getNextDeliveryDate(), sub.getFrequency()));
        return mapToDto(subscriptionRepository.save(sub));
    }

    @Transactional
    public SubscriptionResponseDto updateFrequency(Long id, DeliveryFrequency frequency) {
        Subscription sub = findById(id);
        sub.setFrequency(frequency);
        return mapToDto(subscriptionRepository.save(sub));
    }

    /**
     * Scheduled job — runs daily at 8 AM to process due renewals
     */
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void processScheduledRenewals() {
        List<Subscription> due = subscriptionRepository
                .findByStatusAndNextDeliveryDateLessThanEqual(SubscriptionStatus.ACTIVE, LocalDate.now());
        log.info("[Scheduler] Processing {} due subscription renewals", due.size());

        for (Subscription sub : due) {
            try {
                processRenewal(sub);
            } catch (Exception e) {
                log.error("[Scheduler] Failed to renew subscriptionId={}: {}", sub.getId(), e.getMessage());
            }
        }
    }

    @Transactional
    public SubscriptionResponseDto manualRenew(Long id) {
        Subscription sub = findById(id);
        if (sub.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new SubscriptionNotActiveException("Can only renew an ACTIVE subscription.");
        }
        return mapToDto(processRenewal(sub));
    }

    private Subscription processRenewal(Subscription sub) {
        sub.setTotalRenewals(sub.getTotalRenewals() + 1);
        sub.setLastRenewedAt(LocalDateTime.now());
        sub.setNextDeliveryDate(calculateNextDeliveryDate(sub.getNextDeliveryDate(), sub.getFrequency()));
        Subscription saved = subscriptionRepository.save(sub);
        kafkaProducer.sendSubscriptionRenewed(saved.getId(), saved.getCustomerId(), saved.getTotalRenewals());
        log.info("Renewed subscription={} renewal={}", saved.getId(), saved.getTotalRenewals());
        return saved;
    }

    private LocalDate calculateNextDeliveryDate(LocalDate from, DeliveryFrequency freq) {
        if (from == null) from = LocalDate.now();
        return switch (freq) {
            case WEEKLY     -> from.plusWeeks(1);
            case BIWEEKLY   -> from.plusWeeks(2);
            case MONTHLY    -> from.plusMonths(1);
            case QUARTERLY  -> from.plusMonths(3);
        };
    }

    private Subscription findById(Long id) {
        return subscriptionRepository.findById(id)
                .orElseThrow(() -> new SubscriptionNotFoundException(id));
    }

    private SubscriptionResponseDto mapToDto(Subscription s) {
        BigDecimal discounted = s.getUnitPrice()
                .multiply(BigDecimal.ONE.subtract(s.getDiscountPercentage().divide(BigDecimal.valueOf(100))))
                .setScale(2, RoundingMode.HALF_UP);

        return SubscriptionResponseDto.builder()
                .id(s.getId())
                .customerId(s.getCustomerId())
                .productId(s.getProductId())
                .productName(s.getProductName())
                .quantity(s.getQuantity())
                .frequency(s.getFrequency())
                .status(s.getStatus())
                .unitPrice(s.getUnitPrice())
                .discountPercentage(s.getDiscountPercentage())
                .discountedPrice(discounted)
                .nextDeliveryDate(s.getNextDeliveryDate())
                .lastRenewedAt(s.getLastRenewedAt())
                .skipCount(s.getSkipCount())
                .totalRenewals(s.getTotalRenewals())
                .paymentMethodId(s.getPaymentMethodId())
                .createdAt(s.getCreatedAt())
                .cancelledAt(s.getCancelledAt())
                .build();
    }
}
