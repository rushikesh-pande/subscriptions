package com.subscriptions.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_orders")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SubscriptionOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long subscriptionId;
    private String orderId;          // reference to create-order service
    private String customerId;
    private String productId;
    private int quantity;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime processedAt;

    public enum OrderStatus { PENDING, PLACED, FAILED, SKIPPED }
}
