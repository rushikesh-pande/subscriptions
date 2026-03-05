package com.subscriptions.repository;

import com.subscriptions.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByCustomerId(String customerId);
    List<Subscription> findByStatus(Subscription.SubscriptionStatus status);
    List<Subscription> findByStatusAndNextDeliveryDateLessThanEqual(
        Subscription.SubscriptionStatus status, LocalDate date);
}
