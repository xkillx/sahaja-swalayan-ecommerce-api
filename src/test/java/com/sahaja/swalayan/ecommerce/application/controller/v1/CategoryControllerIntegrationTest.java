package com.sahaja.swalayan.ecommerce.application.controller.v1;

import com.sahaja.swalayan.ecommerce.application.dto.CategoryDTO;
import com.sahaja.swalayan.ecommerce.common.JwtTokenUtil;
import com.sahaja.swalayan.ecommerce.domain.model.user.User;
import com.sahaja.swalayan.ecommerce.domain.model.user.UserRole;
import com.sahaja.swalayan.ecommerce.domain.model.user.UserStatus;
import com.sahaja.swalayan.ecommerce.domain.repository.UserRepository;
import com.sahaja.swalayan.ecommerce.domain.repository.CategoryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ADMIN");
        String token = jwtTokenUtil.generateToken(adminUser.getEmail(), claims);
        headers.setBearerAuth(token);
        return headers;
    }

    // Helper to read page metadata for both legacy (top-level) and VIA_DTO (nested under "page") structures
    private JsonNode pageMeta(JsonNode root) {
        return root.has("page") ? root.get("page") : root;
    }

    private Integer tryGetTotalElements(JsonNode root) {
        JsonNode page = root.has("page") ? root.get("page") : null;
        if (page != null && page.has("totalElements")) return page.get("totalElements").asInt();
        if (root.has("totalElements")) return root.get("totalElements").asInt();
        return null;
    }

    private int getPageNumber(JsonNode root) {
        JsonNode page = root.has("page") ? root.get("page") : null;
        if (page != null && page.has("number")) return page.get("number").asInt();
        if (root.has("number")) return root.get("number").asInt();
        JsonNode pageable = root.has("pageable") ? root.get("pageable") : null;
        if (pageable != null && pageable.has("pageNumber")) return pageable.get("pageNumber").asInt();
        throw new AssertionError("Unable to resolve page number from response JSON");
    }

    private int getPageSize(JsonNode root) {
        JsonNode page = root.has("page") ? root.get("page") : null;
        if (page != null && page.has("size")) return page.get("size").asInt();
        if (root.has("size")) return root.get("size").asInt();
        JsonNode pageable = root.has("pageable") ? root.get("pageable") : null;
        if (pageable != null && pageable.has("pageSize")) return pageable.get("pageSize").asInt();
        throw new AssertionError("Unable to resolve page size from response JSON");
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
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "CUSTOMER");
        String token = jwtTokenUtil.generateToken(nonAdminUser.getEmail(), claims);
        headers.setBearerAuth(token);
        return headers;
    }

    @Test
    void testCreateAndGetCategory() {
        String suffix = "-" + System.currentTimeMillis();
        CategoryDTO created = null;
        try {
            // Create category
            CategoryDTO categoryDTO = CategoryDTO.builder()
                    .name("Electronics" + suffix)
                    .description("Electronic items")
                    .build();

            ResponseEntity<CategoryDTO> createResponse = restTemplate.exchange(
                    getBaseUrl(),
                    HttpMethod.POST,
                    new HttpEntity<>(categoryDTO, adminHeaders()),
                    CategoryDTO.class
            );
            assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            created = createResponse.getBody();
            assertThat(created).isNotNull();
            assertThat(created.getId()).isNotNull();
            assertThat(created.getName()).startsWith("Electronics");
            // DB assert
            assertThat(categoryRepository.findById(created.getId())).isPresent();

            // Get category by id
            ResponseEntity<CategoryDTO> getResponse = restTemplate.getForEntity(getBaseUrl() + "/" + created.getId(),
                    CategoryDTO.class);
            assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            CategoryDTO fetched = getResponse.getBody();
            assertThat(fetched).isNotNull();
            assertThat(fetched.getId()).isEqualTo(created.getId());
            assertThat(fetched.getName()).startsWith("Electronics");
        } finally {
            try {
                if (created != null && created.getId() != null) {
                    restTemplate.exchange(getBaseUrl() + "/" + created.getId(), HttpMethod.DELETE,
                            new HttpEntity<>(adminHeaders()), Void.class);
                }
            } catch (Exception ex) { log.warn("cleanup created failed: {}", ex.getMessage()); }
        }
    }

    @Test
    void testGetAllCategories() {
        String suffix = "-" + System.currentTimeMillis();
        CategoryDTO savedCat1 = null;
        CategoryDTO savedCat2 = null;
        try {
            // save 2 categories
            CategoryDTO cat1 = CategoryDTO.builder().name("Cat 1" + suffix).description("Desc 1").build();
            CategoryDTO cat2 = CategoryDTO.builder().name("Cat 2" + suffix).description("Desc 2").build();
            savedCat1 = restTemplate.exchange(
                    getBaseUrl(), HttpMethod.POST, new HttpEntity<>(cat1, adminHeaders()), CategoryDTO.class
            ).getBody();
            savedCat2 = restTemplate.exchange(
                    getBaseUrl(), HttpMethod.POST, new HttpEntity<>(cat2, adminHeaders()), CategoryDTO.class
            ).getBody();

            // get all categories
            ResponseEntity<CategoryDTO[]> response = restTemplate.getForEntity(getBaseUrl(), CategoryDTO[].class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().length).isGreaterThanOrEqualTo(2);
        } finally {
            try {
                if (savedCat1 != null && savedCat1.getId() != null) {
                    restTemplate.exchange(getBaseUrl() + "/" + savedCat1.getId(), HttpMethod.DELETE,
                            new HttpEntity<>(adminHeaders()), Void.class);
                }
            } catch (Exception ex) { log.warn("cleanup savedCat1 failed: {}", ex.getMessage()); }
            try {
                if (savedCat2 != null && savedCat2.getId() != null) {
                    restTemplate.exchange(getBaseUrl() + "/" + savedCat2.getId(), HttpMethod.DELETE,
                            new HttpEntity<>(adminHeaders()), Void.class);
                }
            } catch (Exception ex) { log.warn("cleanup savedCat2 failed: {}", ex.getMessage()); }
        }
    }

    @Test
    void testUpdateCategory() {
        String suffix = "-" + System.currentTimeMillis();
        CategoryDTO savedCat = null;
        try {
            // save 1 category
            CategoryDTO cat = CategoryDTO.builder().name("Cat 1" + suffix).description("Desc 1").build();
            savedCat = restTemplate.exchange(
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
        } finally {
            try {
                if (savedCat != null && savedCat.getId() != null) {
                    restTemplate.exchange(getBaseUrl() + "/" + savedCat.getId(), HttpMethod.DELETE,
                            new HttpEntity<>(adminHeaders()), Void.class);
                }
            } catch (Exception ex) { log.warn("cleanup savedCat failed: {}", ex.getMessage()); }
        }
    }

    @Test
    void testDeleteCategory() {
        String suffix = "-" + System.currentTimeMillis();
        CategoryDTO savedCat = null;
        try {
            // save 1 category
            CategoryDTO cat = CategoryDTO.builder().name("Cat 1" + suffix).description("Desc 1").build();
            savedCat = restTemplate.exchange(
                    getBaseUrl(), HttpMethod.POST, new HttpEntity<>(cat, adminHeaders()), CategoryDTO.class
            ).getBody();

            // delete category
            ResponseEntity<Void> response = restTemplate.exchange(getBaseUrl() + "/" + savedCat.getId(), HttpMethod.DELETE,
                    new HttpEntity<>(adminHeaders()), Void.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        } finally {
            // best-effort: nothing to clean if deletion already succeeded
            try {
                if (savedCat != null && savedCat.getId() != null) {
                    restTemplate.exchange(getBaseUrl() + "/" + savedCat.getId(), HttpMethod.DELETE,
                            new HttpEntity<>(adminHeaders()), Void.class);
                }
            } catch (Exception ex) { log.warn("cleanup savedCat (delete) failed: {}", ex.getMessage()); }
        }
    }

    @Test
    void testCreateDuplicateCategoryName() {
        String unique = "UniqueCategory-" + System.currentTimeMillis();
        CategoryDTO created = null;
        try {
            // Create first category
            CategoryDTO cat1 = CategoryDTO.builder().name(unique).description("desc").build();
            ResponseEntity<CategoryDTO> response1 = restTemplate.exchange(
                    getBaseUrl(), HttpMethod.POST, new HttpEntity<>(cat1, adminHeaders()), CategoryDTO.class
            );
            assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            created = response1.getBody();
            assertThat(created).isNotNull();
            assertThat(created.getName()).isEqualTo(unique);

            // Attempt to create duplicate
            CategoryDTO cat2 = CategoryDTO.builder().name(unique).description("another desc").build();
            ResponseEntity<String> response2 = restTemplate.exchange(
                    getBaseUrl(), HttpMethod.POST, new HttpEntity<>(cat2, adminHeaders()), String.class
            );
            assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response2.getBody()).contains("Category name already exists");
        } finally {
            // Cleanup
            try {
                if (created != null && created.getId() != null) {
                    restTemplate.exchange(getBaseUrl() + "/" + created.getId(), HttpMethod.DELETE,
                            new HttpEntity<>(adminHeaders()), Void.class);
                }
            } catch (Exception ex) { log.warn("cleanup created (duplicate) failed: {}", ex.getMessage()); }
        }
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
        String suffix = "-" + System.currentTimeMillis();
        CategoryDTO created = null;
        try {
            // create a category with admin
            created = restTemplate.exchange(
                    getBaseUrl(), HttpMethod.POST, new HttpEntity<>(CategoryDTO.builder().name("ToUpdate" + suffix).description("d").build(), adminHeaders()), CategoryDTO.class
            ).getBody();
            // attempt update without token
            CategoryDTO updated = CategoryDTO.builder().name("Updated").description("dd").build();
            ResponseEntity<String> response = restTemplate.exchange(
                    getBaseUrl() + "/" + created.getId(), HttpMethod.PUT, new HttpEntity<>(updated), String.class
            );
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        } finally {
            // cleanup
            try {
                if (created != null && created.getId() != null) {
                    restTemplate.exchange(getBaseUrl() + "/" + created.getId(), HttpMethod.DELETE,
                            new HttpEntity<>(adminHeaders()), Void.class);
                }
            } catch (Exception ex) { log.warn("cleanup created (unauth update) failed: {}", ex.getMessage()); }
        }
    }

    @Test
    void testUpdateCategoryForbidden() {
        String suffix = "-" + System.currentTimeMillis();
        CategoryDTO created = null;
        try {
            // create a category with admin
            created = restTemplate.exchange(
                    getBaseUrl(), HttpMethod.POST, new HttpEntity<>(CategoryDTO.builder().name("ToUpdate2" + suffix).description("d").build(), adminHeaders()), CategoryDTO.class
            ).getBody();
            // attempt update with non-admin
            CategoryDTO updated = CategoryDTO.builder().name("Updated2").description("dd").build();
            ResponseEntity<String> response = restTemplate.exchange(
                    getBaseUrl() + "/" + created.getId(), HttpMethod.PUT, new HttpEntity<>(updated, nonAdminHeaders()), String.class
            );
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        } finally {
            // cleanup
            try {
                if (created != null && created.getId() != null) {
                    restTemplate.exchange(getBaseUrl() + "/" + created.getId(), HttpMethod.DELETE,
                            new HttpEntity<>(adminHeaders()), Void.class);
                }
            } catch (Exception ex) { log.warn("cleanup created (forbidden update) failed: {}", ex.getMessage()); }
        }
    }

    @Test
    void testDeleteCategoryUnauthorized() {
        String suffix = "-" + System.currentTimeMillis();
        CategoryDTO created = null;
        try {
            // create a category with admin
            created = restTemplate.exchange(
                    getBaseUrl(), HttpMethod.POST, new HttpEntity<>(CategoryDTO.builder().name("ToDelete" + suffix).description("d").build(), adminHeaders()), CategoryDTO.class
            ).getBody();
            // attempt delete without token
            ResponseEntity<String> response = restTemplate.exchange(
                    getBaseUrl() + "/" + created.getId(), HttpMethod.DELETE, HttpEntity.EMPTY, String.class
            );
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        } finally {
            // cleanup
            try {
                if (created != null && created.getId() != null) {
                    restTemplate.exchange(getBaseUrl() + "/" + created.getId(), HttpMethod.DELETE,
                            new HttpEntity<>(adminHeaders()), Void.class);
                }
            } catch (Exception ex) { log.warn("cleanup created (unauth delete) failed: {}", ex.getMessage()); }
        }
    }

    @Test
    void testDeleteCategoryForbidden() {
        String suffix = "-" + System.currentTimeMillis();
        CategoryDTO created = null;
        try {
            // create a category with admin
            created = restTemplate.exchange(
                    getBaseUrl(), HttpMethod.POST, new HttpEntity<>(CategoryDTO.builder().name("ToDelete2" + suffix).description("d").build(), adminHeaders()), CategoryDTO.class
            ).getBody();
            // attempt delete with non-admin
            ResponseEntity<String> response = restTemplate.exchange(
                    getBaseUrl() + "/" + created.getId(), HttpMethod.DELETE, new HttpEntity<>(nonAdminHeaders()), String.class
            );
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        } finally {
            // cleanup
            try {
                if (created != null && created.getId() != null) {
                    restTemplate.exchange(getBaseUrl() + "/" + created.getId(), HttpMethod.DELETE,
                            new HttpEntity<>(adminHeaders()), Void.class);
                }
            } catch (Exception ex) { log.warn("cleanup created (forbidden delete) failed: {}", ex.getMessage()); }
        }
    }

    @Test
    void testSearchCategoriesByPartialNameAndDescription() throws Exception {
        // Use unique suffix to avoid duplicates across runs
        String suffix = "-" + System.currentTimeMillis();

        CategoryDTO electronics = null;
        CategoryDTO clothing = null;
        CategoryDTO home = null;
        CategoryDTO both = null;
        try {
            // Arrange: create categories
            electronics = restTemplate.exchange(
                    getBaseUrl(), HttpMethod.POST,
                    new HttpEntity<>(CategoryDTO.builder().name("Electronics" + suffix).description("Electronic devices and accessories").build(), adminHeaders()),
                    CategoryDTO.class
            ).getBody();
            clothing = restTemplate.exchange(
                    getBaseUrl(), HttpMethod.POST,
                    new HttpEntity<>(CategoryDTO.builder().name("Clothing" + suffix).description("Apparel and fashion").build(), adminHeaders()),
                    CategoryDTO.class
            ).getBody();
            home = restTemplate.exchange(
                    getBaseUrl(), HttpMethod.POST,
                    new HttpEntity<>(CategoryDTO.builder().name("Home Appliances" + suffix).description("Kitchen electronics and tools").build(), adminHeaders()),
                    CategoryDTO.class
            ).getBody();

            assertThat(electronics).isNotNull();
            assertThat(clothing).isNotNull();
            assertThat(home).isNotNull();
            // DB asserts
            assertThat(categoryRepository.findById(electronics.getId())).isPresent();
            assertThat(categoryRepository.findById(clothing.getId())).isPresent();
            assertThat(categoryRepository.findById(home.getId())).isPresent();

            ObjectMapper mapper = new ObjectMapper();

            // Act 1: name contains 'elec' (case-insensitive) -> should include Electronics*
            String byNameUrl = getBaseUrl() + "/search?name=elec";
            ResponseEntity<String> byNameResp = restTemplate.getForEntity(byNameUrl, String.class);
            assertThat(byNameResp.getStatusCode()).isEqualTo(HttpStatus.OK);
            JsonNode byNameRoot = mapper.readTree(byNameResp.getBody());
            List<String> byName = new ArrayList<>();
            for (JsonNode item : byNameRoot.get("content")) {
                byName.add(item.get("name").asText());
            }
            assertThat(byName).anySatisfy(n -> assertThat(n).startsWith("Electronics"));
            assertThat(byName).noneSatisfy(n -> assertThat(n).startsWith("Clothing"));

            // Act 2: description contains 'electr' -> should include Electronics* and Home Appliances*
            String byDescUrl = getBaseUrl() + "/search?description=electr";
            ResponseEntity<String> byDescResp = restTemplate.getForEntity(byDescUrl, String.class);
            assertThat(byDescResp.getStatusCode()).isEqualTo(HttpStatus.OK);
            JsonNode byDescRoot = mapper.readTree(byDescResp.getBody());
            List<String> byDesc = new ArrayList<>();
            for (JsonNode item : byDescRoot.get("content")) {
                byDesc.add(item.get("name").asText());
            }
            assertThat(byDesc).anySatisfy(n -> assertThat(n).startsWith("Electronics"));
            assertThat(byDesc).anySatisfy(n -> assertThat(n).startsWith("Home Appliances"));
            assertThat(byDesc).noneSatisfy(n -> assertThat(n).startsWith("Clothing"));

            // Act 3: both filters (name contains 'elec' AND description contains 'appl')
            // Create one that matches both
            both = restTemplate.exchange(
                    getBaseUrl(), HttpMethod.POST,
                    new HttpEntity<>(CategoryDTO.builder().name("Electronics Appliances" + suffix).description("Appliances and electronics").build(), adminHeaders()),
                    CategoryDTO.class
            ).getBody();
            assertThat(both).isNotNull();
            assertThat(categoryRepository.findById(both.getId())).isPresent();

            String bothUrl = getBaseUrl() + "/search?name=elec&description=appl";
            ResponseEntity<String> bothResp = restTemplate.getForEntity(bothUrl, String.class);
            assertThat(bothResp.getStatusCode()).isEqualTo(HttpStatus.OK);
            JsonNode bothRoot = mapper.readTree(bothResp.getBody());
            List<String> bothNames = new ArrayList<>();
            for (JsonNode item : bothRoot.get("content")) {
                bothNames.add(item.get("name").asText());
            }
            assertThat(bothNames).anySatisfy(n -> assertThat(n).startsWith("Electronics Appliances"));
        } finally {
            try {
                if (electronics != null && electronics.getId() != null) {
                    restTemplate.exchange(getBaseUrl() + "/" + electronics.getId(), HttpMethod.DELETE, new HttpEntity<>(adminHeaders()), Void.class);
                }
            } catch (Exception ex) { log.warn("cleanup electronics failed: {}", ex.getMessage()); }
            try {
                if (clothing != null && clothing.getId() != null) {
                    restTemplate.exchange(getBaseUrl() + "/" + clothing.getId(), HttpMethod.DELETE, new HttpEntity<>(adminHeaders()), Void.class);
                }
            } catch (Exception ex) { log.warn("cleanup clothing failed: {}", ex.getMessage()); }
            try {
                if (home != null && home.getId() != null) {
                    restTemplate.exchange(getBaseUrl() + "/" + home.getId(), HttpMethod.DELETE, new HttpEntity<>(adminHeaders()), Void.class);
                }
            } catch (Exception ex) { log.warn("cleanup home failed: {}", ex.getMessage()); }
            try {
                if (both != null && both.getId() != null) {
                    restTemplate.exchange(getBaseUrl() + "/" + both.getId(), HttpMethod.DELETE, new HttpEntity<>(adminHeaders()), Void.class);
                }
            } catch (Exception ex) { log.warn("cleanup both failed: {}", ex.getMessage()); }
        }
    }

    @Test
    void testSearchCategoriesPaginationAndSorting() throws Exception {
        String suffix = "-" + System.currentTimeMillis();
        CategoryDTO alpha = null;
        CategoryDTO bravo = null;
        CategoryDTO charlie = null;
        try {
            // Arrange: create three categories with distinct names
            alpha = restTemplate.exchange(
                    getBaseUrl(), HttpMethod.POST,
                    new HttpEntity<>(CategoryDTO.builder().name("Alpha" + suffix).description("A").build(), adminHeaders()),
                    CategoryDTO.class
            ).getBody();
            charlie = restTemplate.exchange(
                    getBaseUrl(), HttpMethod.POST,
                    new HttpEntity<>(CategoryDTO.builder().name("Charlie" + suffix).description("C").build(), adminHeaders()),
                    CategoryDTO.class
            ).getBody();
            bravo = restTemplate.exchange(
                    getBaseUrl(), HttpMethod.POST,
                    new HttpEntity<>(CategoryDTO.builder().name("Bravo" + suffix).description("B").build(), adminHeaders()),
                    CategoryDTO.class
            ).getBody();

            assertThat(alpha).isNotNull();
            assertThat(bravo).isNotNull();
            assertThat(charlie).isNotNull();
            // DB asserts
            assertThat(categoryRepository.findById(alpha.getId())).isPresent();
            assertThat(categoryRepository.findById(bravo.getId())).isPresent();
            assertThat(categoryRepository.findById(charlie.getId())).isPresent();

            ObjectMapper mapper = new ObjectMapper();

            // Page 0, size 2, default sort name asc => [Alpha*, Bravo*]
            String page0Url = getBaseUrl() + "/search?page=0&size=2";
            ResponseEntity<String> page0Resp = restTemplate.getForEntity(page0Url, String.class);
            assertThat(page0Resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            JsonNode root0 = mapper.readTree(page0Resp.getBody());
            JsonNode content0 = root0.get("content");
            assertThat(content0).isNotNull();
            assertThat(content0.size()).isEqualTo(2);
            assertThat(content0.get(0).get("name").asText()).startsWith("Alpha");
            assertThat(content0.get(1).get("name").asText()).startsWith("Bravo");
            Integer total0 = tryGetTotalElements(root0);
            if (total0 != null) {
                assertThat(total0).isGreaterThanOrEqualTo(3);
            }
            assertThat(getPageNumber(root0)).isEqualTo(0);
            assertThat(getPageSize(root0)).isEqualTo(2);

            // Page 1, size 2 => remaining [Charlie*]
            String page1Url = getBaseUrl() + "/search?page=1&size=2";
            ResponseEntity<String> page1Resp = restTemplate.getForEntity(page1Url, String.class);
            assertThat(page1Resp.getStatusCode()).isEqualTo(HttpStatus.OK);
            JsonNode root1 = mapper.readTree(page1Resp.getBody());
            JsonNode content1 = root1.get("content");
            assertThat(content1).isNotNull();
            assertThat(content1.size()).isGreaterThanOrEqualTo(1);
            assertThat(content1.get(0).get("name").asText()).startsWith("Charlie");
        } finally {
            try {
                if (alpha != null && alpha.getId() != null) {
                    restTemplate.exchange(getBaseUrl() + "/" + alpha.getId(), HttpMethod.DELETE, new HttpEntity<>(adminHeaders()), Void.class);
                }
            } catch (Exception ex) { log.warn("cleanup alpha failed: {}", ex.getMessage()); }
            try {
                if (bravo != null && bravo.getId() != null) {
                    restTemplate.exchange(getBaseUrl() + "/" + bravo.getId(), HttpMethod.DELETE, new HttpEntity<>(adminHeaders()), Void.class);
                }
            } catch (Exception ex) { log.warn("cleanup bravo failed: {}", ex.getMessage()); }
            try {
                if (charlie != null && charlie.getId() != null) {
                    restTemplate.exchange(getBaseUrl() + "/" + charlie.getId(), HttpMethod.DELETE, new HttpEntity<>(adminHeaders()), Void.class);
                }
            } catch (Exception ex) { log.warn("cleanup charlie failed: {}", ex.getMessage()); }
        }
    }
}
