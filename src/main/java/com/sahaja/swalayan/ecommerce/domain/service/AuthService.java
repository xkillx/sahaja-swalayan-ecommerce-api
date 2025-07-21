package com.sahaja.swalayan.ecommerce.domain.service;

import com.sahaja.swalayan.ecommerce.application.dto.RegisterRequest;

public interface AuthService {
    void registerUser(RegisterRequest request);
    void confirmUser(String token);
}
