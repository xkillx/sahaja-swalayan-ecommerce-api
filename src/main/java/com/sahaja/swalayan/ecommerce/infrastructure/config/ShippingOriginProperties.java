package com.sahaja.swalayan.ecommerce.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "shipping.origin")
public class ShippingOriginProperties {
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String organization;

    private String address;
    private String note;
    private String postalCode;
    private String areaId;
    private String locationId;
    private String collectionMethod; // e.g., pickup or dropoff

    private Double latitude;
    private Double longitude;
}
