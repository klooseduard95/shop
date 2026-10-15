package ro.msg.learning.shop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ro.msg.learning.shop.exception.ResourceNotFoundException;
import ro.msg.learning.shop.model.Product;
import ro.msg.learning.shop.model.ProductCategory;
import ro.msg.learning.shop.repository.ProductCategoryRepository;
import ro.msg.learning.shop.repository.ProductRepository;
import ro.msg.learning.shop.repository.StockRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductCategoryRepository categoryRepository;

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private ProductService productService;

    private UUID productId;
    private UUID categoryId;
    private ProductCategory category;
    private Product product;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        category = ProductCategory.builder()
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
    }

    @Test
    void findAll_returnsPageOfProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> expectedPage = new PageImpl<>(List.of(product));
        when(productRepository.findAll(pageable)).thenReturn(expectedPage);

        Page<Product> result = productService.findAll(pageable);

        assertThat(result.getContent()).hasSize(1).containsExactly(product);
        verify(productRepository).findAll(pageable);
    }

    @Test
    void findById_whenProductExists_returnsProduct() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        Product result = productService.findById(productId);

        assertThat(result).isEqualTo(product);
        verify(productRepository).findById(productId);
    }

    @Test
    void findById_whenProductDoesNotExist_throwsResourceNotFoundException() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(productId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(productId.toString());
    }

    @Test
    void create_whenCategoryExists_savesAndReturnsProduct() {
        Product toCreate = Product.builder()
                .name("Telemea")
                .price(new BigDecimal("24.90"))
                .weight(0.4)
                .build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(productId);
            return p;
        });

        Product created = productService.create(toCreate, categoryId);

        assertThat(created.getId()).isEqualTo(productId);
        assertThat(created.getCategory()).isEqualTo(category);
        verify(productRepository).save(toCreate);
    }

    @Test
    void create_whenCategoryDoesNotExist_throwsResourceNotFoundException() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(product, categoryId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(categoryId.toString());

        verify(productRepository, never()).save(any());
    }

    @Test
    void update_whenProductAndCategoryExist_updatesFieldsAndReturnsProduct() {
        UUID newCategoryId = UUID.randomUUID();
        ProductCategory newCategory = ProductCategory.builder()
                .id(newCategoryId)
                .name("Mezeluri")
                .build();

        Product updatedData = Product.builder()
                .name("Salam de Sibiu")
                .price(new BigDecimal("46.00"))
                .weight(0.5)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(newCategoryId)).thenReturn(Optional.of(newCategory));

        Product result = productService.update(productId, updatedData, newCategoryId);

        assertThat(result.getName()).isEqualTo("Salam de Sibiu");
        assertThat(result.getPrice()).isEqualByComparingTo("46.00");
        assertThat(result.getWeight()).isEqualTo(0.5);
        assertThat(result.getCategory()).isEqualTo(newCategory);
    }

    @Test
    void update_whenProductDoesNotExist_throwsResourceNotFoundException() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(productId, product, categoryId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(productId.toString());
    }

    @Test
    void delete_whenProductExists_deletesProduct() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        productService.delete(productId);

        InOrder deletionOrder = inOrder(stockRepository, productRepository);
        deletionOrder.verify(stockRepository).deleteAllByProductId(productId);
        deletionOrder.verify(productRepository).delete(product);
    }

    @Test
    void delete_whenProductDoesNotExist_throwsResourceNotFoundException() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete(productId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(productId.toString());

        verify(productRepository, never()).delete(any());
    }
}
