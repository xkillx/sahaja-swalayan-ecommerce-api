package com.sahaja.swalayan.ecommerce.infrastructure.external.shipping.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing an item in a shipment order for Biteship API.
 * <p>
 * Contains details about the package item, its value, quantity, and dimensions.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderItemDTO {
    /**
     * Name of the item (required).
     */
    private String name;

    /**
     * Description of the item (optional).
     * Can be null.
     */
    private String description;

    /**
     * Category of the item (optional).
     * Should match Biteship categories (e.g., "fashion", "electronic").
     * Can be null.
     */
    private String category;

    /**
     * Stock keeping unit (optional).
     * Can be null.
     */
    private String sku;

    /**
     * Value of the item in IDR (required).
     */
    private Integer value;

    /**
     * Number of items (required).
     */
    private Integer quantity;

    /**
     * Weight of the item in grams (required).
     */
    private Integer weight;

    /**
     * Height of the item in centimeters (optional).
     * Can be null.
     */
    private Integer height;

    /**
     * Length of the item in centimeters (optional).
     * Can be null.
     */
    private Integer length;

    /**
     * Width of the item in centimeters (optional).
     * Can be null.
     */
    private Integer width;
}
