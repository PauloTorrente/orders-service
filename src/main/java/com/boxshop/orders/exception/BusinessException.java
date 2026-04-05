package com.boxshop.orders.exception;

// 422 Unprocessable Entity
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
