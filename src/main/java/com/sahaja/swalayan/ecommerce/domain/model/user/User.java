package com.sahaja.swalayan.ecommerce.domain.model.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

import com.sahaja.swalayan.ecommerce.domain.model.AuditableEntity;

@Entity
@Table(name = "users", uniqueConstraints = { @UniqueConstraint(columnNames = "email") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends AuditableEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number")
    @Column(length = 20)
    private String phone;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;
}
