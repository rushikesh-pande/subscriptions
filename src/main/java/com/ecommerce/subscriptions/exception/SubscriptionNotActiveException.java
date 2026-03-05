package com.ecommerce.subscriptions.exception;

public class SubscriptionNotActiveException extends RuntimeException {
    public SubscriptionNotActiveException(String message) {
        super(message);
    }
}
