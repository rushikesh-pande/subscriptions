package com.ecommerce.subscriptions.dto;

import com.ecommerce.subscriptions.entity.DeliveryFrequency;
import com.ecommerce.subscriptions.entity.SubscriptionStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionResponseDto {
    private Long id;
    private Long customerId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private DeliveryFrequency frequency;
    private SubscriptionStatus status;
    private BigDecimal unitPrice;
    private BigDecimal discountPercentage;
    private BigDecimal discountedPrice;
    private LocalDate nextDeliveryDate;
    private LocalDateTime lastRenewedAt;
    private int skipCount;
    private int totalRenewals;
    private String paymentMethodId;
    private LocalDateTime createdAt;
    private LocalDateTime cancelledAt;
}
