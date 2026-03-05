package com.subscriptions.dto;
import com.subscriptions.entity.Subscription;
import jakarta.validation.constraints.*;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SubscriptionRequest {
    @NotBlank private String customerId;
    @NotBlank private String productId;
    @Min(1) private int quantity;
    @NotNull private Subscription.Frequency frequency;
    @NotBlank private String paymentMethodId;
    @NotBlank private String deliveryAddressId;
}
