package com.boxshop.orders.exception;

// thrown when an entity is not found by id, triggers a 404 response
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " not found with id: " + id);
    }
}
