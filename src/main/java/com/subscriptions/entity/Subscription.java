package com.subscriptions.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String customerId;

    @NotBlank
    private String productId;

    private int quantity;

    @Enumerated(EnumType.STRING)
    private Frequency frequency;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    private BigDecimal price;
    private BigDecimal discountPercent;  // subscription discount

    private LocalDate nextDeliveryDate;
    private LocalDate lastDeliveryDate;
    private int skipsRemaining;          // skip delivery option

    private String paymentMethodId;      // auto-payment token
    private String deliveryAddressId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime pausedAt;
    private LocalDateTime resumedAt;

    public enum Frequency { WEEKLY, BIWEEKLY, MONTHLY, QUARTERLY }
    public enum SubscriptionStatus { ACTIVE, PAUSED, CANCELLED, EXPIRED }
}
