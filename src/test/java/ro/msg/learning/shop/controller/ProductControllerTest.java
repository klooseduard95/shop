package ro.msg.learning.shop.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ro.msg.learning.shop.dto.request.ProductRequest;
import ro.msg.learning.shop.exception.GlobalExceptionHandler;
import ro.msg.learning.shop.exception.ResourceNotFoundException;
import ro.msg.learning.shop.mapper.ProductMapper;
import ro.msg.learning.shop.model.Product;
import ro.msg.learning.shop.model.ProductCategory;
import ro.msg.learning.shop.service.ProductService;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import({ProductMapper.class, GlobalExceptionHandler.class})
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ProductService productService;

    private UUID productId;
    private UUID categoryId;
    private Product product;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        ProductCategory category = ProductCategory.builder()
                .id(categoryId)
                .name("Lactate")
                .description("Produse lactate")
                .build();

        product = Product.builder()
                .id(productId)
                .name("Telemea")
                .price(new BigDecimal("24.90"))
                .weight(0.4)
                .category(category)
                .build();

        productRequest = ProductRequest.builder()
                .name("Telemea")
                .price(new BigDecimal("24.90"))
                .weight(0.4)
                .categoryId(categoryId)
                .build();
    }

    @Test
    void getAllProducts_returnsOkWithPage() throws Exception {
        when(productService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(product)));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(productId.toString()))
                .andExpect(jsonPath("$.content[0].name").value("Telemea"))
                .andExpect(jsonPath("$.content[0].price").value(24.90));
    }

    @Test
    void getProductById_whenExists_returnsOk() throws Exception {
        when(productService.findById(productId)).thenReturn(product);

        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Telemea"))
                .andExpect(jsonPath("$.categoryId").value(categoryId.toString()));
    }

    @Test
    void getProductById_whenNotFound_returnsNotFound() throws Exception {
        when(productService.findById(productId))
                .thenThrow(new ResourceNotFoundException("Product not found with id: " + productId));

        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void createProduct_whenValid_returnsCreated() throws Exception {
        when(productService.create(any(Product.class), eq(categoryId))).thenReturn(product);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Telemea"));
    }

    @Test
    void updateProduct_whenValid_returnsOk() throws Exception {
        when(productService.update(eq(productId), any(Product.class), eq(categoryId))).thenReturn(product);

        mockMvc.perform(put("/api/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Telemea"));
    }

    @Test
    void deleteProduct_whenExists_returnsNoContent() throws Exception {
        doNothing().when(productService).delete(productId);

        mockMvc.perform(delete("/api/products/{id}", productId))
                .andExpect(status().isNoContent());

        verify(productService).delete(productId);
    }

    @Test
    void createProduct_whenInvalidRequest_returnsBadRequest() throws Exception {
        ProductRequest invalidRequest = ProductRequest.builder()
                .name("")
                .price(new BigDecimal("-5.00"))
                .build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details.name").exists());
    }

    @Test
    void getProductById_whenUnexpectedError_returnsInternalServerError() throws Exception {
        when(productService.findById(productId)).thenThrow(new RuntimeException("DB connection lost"));

        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("An unexpected error occurred"));
    }

    @Test
    void handleValidation_whenDuplicateFieldErrors_keepsFirstMessage() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        org.springframework.validation.BeanPropertyBindingResult bindingResult =
                new org.springframework.validation.BeanPropertyBindingResult(new Object(), "productRequest");
        bindingResult.addError(new org.springframework.validation.FieldError("productRequest", "name", "First error"));
        bindingResult.addError(new org.springframework.validation.FieldError("productRequest", "name", "Second error"));

        org.springframework.web.bind.MethodArgumentNotValidException ex =
                new org.springframework.web.bind.MethodArgumentNotValidException(null, bindingResult);
        var response = handler.handleValidation(ex);

        org.assertj.core.api.Assertions.assertThat(response.details()).containsEntry("name", "First error");
    }
}