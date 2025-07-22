package com.sahaja.swalayan.ecommerce.application.dto;

public class LoginResponse {
    private String token;
    private String tokenType = "Bearer";
    private String errorMessage;

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
}
