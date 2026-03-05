package com.ecommerce.subscriptions.dto;

import com.ecommerce.subscriptions.entity.DeliveryFrequency;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionRequestDto {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "Product name is required")
    private String productName;

    @Positive(message = "Quantity must be positive")
    @Builder.Default
    private Integer quantity = 1;

    private DeliveryFrequency frequency;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.01", message = "Unit price must be positive")
    private BigDecimal unitPrice;

    private String paymentMethodId;

    private LocalDate startDate;
}
