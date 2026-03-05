package com.ecommerce.subscriptions.repository;

import com.ecommerce.subscriptions.entity.Subscription;
import com.ecommerce.subscriptions.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByCustomerId(Long customerId);

    List<Subscription> findByStatus(SubscriptionStatus status);

    List<Subscription> findByStatusAndNextDeliveryDateLessThanEqual(
            SubscriptionStatus status, LocalDate date);

    List<Subscription> findByCustomerIdAndStatus(Long customerId, SubscriptionStatus status);
}
