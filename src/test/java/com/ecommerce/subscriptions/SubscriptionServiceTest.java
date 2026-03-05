package com.ecommerce.subscriptions;

import com.ecommerce.subscriptions.dto.SubscriptionRequestDto;
import com.ecommerce.subscriptions.dto.SubscriptionResponseDto;
import com.ecommerce.subscriptions.entity.*;
import com.ecommerce.subscriptions.exception.*;
import com.ecommerce.subscriptions.kafka.SubscriptionKafkaProducer;
import com.ecommerce.subscriptions.repository.SubscriptionRepository;
import com.ecommerce.subscriptions.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock SubscriptionRepository subscriptionRepository;
    @Mock SubscriptionKafkaProducer kafkaProducer;
    @InjectMocks SubscriptionService subscriptionService;

    @Test
    void createSubscription_ShouldReturnActiveStatus() {
        SubscriptionRequestDto dto = SubscriptionRequestDto.builder()
                .customerId(1L).productId(2L).productName("Coffee Beans")
                .quantity(1).frequency(DeliveryFrequency.MONTHLY)
                .unitPrice(BigDecimal.valueOf(29.99))
                .paymentMethodId("pm_test_123")
                .build();

        Subscription saved = Subscription.builder()
                .id(1L).customerId(1L).productId(2L).productName("Coffee Beans")
                .quantity(1).frequency(DeliveryFrequency.MONTHLY)
                .status(SubscriptionStatus.ACTIVE)
                .unitPrice(BigDecimal.valueOf(29.99))
                .discountPercentage(BigDecimal.valueOf(10))
                .nextDeliveryDate(LocalDate.now().plusDays(1))
                .build();

        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(saved);
        doNothing().when(kafkaProducer).sendSubscriptionCreated(anyLong(), anyLong(), anyLong());

        SubscriptionResponseDto result = subscriptionService.createSubscription(dto);

        assertNotNull(result);
        assertEquals(SubscriptionStatus.ACTIVE, result.getStatus());
        assertEquals(DeliveryFrequency.MONTHLY, result.getFrequency());
        verify(kafkaProducer).sendSubscriptionCreated(1L, 1L, 2L);
    }

    @Test
    void pauseSubscription_ShouldChangeToPaused() {
        Subscription sub = Subscription.builder()
                .id(1L).customerId(1L).productId(2L)
                .status(SubscriptionStatus.ACTIVE)
                .unitPrice(BigDecimal.valueOf(29.99))
                .discountPercentage(BigDecimal.valueOf(10))
                .build();

        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(sub));
        when(subscriptionRepository.save(any())).thenReturn(sub);
        doNothing().when(kafkaProducer).sendSubscriptionPaused(anyLong(), anyLong());

        SubscriptionResponseDto result = subscriptionService.pauseSubscription(1L);
        assertEquals(SubscriptionStatus.PAUSED, result.getStatus());
    }

    @Test
    void pauseSubscription_WhenNotActive_ShouldThrow() {
        Subscription sub = Subscription.builder()
                .id(1L).status(SubscriptionStatus.PAUSED)
                .unitPrice(BigDecimal.valueOf(29.99))
                .discountPercentage(BigDecimal.valueOf(10))
                .build();

        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(sub));
        assertThrows(SubscriptionNotActiveException.class, () -> subscriptionService.pauseSubscription(1L));
    }

    @Test
    void getSubscription_WhenNotFound_ShouldThrow() {
        when(subscriptionRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(SubscriptionNotFoundException.class, () -> subscriptionService.getSubscription(99L));
    }

    @Test
    void skipDelivery_ShouldAdvanceNextDeliveryDate() {
        LocalDate today = LocalDate.now();
        Subscription sub = Subscription.builder()
                .id(1L).customerId(1L).productId(2L)
                .status(SubscriptionStatus.ACTIVE)
                .frequency(DeliveryFrequency.MONTHLY)
                .nextDeliveryDate(today.plusDays(5))
                .skipCount(0)
                .unitPrice(BigDecimal.valueOf(29.99))
                .discountPercentage(BigDecimal.valueOf(10))
                .build();

        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(sub));
        when(subscriptionRepository.save(any())).thenReturn(sub);

        SubscriptionResponseDto result = subscriptionService.skipDelivery(1L);
        assertEquals(1, result.getSkipCount());
    }
}
