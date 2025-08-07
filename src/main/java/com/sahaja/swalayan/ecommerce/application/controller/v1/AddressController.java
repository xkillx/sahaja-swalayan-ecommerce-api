package com.sahaja.swalayan.ecommerce.application.controller.v1;

import com.sahaja.swalayan.ecommerce.application.dto.ApiResponse;
import com.sahaja.swalayan.ecommerce.application.dto.user.AddressDTO;
import com.sahaja.swalayan.ecommerce.application.dto.user.CreateAddressRequestDTO;
import com.sahaja.swalayan.ecommerce.domain.service.AddressService;
import com.sahaja.swalayan.ecommerce.common.CustomUserDetails;
import com.sahaja.swalayan.ecommerce.infrastructure.swagger.ApiCrudResponses;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/addresses")
@RequiredArgsConstructor
public class AddressController {
    private final AddressService addressService;

    @Operation(summary = "Get all addresses for current user", description = "Returns all shipping addresses saved by the authenticated user.")
    @ApiCrudResponses
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<AddressDTO>>> getAddresses(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID userId = userDetails.getId();
        List<AddressDTO> addresses = addressService.getUserAddresses(userId);
        return ResponseEntity.ok(ApiResponse.success("Addresses retrieved successfully", addresses));
    }

    @Operation(summary = "Add a new address", description = "Creates a new shipping address for the authenticated user.")
    @ApiCrudResponses
    @PostMapping("")
    public ResponseEntity<ApiResponse<AddressDTO>> createAddress(@Valid @RequestBody CreateAddressRequestDTO request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID userId = userDetails.getId();
        AddressDTO address = addressService.saveAddress(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Address created successfully", address));
    }

    @Operation(summary = "Update an address", description = "Updates an existing shipping address for the authenticated user.")
    @ApiCrudResponses
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressDTO>> updateAddress(@PathVariable("id") UUID addressId,
            @Valid @RequestBody CreateAddressRequestDTO request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID userId = userDetails.getId();
        AddressDTO address = addressService.updateAddress(userId, addressId, request);
        return ResponseEntity.ok(ApiResponse.success("Address updated successfully", address));
    }

    @Operation(summary = "Delete an address", description = "Deletes a shipping address for the authenticated user.")
    @ApiCrudResponses
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(@PathVariable("id") UUID addressId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID userId = userDetails.getId();
        addressService.deleteAddress(userId, addressId);
        return ResponseEntity.ok(ApiResponse.success("Address deleted successfully"));
    }
}
