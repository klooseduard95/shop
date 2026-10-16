package ro.msg.learning.shop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.msg.learning.shop.exception.ResourceNotFoundException;
import ro.msg.learning.shop.model.Product;
import ro.msg.learning.shop.model.ProductCategory;
import ro.msg.learning.shop.repository.ProductCategoryRepository;
import ro.msg.learning.shop.repository.ProductRepository;
import ro.msg.learning.shop.repository.StockRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final StockRepository stockRepository;

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
        Product saved = productRepository.save(product);
        log.info("Product created: productId={}, categoryId={}", saved.getId(), categoryId);
        return saved;
    }

    @Transactional
    public Product update(UUID id, Product updatedData, UUID categoryId) {
        Product existing = getProductOrThrow(id);
        ProductCategory category = getCategoryOrThrow(categoryId);

        existing.setName(updatedData.getName());
        existing.setPrice(updatedData.getPrice());
        existing.setWeight(updatedData.getWeight());
        existing.setCategory(category);

        log.info("Product updated: productId={}, categoryId={}", id, categoryId);
        return existing;
    }

    @Transactional
    public void delete(UUID id) {
        Product product = getProductOrThrow(id);
        stockRepository.deleteAllByProductId(id);
        productRepository.delete(product);
        log.info("Product deleted: productId={}", id);
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
