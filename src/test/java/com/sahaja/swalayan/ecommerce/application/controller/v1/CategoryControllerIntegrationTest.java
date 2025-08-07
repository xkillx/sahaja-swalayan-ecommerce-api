package com.sahaja.swalayan.ecommerce.application.controller.v1;

import com.sahaja.swalayan.ecommerce.application.dto.CategoryDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CategoryControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/v1/categories";
    }

    @Test
    void testCreateAndGetCategory() {
        // Create category
        CategoryDTO categoryDTO = CategoryDTO.builder()
                .name("Electronics")
                .description("Electronic items")
                .build();

        ResponseEntity<CategoryDTO> createResponse = restTemplate.postForEntity(getBaseUrl(), categoryDTO,
                CategoryDTO.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        CategoryDTO created = createResponse.getBody();
        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Electronics");

        // Get category by id
        ResponseEntity<CategoryDTO> getResponse = restTemplate.getForEntity(getBaseUrl() + "/" + created.getId(),
                CategoryDTO.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        CategoryDTO fetched = getResponse.getBody();
        assertThat(fetched).isNotNull();
        assertThat(fetched.getId()).isEqualTo(created.getId());
        assertThat(fetched.getName()).isEqualTo("Electronics");

        // delete category after test
        restTemplate.delete(getBaseUrl() + "/" + created.getId());
    }

    @Test
    void testGetAllCategories() {
        // save 2 categories
        CategoryDTO cat1 = CategoryDTO.builder().name("Cat 1").description("Desc 1").build();
        CategoryDTO cat2 = CategoryDTO.builder().name("Cat 2").description("Desc 2").build();
        CategoryDTO savedCat1 = restTemplate.postForEntity(getBaseUrl(), cat1, CategoryDTO.class).getBody();
        CategoryDTO savedCat2 = restTemplate.postForEntity(getBaseUrl(), cat2, CategoryDTO.class).getBody();

        // get all categories
        ResponseEntity<CategoryDTO[]> response = restTemplate.getForEntity(getBaseUrl(), CategoryDTO[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThanOrEqualTo(2);

        // delete categories after test
        restTemplate.delete(getBaseUrl() + "/" + savedCat1.getId());
        restTemplate.delete(getBaseUrl() + "/" + savedCat2.getId());
    }

    @Test
    void testUpdateCategory() {
        // save 1 category
        CategoryDTO cat = CategoryDTO.builder().name("Cat 1").description("Desc 1").build();
        CategoryDTO savedCat = restTemplate.postForEntity(getBaseUrl(), cat, CategoryDTO.class).getBody();

        // update category
        CategoryDTO updatedCat = CategoryDTO.builder().name("Updated Cat 1").description("Updated Desc 1").build();
        ResponseEntity<CategoryDTO> response = restTemplate.exchange(getBaseUrl() + "/" + savedCat.getId(),
                HttpMethod.PUT, new HttpEntity<>(updatedCat), CategoryDTO.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Updated Cat 1");
        assertThat(response.getBody().getDescription()).isEqualTo("Updated Desc 1");

        // delete category after test
        restTemplate.delete(getBaseUrl() + "/" + savedCat.getId());
    }

    @Test
    void testDeleteCategory() {
        // save 1 category
        CategoryDTO cat = CategoryDTO.builder().name("Cat 1").description("Desc 1").build();
        CategoryDTO savedCat = restTemplate.postForEntity(getBaseUrl(), cat, CategoryDTO.class).getBody();

        // delete category
        ResponseEntity<Void> response = restTemplate.exchange(getBaseUrl() + "/" + savedCat.getId(), HttpMethod.DELETE,
                null, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void testCreateDuplicateCategoryName() {
        // Create first category
        CategoryDTO cat1 = CategoryDTO.builder().name("UniqueCategory").description("desc").build();
        ResponseEntity<CategoryDTO> response1 = restTemplate.postForEntity(getBaseUrl(), cat1, CategoryDTO.class);
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        CategoryDTO created = response1.getBody();
        assertThat(created).isNotNull();
        assertThat(created.getName()).isEqualTo("UniqueCategory");

        // Attempt to create duplicate
        CategoryDTO cat2 = CategoryDTO.builder().name("UniqueCategory").description("another desc").build();
        ResponseEntity<String> response2 = restTemplate.postForEntity(getBaseUrl(), cat2, String.class);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response2.getBody()).contains("Category name already exists");

        // Cleanup
        restTemplate.delete(getBaseUrl() + "/" + created.getId());
    }

    @Test
    void testCreateCategoryValidationErrors() {
        // Create an invalid CategoryDTO (blank name)
        CategoryDTO invalidCategory = CategoryDTO.builder().name("").description("desc").build();
        ResponseEntity<String> response = restTemplate.postForEntity(getBaseUrl(), invalidCategory, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Category name must not be blank");
    }
}
