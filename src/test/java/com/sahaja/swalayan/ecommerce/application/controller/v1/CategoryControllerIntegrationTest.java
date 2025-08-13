package com.sahaja.swalayan.ecommerce.application.controller.v1;

import com.sahaja.swalayan.ecommerce.application.dto.CategoryDTO;
import com.sahaja.swalayan.ecommerce.common.JwtTokenUtil;
import com.sahaja.swalayan.ecommerce.domain.model.user.User;
import com.sahaja.swalayan.ecommerce.domain.model.user.UserRole;
import com.sahaja.swalayan.ecommerce.domain.model.user.UserStatus;
import com.sahaja.swalayan.ecommerce.domain.repository.UserRepository;
import com.sahaja.swalayan.ecommerce.domain.repository.CategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class CategoryControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User adminUser;
    private User nonAdminUser;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/v1/categories";
    }

    @BeforeEach
    void setUpAdminUser() {
        adminUser = User.builder()
                .name("Admin IT User")
                .email("admin.category.it@example.com")
                .passwordHash("dummy")
                .phone("+620000000001")
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(adminUser);
    }

    @AfterEach
    void cleanUpUsers() {
        try {
            if (nonAdminUser != null && nonAdminUser.getId() != null) {
                userRepository.delete(nonAdminUser);
                nonAdminUser = null;
            }
            if (adminUser != null && adminUser.getId() != null) {
                userRepository.delete(adminUser);
                adminUser = null;
            }
        } catch (Exception e) {
            log.warn("[cleanUpUsers] Failed to delete users: {}", e.getMessage());
        }
    }

    private HttpHeaders adminHeaders() {
        HttpHeaders headers = new HttpHeaders();
        java.util.Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("role", "ADMIN");
        String token = jwtTokenUtil.generateToken(adminUser.getEmail(), claims);
        headers.setBearerAuth(token);
        return headers;
    }

    private HttpHeaders nonAdminHeaders() {
        if (nonAdminUser == null) {
            nonAdminUser = User.builder()
                    .name("Customer IT User")
                    .email("customer.category.it@example.com")
                    .passwordHash("dummy")
                    .phone("+620000000002")
                    .role(UserRole.CUSTOMER)
                    .status(UserStatus.ACTIVE)
                    .build();
            userRepository.save(nonAdminUser);
        }
        HttpHeaders headers = new HttpHeaders();
        java.util.Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("role", "CUSTOMER");
        String token = jwtTokenUtil.generateToken(nonAdminUser.getEmail(), claims);
        headers.setBearerAuth(token);
        return headers;
    }

    @Test
    void testCreateAndGetCategory() {
        // Create category
        CategoryDTO categoryDTO = CategoryDTO.builder()
                .name("Electronics")
                .description("Electronic items")
                .build();

        ResponseEntity<CategoryDTO> createResponse = restTemplate.exchange(
                getBaseUrl(),
                HttpMethod.POST,
                new HttpEntity<>(categoryDTO, adminHeaders()),
                CategoryDTO.class
        );
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        CategoryDTO created = createResponse.getBody();
        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Electronics");
        // DB assert
        assertThat(categoryRepository.findById(created.getId())).isPresent();

        // Get category by id
        ResponseEntity<CategoryDTO> getResponse = restTemplate.getForEntity(getBaseUrl() + "/" + created.getId(),
                CategoryDTO.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        CategoryDTO fetched = getResponse.getBody();
        assertThat(fetched).isNotNull();
        assertThat(fetched.getId()).isEqualTo(created.getId());
        assertThat(fetched.getName()).isEqualTo("Electronics");

        // delete category after test (admin)
        restTemplate.exchange(getBaseUrl() + "/" + created.getId(), HttpMethod.DELETE,
                new HttpEntity<>(adminHeaders()), Void.class);
    }

    @Test
    void testGetAllCategories() {
        // save 2 categories
        CategoryDTO cat1 = CategoryDTO.builder().name("Cat 1").description("Desc 1").build();
        CategoryDTO cat2 = CategoryDTO.builder().name("Cat 2").description("Desc 2").build();
        CategoryDTO savedCat1 = restTemplate.exchange(
                getBaseUrl(), HttpMethod.POST, new HttpEntity<>(cat1, adminHeaders()), CategoryDTO.class
        ).getBody();
        CategoryDTO savedCat2 = restTemplate.exchange(
                getBaseUrl(), HttpMethod.POST, new HttpEntity<>(cat2, adminHeaders()), CategoryDTO.class
        ).getBody();

        // get all categories
        ResponseEntity<CategoryDTO[]> response = restTemplate.getForEntity(getBaseUrl(), CategoryDTO[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThanOrEqualTo(2);

        // delete categories after test
        restTemplate.exchange(getBaseUrl() + "/" + savedCat1.getId(), HttpMethod.DELETE,
                new HttpEntity<>(adminHeaders()), Void.class);
        restTemplate.exchange(getBaseUrl() + "/" + savedCat2.getId(), HttpMethod.DELETE,
                new HttpEntity<>(adminHeaders()), Void.class);
    }

    @Test
    void testUpdateCategory() {
        // save 1 category
        CategoryDTO cat = CategoryDTO.builder().name("Cat 1").description("Desc 1").build();
        CategoryDTO savedCat = restTemplate.exchange(
                getBaseUrl(), HttpMethod.POST, new HttpEntity<>(cat, adminHeaders()), CategoryDTO.class
        ).getBody();

        // update category
        CategoryDTO updatedCat = CategoryDTO.builder().name("Updated Cat 1").description("Updated Desc 1").build();
        ResponseEntity<CategoryDTO> response = restTemplate.exchange(getBaseUrl() + "/" + savedCat.getId(),
                HttpMethod.PUT, new HttpEntity<>(updatedCat, adminHeaders()), CategoryDTO.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Updated Cat 1");
        assertThat(response.getBody().getDescription()).isEqualTo("Updated Desc 1");
        assertThat(categoryRepository.findById(savedCat.getId())).isPresent();

        // delete category after test
        restTemplate.exchange(getBaseUrl() + "/" + savedCat.getId(), HttpMethod.DELETE,
                new HttpEntity<>(adminHeaders()), Void.class);
    }

    @Test
    void testDeleteCategory() {
        // save 1 category
        CategoryDTO cat = CategoryDTO.builder().name("Cat 1").description("Desc 1").build();
        CategoryDTO savedCat = restTemplate.exchange(
                getBaseUrl(), HttpMethod.POST, new HttpEntity<>(cat, adminHeaders()), CategoryDTO.class
        ).getBody();

        // delete category
        ResponseEntity<Void> response = restTemplate.exchange(getBaseUrl() + "/" + savedCat.getId(), HttpMethod.DELETE,
                new HttpEntity<>(adminHeaders()), Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void testCreateDuplicateCategoryName() {
        // Create first category
        CategoryDTO cat1 = CategoryDTO.builder().name("UniqueCategory").description("desc").build();
        ResponseEntity<CategoryDTO> response1 = restTemplate.exchange(
                getBaseUrl(), HttpMethod.POST, new HttpEntity<>(cat1, adminHeaders()), CategoryDTO.class
        );
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        CategoryDTO created = response1.getBody();
        assertThat(created).isNotNull();
        assertThat(created.getName()).isEqualTo("UniqueCategory");

        // Attempt to create duplicate
        CategoryDTO cat2 = CategoryDTO.builder().name("UniqueCategory").description("another desc").build();
        ResponseEntity<String> response2 = restTemplate.exchange(
                getBaseUrl(), HttpMethod.POST, new HttpEntity<>(cat2, adminHeaders()), String.class
        );
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response2.getBody()).contains("Category name already exists");

        // Cleanup
        restTemplate.exchange(getBaseUrl() + "/" + created.getId(), HttpMethod.DELETE,
                new HttpEntity<>(adminHeaders()), Void.class);
    }

    @Test
    void testCreateCategoryValidationErrors() {
        // Create an invalid CategoryDTO (blank name)
        CategoryDTO invalidCategory = CategoryDTO.builder().name("").description("desc").build();
        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl(), HttpMethod.POST, new HttpEntity<>(invalidCategory, adminHeaders()), String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Category name must not be blank");

    }

    @Test
    void testCreateCategoryUnauthorized() {
        CategoryDTO categoryDTO = CategoryDTO.builder().name("Unauthorized").description("desc").build();
        ResponseEntity<String> response = restTemplate.postForEntity(getBaseUrl(), categoryDTO, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void testCreateCategoryForbidden() {
        CategoryDTO categoryDTO = CategoryDTO.builder().name("Forbidden").description("desc").build();
        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl(), HttpMethod.POST, new HttpEntity<>(categoryDTO, nonAdminHeaders()), String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void testUpdateCategoryUnauthorized() {
        // create a category with admin
        CategoryDTO created = restTemplate.exchange(
                getBaseUrl(), HttpMethod.POST, new HttpEntity<>(CategoryDTO.builder().name("ToUpdate").description("d").build(), adminHeaders()), CategoryDTO.class
        ).getBody();
        // attempt update without token
        CategoryDTO updated = CategoryDTO.builder().name("Updated").description("dd").build();
        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl() + "/" + created.getId(), HttpMethod.PUT, new HttpEntity<>(updated), String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        // cleanup
        restTemplate.exchange(getBaseUrl() + "/" + created.getId(), HttpMethod.DELETE,
                new HttpEntity<>(adminHeaders()), Void.class);
    }

    @Test
    void testUpdateCategoryForbidden() {
        // create a category with admin
        CategoryDTO created = restTemplate.exchange(
                getBaseUrl(), HttpMethod.POST, new HttpEntity<>(CategoryDTO.builder().name("ToUpdate2").description("d").build(), adminHeaders()), CategoryDTO.class
        ).getBody();
        // attempt update with non-admin
        CategoryDTO updated = CategoryDTO.builder().name("Updated2").description("dd").build();
        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl() + "/" + created.getId(), HttpMethod.PUT, new HttpEntity<>(updated, nonAdminHeaders()), String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        // cleanup
        restTemplate.exchange(getBaseUrl() + "/" + created.getId(), HttpMethod.DELETE,
                new HttpEntity<>(adminHeaders()), Void.class);
    }

    @Test
    void testDeleteCategoryUnauthorized() {
        // create a category with admin
        CategoryDTO created = restTemplate.exchange(
                getBaseUrl(), HttpMethod.POST, new HttpEntity<>(CategoryDTO.builder().name("ToDelete").description("d").build(), adminHeaders()), CategoryDTO.class
        ).getBody();
        // attempt delete without token
        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl() + "/" + created.getId(), HttpMethod.DELETE, HttpEntity.EMPTY, String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        // cleanup
        restTemplate.exchange(getBaseUrl() + "/" + created.getId(), HttpMethod.DELETE,
                new HttpEntity<>(adminHeaders()), Void.class);
    }

    @Test
    void testDeleteCategoryForbidden() {
        // create a category with admin
        CategoryDTO created = restTemplate.exchange(
                getBaseUrl(), HttpMethod.POST, new HttpEntity<>(CategoryDTO.builder().name("ToDelete2").description("d").build(), adminHeaders()), CategoryDTO.class
        ).getBody();
        // attempt delete with non-admin
        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl() + "/" + created.getId(), HttpMethod.DELETE, new HttpEntity<>(nonAdminHeaders()), String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        // cleanup
        restTemplate.exchange(getBaseUrl() + "/" + created.getId(), HttpMethod.DELETE,
                new HttpEntity<>(adminHeaders()), Void.class);
    }
}
