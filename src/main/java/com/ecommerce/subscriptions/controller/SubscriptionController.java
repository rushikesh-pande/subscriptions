package com.ecommerce.subscriptions.controller;

import com.ecommerce.subscriptions.dto.*;
import com.ecommerce.subscriptions.entity.DeliveryFrequency;
import com.ecommerce.subscriptions.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * POST /api/subscriptions — Create a new subscription
     */
    @PostMapping
    public ResponseEntity<SubscriptionResponseDto> createSubscription(
            @Valid @RequestBody SubscriptionRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subscriptionService.createSubscription(dto));
    }

    /**
     * GET /api/subscriptions/customer/{customerId} — Get all customer subscriptions
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<SubscriptionResponseDto>> getCustomerSubscriptions(
            @PathVariable Long customerId) {
        return ResponseEntity.ok(subscriptionService.getCustomerSubscriptions(customerId));
    }

    /**
     * GET /api/subscriptions/customer/{customerId}/active — Get active subscriptions
     */
    @GetMapping("/customer/{customerId}/active")
    public ResponseEntity<List<SubscriptionResponseDto>> getActiveSubscriptions(
            @PathVariable Long customerId) {
        return ResponseEntity.ok(subscriptionService.getActiveSubscriptions(customerId));
    }

    /**
     * GET /api/subscriptions/{id} — Get subscription by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionResponseDto> getSubscription(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.getSubscription(id));
    }

    /**
     * PUT /api/subscriptions/{id}/pause — Pause a subscription
     */
    @PutMapping("/{id}/pause")
    public ResponseEntity<SubscriptionResponseDto> pauseSubscription(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.pauseSubscription(id));
    }

    /**
     * PUT /api/subscriptions/{id}/resume — Resume a paused subscription
     */
    @PutMapping("/{id}/resume")
    public ResponseEntity<SubscriptionResponseDto> resumeSubscription(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.resumeSubscription(id));
    }

    /**
     * DELETE /api/subscriptions/{id}/cancel — Cancel a subscription
     */
    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<SubscriptionResponseDto> cancelSubscription(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.cancelSubscription(id));
    }

    /**
     * POST /api/subscriptions/{id}/skip — Skip next delivery
     */
    @PostMapping("/{id}/skip")
    public ResponseEntity<SubscriptionResponseDto> skipDelivery(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.skipDelivery(id));
    }

    /**
     * PUT /api/subscriptions/{id}/frequency — Update delivery frequency
     */
    @PutMapping("/{id}/frequency")
    public ResponseEntity<SubscriptionResponseDto> updateFrequency(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFrequencyDto dto) {
        return ResponseEntity.ok(subscriptionService.updateFrequency(id, dto.getFrequency()));
    }

    /**
     * POST /api/subscriptions/{id}/renew — Manually trigger renewal
     */
    @PostMapping("/{id}/renew")
    public ResponseEntity<SubscriptionResponseDto> manualRenew(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.manualRenew(id));
    }
}
