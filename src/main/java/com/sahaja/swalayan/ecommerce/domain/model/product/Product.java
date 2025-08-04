package com.sahaja.swalayan.ecommerce.domain.model.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;
import java.math.BigDecimal;

import com.sahaja.swalayan.ecommerce.domain.model.AuditableEntity;

import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;
    
    @Column(name = "name", nullable = false, length = 255)
    String name;

    @Column(name = "description", length = 1024)
    String description;

    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    BigDecimal price;

    @Column(name = "quantity", nullable = false)
    Integer quantity;

    @Column(name = "weight", nullable = false)
    Integer weight;

    @Column(name = "sku", length = 64)
    String sku;

    @Column(name = "height")
    Integer height;

    @Column(name = "length")
    Integer length;

    @Column(name = "width")
    Integer width;

    @ManyToOne
    @JoinColumn(name = "category_id")
    Category category;

    @Column(name = "image_url")
    String imageUrl;
}
