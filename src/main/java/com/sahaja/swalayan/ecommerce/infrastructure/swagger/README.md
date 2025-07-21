# Custom Swagger Composed Annotations

This package contains custom composed annotations that follow Spring Boot best practices for OpenAPI/Swagger documentation. These annotations reduce code duplication and provide consistent API documentation across the application.

## Benefits of Custom Composed Annotations

1. **Reduced Boilerplate**: Eliminate repetitive Swagger annotation code
2. **Consistency**: Standardized response formats across all endpoints
3. **Maintainability**: Centralized documentation configuration
4. **Reusability**: Common patterns can be reused across controllers
5. **Type Safety**: Compile-time validation of documentation

## Available Annotations

### Response Annotations

#### `@ApiSuccessResponse`
Basic success response (200 OK) annotation.
```java
@ApiSuccessResponse(
    description = "Operation completed successfully",
    example = "{ \"success\": true, \"message\": \"Success\" }"
)
```

#### `@ApiSuccessResponseWithExample`
Success response with customizable example content.
```java
@ApiSuccessResponseWithExample(
    description = "User registered successfully",
    exampleName = "Registration Success",
    example = """
    {
        "success": true,
        "message": "Registration completed successfully",
        "data": { "email": "user@example.com" }
    }
    """
)
```

#### `@ApiBadRequestResponse`
Standard bad request response (400) for validation errors.
```java
@ApiBadRequestResponse // Uses default description and example
```

#### `@ApiConflictResponse`
Conflict response (409) for resource conflicts like duplicate emails.
```java
@ApiConflictResponse // Uses default description and example
```

#### `@ApiServerErrorResponse`
Internal server error response (500).
```java
@ApiServerErrorResponse // Uses default description and example
```

### Composed Response Groups

#### `@ApiStandardResponses`
Combines common responses: 200 Success, 400 Bad Request, 500 Server Error.
```java
@ApiStandardResponses
public ResponseEntity<ApiResponse<Data>> getData() {
    // Method implementation
}
```

#### `@ApiAuthResponses`
Authentication-specific responses: 200 Success, 400 Bad Request, 409 Conflict, 500 Server Error.
```java
@ApiAuthResponses
public ResponseEntity<ApiResponse<RegisterResponse>> register() {
    // Method implementation
}
```

### Operation Annotations

#### `@ApiRegistrationOperation`
Complete operation definition for user registration endpoints.
```java
@PostMapping("/register")
@ApiRegistrationOperation
@ApiSuccessResponseWithExample(
    description = "User registered successfully",
    example = "{ ... }"
)
public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
    // Method implementation
}
```

#### `@ApiConfirmationOperation`
Complete operation definition for email confirmation endpoints.
```java
@GetMapping("/confirm")
@ApiConfirmationOperation
@ApiSuccessResponseWithExample(
    description = "Email confirmed successfully",
    example = "{ ... }"
)
public ResponseEntity<ApiResponse<ConfirmResponse>> confirm(@RequestParam("token") String token) {
    // Method implementation
}
```

## Usage Examples

### Before (Verbose Approach)
```java
@PostMapping("/register")
@Operation(
    summary = "Register a new user",
    description = "Creates a new user account and sends a confirmation email...",
    requestBody = @RequestBody(
        description = "User registration details",
        required = true,
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = RegisterRequest.class),
            examples = @ExampleObject(
                name = "Registration Example",
                value = "{ ... }"
            )
        )
    )
)
@ApiResponses(value = {
    @ApiResponse(
        responseCode = "200",
        description = "User registered successfully",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = ApiResponse.class),
            examples = @ExampleObject(
                name = "Success Response",
                value = "{ ... }"
            )
        )
    ),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid input data or validation errors",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            examples = @ExampleObject(
                name = "Validation Error",
                value = "{ ... }"
            )
        )
    ),
    // ... more responses
})
public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
    // Method implementation
}
```

### After (Composed Annotations)
```java
@PostMapping("/register")
@ApiRegistrationOperation
@ApiSuccessResponseWithExample(
    description = "User registered successfully",
    exampleName = "Registration Success",
    example = """
    {
        "success": true,
        "message": "Registration completed successfully",
        "data": {
            "email": "john.doe@example.com",
            "message": "Registration successful. Please check your email for confirmation.",
            "requiresConfirmation": true
        },
        "timestamp": "2025-01-21T12:56:03"
    }
    """
)
public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
    // Method implementation
}
```

## Creating New Composed Annotations

When creating new endpoints, consider creating specific composed annotations:

1. **Identify Common Patterns**: Look for repeated annotation combinations
2. **Create Specific Annotations**: For domain-specific operations (e.g., `@ApiProductOperation`)
3. **Combine Response Patterns**: Group common response codes together
4. **Use Meaningful Names**: Annotation names should clearly indicate their purpose

### Example: Creating a Product Operation Annotation
```java
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "Create a new product",
    description = "Creates a new product in the catalog with the provided details."
)
@ApiStandardResponses
public @interface ApiCreateProductOperation {
}
```

## Best Practices

1. **Use Composed Annotations**: Always prefer composed annotations over individual Swagger annotations
2. **Consistent Examples**: Use realistic and consistent example data
3. **Meaningful Descriptions**: Provide clear, concise descriptions for operations and responses
4. **Group Related Responses**: Use response group annotations for common patterns
5. **Document Edge Cases**: Include examples for error scenarios
6. **Version Compatibility**: Ensure annotations work with your OpenAPI version

## Integration with Controllers

These annotations integrate seamlessly with Spring Boot controllers and are automatically processed by the OpenAPI documentation generator. The resulting documentation will be available at:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Future Enhancements

Consider adding these annotations as needed:

- `@ApiUnauthorizedResponse` (401)
- `@ApiForbiddenResponse` (403)
- `@ApiNotFoundResponse` (404)
- `@ApiCreateOperation`, `@ApiUpdateOperation`, `@ApiDeleteOperation`
- Domain-specific operation annotations for products, categories, etc.
