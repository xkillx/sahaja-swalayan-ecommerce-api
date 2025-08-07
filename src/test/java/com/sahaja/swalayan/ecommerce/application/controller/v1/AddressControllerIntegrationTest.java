package com.sahaja.swalayan.ecommerce.application.controller.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahaja.swalayan.ecommerce.application.dto.user.CreateAddressRequestDTO;
import com.sahaja.swalayan.ecommerce.domain.model.user.Address;
import com.sahaja.swalayan.ecommerce.domain.model.user.User;
import com.sahaja.swalayan.ecommerce.domain.model.user.UserRole;
import com.sahaja.swalayan.ecommerce.domain.model.user.UserStatus;
import com.sahaja.swalayan.ecommerce.domain.repository.user.AddressRepository;
import com.sahaja.swalayan.ecommerce.domain.repository.UserRepository;
import com.sahaja.swalayan.ecommerce.common.JwtTokenUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
class AddressControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private String jwtToken;
    private User user;

    @BeforeEach
    void setUp() {
        String password = "password";
        String passwordHash = passwordEncoder.encode(password);
        user = User.builder()
                .name("Test User")
                .email("addressuser@example.com")
                .phone("08123456789")
                .passwordHash(passwordHash)
                .status(UserStatus.ACTIVE)
                .role(UserRole.CUSTOMER)
                .build();
        userRepository.save(user);
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole().name());
        claims.put("email", user.getEmail());
        jwtToken = jwtTokenUtil.generateToken(user.getEmail(), claims);
    }

    @AfterEach
    void cleanUp() {
        addressRepository.findAllByUserId(user.getId()).forEach(addressRepository::delete);
        userRepository.delete(user);
    }

    private MockHttpServletRequestBuilder authenticated(MockHttpServletRequestBuilder builder) {
        return builder.header("Authorization", "Bearer " + jwtToken);
    }

    @Test
    void createAddress_missingFields_error() throws Exception {
        // Missing required fields: label, isDefault
        CreateAddressRequestDTO req = CreateAddressRequestDTO.builder()
                .contactName("Test User")
                .contactPhone("08123456789")
                .addressLine("Jl. Testing No. 1")
                .postalCode("12345")
                .areaId(UUID.randomUUID().toString())
                .latitude(1.23)
                .longitude(4.56)
                .build();
        mockMvc.perform(authenticated(post("/v1/addresses"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void updateAddress_setAsDefault_success() throws Exception {
        // First, create two addresses, one default, one not
        Address addr1 = Address.builder()
                .userId(user.getId())
                .label("Default")
                .contactName("Test User")
                .contactPhone("08123456789")
                .addressLine("Jl. Default No. 1")
                .postalCode("11111")
                .areaId(UUID.randomUUID().toString())
                .latitude(1.0)
                .longitude(2.0)
                .isDefault(true)
                .build();
        Address addr2 = Address.builder()
                .userId(user.getId())
                .label("Other")
                .contactName("Test User")
                .contactPhone("08123456789")
                .addressLine("Jl. Other No. 2")
                .postalCode("22222")
                .areaId(UUID.randomUUID().toString())
                .latitude(3.0)
                .longitude(4.0)
                .isDefault(false)
                .build();
        addressRepository.save(addr1);
        addressRepository.save(addr2);
        // Set addr2 as default
        CreateAddressRequestDTO updateReq = CreateAddressRequestDTO.builder()
                .label("Other")
                .contactName("Test User")
                .contactPhone("08123456789")
                .addressLine("Jl. Other No. 2")
                .postalCode("22222")
                .areaId(addr2.getAreaId())
                .latitude(3.0)
                .longitude(4.0)
                .isDefault(true)
                .build();
        mockMvc.perform(authenticated(put("/v1/addresses/" + addr2.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.is_default").value(true));
        // Reload both from DB to check only one is default
        Address updated1 = addressRepository.findById(addr1.getId()).get();
        Address updated2 = addressRepository.findById(addr2.getId()).get();
        assertThat(updated1.getIsDefault()).as("Previous default must be unset").isFalse();
        assertThat(updated2.getIsDefault()).as("Newly set address must be default").isTrue();
    }

    @Test
    void createMultipleDefaultAddresses_conflictOrUpdatePrevious() throws Exception {
        // Create first address as default
        CreateAddressRequestDTO req1 = CreateAddressRequestDTO.builder()
                .label("A")
                .contactName("Test User")
                .contactPhone("08123456789")
                .addressLine("Jl. A")
                .postalCode("10000")
                .areaId(UUID.randomUUID().toString())
                .latitude(1.0)
                .longitude(2.0)
                .isDefault(true)
                .build();
        mockMvc.perform(authenticated(post("/v1/addresses"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.is_default").value(true));
        // Create second address as default
        CreateAddressRequestDTO req2 = CreateAddressRequestDTO.builder()
                .label("B")
                .contactName("Test User")
                .contactPhone("08123456789")
                .addressLine("Jl. B")
                .postalCode("20000")
                .areaId(UUID.randomUUID().toString())
                .latitude(3.0)
                .longitude(4.0)
                .isDefault(true)
                .build();
        mockMvc.perform(authenticated(post("/v1/addresses"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.is_default").value(true));
        // Ensure only one address is default
        List<Address> addresses = addressRepository.findAllByUserId(user.getId());
        long defaultCount = addresses.stream().filter(Address::getIsDefault).count();
        assertThat(defaultCount).as("Only one address should be default").isEqualTo(1);
    }

    @Test
    void createAddress_success() throws Exception {
        CreateAddressRequestDTO req = CreateAddressRequestDTO.builder()
                .label("Home")
                .contactName("Test User")
                .contactPhone("08123456789")
                .addressLine("Jl. Testing No. 1")
                .postalCode("12345")
                .areaId(UUID.randomUUID().toString())
                .latitude(1.23)
                .longitude(4.56)
                .isDefault(true)
                .build();
        mockMvc.perform(authenticated(post("/v1/addresses"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.label").value("Home"));
    }

    @Test
    void getAddresses_success() throws Exception {
        Address address = Address.builder()
                .userId(user.getId())
                .label("Office")
                .contactName("Test User")
                .contactPhone("08123456789")
                .addressLine("Jl. Kantor No. 2")
                .postalCode("54321")
                .areaId(UUID.randomUUID().toString())
                .latitude(2.34)
                .longitude(5.67)
                .isDefault(false)
                .build();
        addressRepository.save(address);
        mockMvc.perform(authenticated(get("/v1/addresses")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].label").value("Office"));
    }

    @Test
    void updateAddress_success() throws Exception {
        Address address = Address.builder()
                .userId(user.getId())
                .label("Old Label")
                .contactName("Test User")
                .contactPhone("08123456789")
                .addressLine("Jl. Lama No. 3")
                .postalCode("11111")
                .areaId(UUID.randomUUID().toString())
                .latitude(3.45)
                .longitude(6.78)
                .isDefault(false)
                .build();
        addressRepository.save(address);
        CreateAddressRequestDTO updateReq = CreateAddressRequestDTO.builder()
                .label("New Label")
                .contactName("Test User")
                .contactPhone("08123456789")
                .addressLine("Jl. Baru No. 4")
                .postalCode("22222")
                .areaId(address.getAreaId())
                .latitude(7.89)
                .longitude(1.23)
                .isDefault(true)
                .build();
        mockMvc.perform(authenticated(put("/v1/addresses/" + address.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.label").value("New Label"));
    }

    @Test
    void deleteAddress_success() throws Exception {
        Address address = Address.builder()
                .userId(user.getId())
                .label("To Delete")
                .contactName("Test User")
                .contactPhone("08123456789")
                .addressLine("Jl. Hapus No. 5")
                .postalCode("33333")
                .areaId(UUID.randomUUID().toString())
                .latitude(4.56)
                .longitude(7.89)
                .isDefault(false)
                .build();
        addressRepository.save(address);
        mockMvc.perform(authenticated(delete("/v1/addresses/" + address.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateAddress_notFound_error() throws Exception {
        CreateAddressRequestDTO updateReq = CreateAddressRequestDTO.builder()
                .label("Doesn't Matter")
                .contactName("Test User")
                .contactPhone("08123456789")
                .addressLine("Jl. Salah No. 6")
                .postalCode("44444")
                .areaId(UUID.randomUUID().toString())
                .latitude(0.0)
                .longitude(0.0)
                .isDefault(false)
                .build();
        mockMvc.perform(authenticated(put("/v1/addresses/" + UUID.randomUUID()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
