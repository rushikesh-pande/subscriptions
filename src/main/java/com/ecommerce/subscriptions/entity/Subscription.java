package com.ecommerce.subscriptions.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @NotNull
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @NotBlank
    @Column(name = "product_name", nullable = false)
    private String productName;

    @Positive
    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = false)
    @Builder.Default
    private DeliveryFrequency frequency = DeliveryFrequency.MONTHLY;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @DecimalMin("0.00")
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "discount_percentage")
    @Builder.Default
    private BigDecimal discountPercentage = BigDecimal.valueOf(10);

    @Column(name = "next_delivery_date")
    private LocalDate nextDeliveryDate;

    @Column(name = "last_renewed_at")
    private LocalDateTime lastRenewedAt;

    @Column(name = "skip_count")
    @Builder.Default
    private int skipCount = 0;

    @Column(name = "total_renewals")
    @Builder.Default
    private int totalRenewals = 0;

    @Column(name = "payment_method_id")
    private String paymentMethodId;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
}
