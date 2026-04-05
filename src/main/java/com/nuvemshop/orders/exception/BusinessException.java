package com.nuvemshop.orders.exception;

// thrown when a business rule is violated, triggers a 422 response
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
