package com.sahaja.swalayan.ecommerce.application.dto;

public class LoginResponse {
    private String token;
    private String tokenType = "Bearer";
    private String errorMessage;

    // Optional user info for convenience on FE
    private String userId;
    private String email;
    private String name;
    private String role;

    public LoginResponse(String token) {
        this.token = token;
    }

    public LoginResponse(String token, String errorMessage) {
        this.token = token;
        this.errorMessage = errorMessage;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
