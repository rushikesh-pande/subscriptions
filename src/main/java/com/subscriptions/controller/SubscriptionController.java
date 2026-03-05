package com.subscriptions.controller;

import com.subscriptions.dto.*;
import com.subscriptions.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<SubscriptionResponse> createSubscription(
            @Valid @RequestBody SubscriptionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(subscriptionService.createSubscription(request));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<SubscriptionResponse>> getCustomerSubscriptions(
            @PathVariable String customerId) {
        return ResponseEntity.ok(subscriptionService.getCustomerSubscriptions(customerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> getSubscription(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.getSubscription(id));
    }

    @PutMapping("/{id}/pause")
    public ResponseEntity<SubscriptionResponse> pause(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.pauseSubscription(id));
    }

    @PutMapping("/{id}/resume")
    public ResponseEntity<SubscriptionResponse> resume(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.resumeSubscription(id));
    }

    @PutMapping("/{id}/skip")
    public ResponseEntity<SubscriptionResponse> skip(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.skipNextDelivery(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.cancelSubscription(id));
    }
}
