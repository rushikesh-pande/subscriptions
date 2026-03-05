package com.ecommerce.subscriptions.dto;

import com.ecommerce.subscriptions.entity.DeliveryFrequency;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFrequencyDto {
    @NotNull(message = "Frequency is required")
    private DeliveryFrequency frequency;
}
