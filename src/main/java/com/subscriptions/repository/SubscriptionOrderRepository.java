package com.subscriptions.repository;

import com.subscriptions.entity.SubscriptionOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SubscriptionOrderRepository extends JpaRepository<SubscriptionOrder, Long> {
    List<SubscriptionOrder> findBySubscriptionId(Long subscriptionId);
    List<SubscriptionOrder> findByCustomerId(String customerId);
}
