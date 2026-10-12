package ro.msg.learning.shop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.msg.learning.shop.exception.ResourceNotFoundException;
import ro.msg.learning.shop.model.*;
import ro.msg.learning.shop.repository.*;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;

    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Product findById(UUID id) {
        return getProductOrThrow(id);
    }

    @Transactional
    public Product create(Product product, UUID categoryId) {
        ProductCategory category = getCategoryOrThrow(categoryId);
        product.setCategory(category);
        return productRepository.save(product);
    }

    @Transactional
    public Product update(UUID id, Product updatedData, UUID categoryId) {
        Product existing = getProductOrThrow(id);
        ProductCategory category = getCategoryOrThrow(categoryId);

        existing.setName(updatedData.getName());
        existing.setPrice(updatedData.getPrice());
        existing.setWeight(updatedData.getWeight());
        existing.setCategory(category);

        return existing; // flushed by dirty checking at commit
    }

    @Transactional
    public void delete(UUID id) {
        productRepository.delete(getProductOrThrow(id));
    }

    private Product getProductOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private ProductCategory getCategoryOrThrow(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }
}
