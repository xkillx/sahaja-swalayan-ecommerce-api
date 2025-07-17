package com.sahaja.swalayan.ecommerce.domain.model.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;
    @NotBlank(message = "Product name must not be blank")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    String name;

    String description;

    @Valid
    Price price;

    @Valid
    Stock stock;

    @ManyToOne
    @JoinColumn(name = "category_id")
    @NotNull(message = "Product category must not be null")
    Category category;
}

