package com.sahaja.swalayan.ecommerce.common;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(String message) {
        super(message);
    }
    public CategoryNotFoundException() {
        super("Category does not exist");
    }
}
