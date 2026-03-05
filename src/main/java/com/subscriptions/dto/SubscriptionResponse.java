package com.subscriptions.dto;
import com.subscriptions.entity.Subscription;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SubscriptionResponse {
    private Long id;
    private String customerId, productId;
    private int quantity;
    private Subscription.Frequency frequency;
    private Subscription.SubscriptionStatus status;
    private BigDecimal price, discountPercent;
    private LocalDate nextDeliveryDate, lastDeliveryDate;
    private int skipsRemaining;
    private LocalDateTime createdAt;
    public static SubscriptionResponse from(Subscription s) {
        return SubscriptionResponse.builder()
            .id(s.getId()).customerId(s.getCustomerId()).productId(s.getProductId())
            .quantity(s.getQuantity()).frequency(s.getFrequency()).status(s.getStatus())
            .price(s.getPrice()).discountPercent(s.getDiscountPercent())
            .nextDeliveryDate(s.getNextDeliveryDate()).lastDeliveryDate(s.getLastDeliveryDate())
            .skipsRemaining(s.getSkipsRemaining()).createdAt(s.getCreatedAt()).build();
    }
}
