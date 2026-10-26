package ro.msg.learning.shop.repository;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnitUtil;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ro.msg.learning.shop.IntegrationTest;
import ro.msg.learning.shop.model.Product;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class ProductRepositoryIntegrationTest {

    private static final UUID PRODUCT_ZACUSCA =
            UUID.fromString("d0000000-0000-0000-0000-000000000001");

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private Flyway flyway;

    @BeforeEach
    void resetDatabaseSchema() {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    void findById_initialisesCategorySoTheMapperCanReadItsName() {
        Product product = productRepository.findById(PRODUCT_ZACUSCA).orElseThrow();

        PersistenceUnitUtil util = entityManagerFactory.getPersistenceUnitUtil();
        assertThat(util.isLoaded(product, "category"))
                .as("ProductMapper reads category.name outside the persistence context")
                .isTrue();
    }
}
