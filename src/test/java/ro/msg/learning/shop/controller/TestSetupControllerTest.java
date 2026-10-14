package ro.msg.learning.shop.controller;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ro.msg.learning.shop.config.TestcontainersConfig;
import ro.msg.learning.shop.repository.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfig.class)
class TestSetupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Flyway flyway;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ProductDetailRepository productDetailRepository;

    @BeforeEach
    void resetDatabaseSchema() {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    void populate_rebuildsSchemaAndSeedData() throws Exception {
        mockMvc.perform(post("/api/test/clear"))
                .andExpect(status().isNoContent());
        assertThat(productRepository.count()).isZero();

        mockMvc.perform(post("/api/test/populate"))
                .andExpect(status().isNoContent());

        assertThat(productRepository.count()).isPositive();
        assertThat(stockRepository.count()).isPositive();
        assertThat(userRepository.count()).isPositive();
        assertThat(locationRepository.count()).isPositive();
    }

    @Test
    void clear_removesAllRowsWithoutViolatingForeignKeys() throws Exception {
        assertThat(productRepository.count()).isPositive();

        mockMvc.perform(post("/api/test/clear"))
                .andExpect(status().isNoContent());

        assertThat(stockRepository.count()).isZero();
        assertThat(productDetailRepository.count()).isZero();
        assertThat(productRepository.count()).isZero();
        assertThat(productCategoryRepository.count()).isZero();
        assertThat(locationRepository.count()).isZero();
        assertThat(addressRepository.count()).isZero();
        assertThat(userRepository.count()).isZero();
    }

    @Test
    void clear_isIdempotent() throws Exception {
        mockMvc.perform(post("/api/test/clear"))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/test/clear"))
                .andExpect(status().isNoContent());

        assertThat(userRepository.count()).isZero();
    }
}
