package com.sahaja.swalayan.ecommerce.domain.model.settings;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "store_settings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreSettings {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "store_name", length = 200)
    private String storeName;

    @Column(name = "address_line", length = 500)
    private String addressLine;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "google_maps_api_key", length = 300)
    private String googleMapsApiKey;
}
