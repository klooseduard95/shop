package ro.msg.learning.shop.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ro.msg.learning.shop.dto.request.ProductRequest;
import ro.msg.learning.shop.dto.response.ProductResponse;
import ro.msg.learning.shop.mapper.ProductMapper;
import ro.msg.learning.shop.model.Product;
import ro.msg.learning.shop.service.ProductService;

import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @GetMapping
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productService.findAll(pageable).map(productMapper::toResponse);
    }

    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable UUID id) {
        return productMapper.toResponse(productService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@Valid @RequestBody ProductRequest request) {
        Product mappedModel = productMapper.toModel(request);
        Product savedProduct = productService.create(mappedModel, request.getCategoryId());

        return productMapper.toResponse(savedProduct);
    }

    @PutMapping("/{id}")
    public ProductResponse updateProduct(@PathVariable UUID id, @Valid @RequestBody ProductRequest request) {
        Product mappedModel = productMapper.toModel(request);
        Product updatedProduct = productService.update(id, mappedModel, request.getCategoryId());

        return productMapper.toResponse(updatedProduct);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public void deleteProduct(@PathVariable UUID id) {
        productService.delete(id);
    }
}
