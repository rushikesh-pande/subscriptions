package com.subscriptions.controller;
import com.subscriptions.dto.*;
import com.subscriptions.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/subscriptions") @RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService service;
    @PostMapping
    public ResponseEntity<SubscriptionResponse> create(@Valid @RequestBody SubscriptionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }
    @GetMapping("/customer/{cid}")
    public ResponseEntity<List<SubscriptionResponse>> getByCustomer(@PathVariable String cid) {
        return ResponseEntity.ok(service.getByCustomer(cid));
    }
    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> get(@PathVariable Long id) { return ResponseEntity.ok(service.get(id)); }
    @PutMapping("/{id}/pause")
    public ResponseEntity<SubscriptionResponse> pause(@PathVariable Long id) { return ResponseEntity.ok(service.pause(id)); }
    @PutMapping("/{id}/resume")
    public ResponseEntity<SubscriptionResponse> resume(@PathVariable Long id) { return ResponseEntity.ok(service.resume(id)); }
    @PutMapping("/{id}/skip")
    public ResponseEntity<SubscriptionResponse> skip(@PathVariable Long id) { return ResponseEntity.ok(service.skipDelivery(id)); }
    @DeleteMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> cancel(@PathVariable Long id) { return ResponseEntity.ok(service.cancel(id)); }
}
