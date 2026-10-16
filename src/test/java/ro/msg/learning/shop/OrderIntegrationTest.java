package ro.msg.learning.shop;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ro.msg.learning.shop.dto.request.AddressRequest;
import ro.msg.learning.shop.dto.request.OrderCreateRequest;
import ro.msg.learning.shop.dto.request.ProductQuantityRequest;
import ro.msg.learning.shop.model.Stock;
import ro.msg.learning.shop.model.StockId;
import ro.msg.learning.shop.repository.OrderDetailRepository;
import ro.msg.learning.shop.repository.OrderRepository;
import ro.msg.learning.shop.repository.StockRepository;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTest
@AutoConfigureMockMvc(addFilters = false)
class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Flyway flyway;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private StockRepository stockRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final UUID USER_ANDREI = UUID.fromString("a0000000-0000-0000-0000-000000000001");
    private static final UUID PRODUCT_ZACUSCA = UUID.fromString("d0000000-0000-0000-0000-000000000001");
    private static final UUID PRODUCT_MURATURI = UUID.fromString("d0000000-0000-0000-0000-000000000002");
    private static final UUID LOCATION_BUCURESTI = UUID.fromString("e0000000-0000-0000-0000-000000000001");

    @BeforeEach
    void resetDatabaseSchema() {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    void createOrder_whenStockIsSufficient_createsOrderAndDecreasesStock() throws Exception {
        OrderCreateRequest request = createOrderRequest(
                USER_ANDREI,
                productQuantity(PRODUCT_ZACUSCA, 10),
                productQuantity(PRODUCT_MURATURI, 5)
        );

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.userId").value(USER_ANDREI.toString()))
                .andExpect(jsonPath("$.deliveryAddressId").isNotEmpty())
                .andExpect(jsonPath("$.createdAt").isNotEmpty());

        assertThat(orderRepository.count()).isEqualTo(1);
        assertThat(orderDetailRepository.count()).isEqualTo(2);

        Stock updatedZacuscaStock = stockRepository.findById(new StockId(PRODUCT_ZACUSCA, LOCATION_BUCURESTI)).orElseThrow();
        Stock updatedMuraturiStock = stockRepository.findById(new StockId(PRODUCT_MURATURI, LOCATION_BUCURESTI)).orElseThrow();

        assertThat(updatedZacuscaStock.getQuantity()).isEqualTo(110);
        assertThat(updatedMuraturiStock.getQuantity()).isEqualTo(85);
    }

    @Test
    void createOrder_whenStockIsMissing_returnsConflictAndRollsBack() throws Exception {
        OrderCreateRequest request = createOrderRequest(
                USER_ANDREI,
                productQuantity(PRODUCT_ZACUSCA, 9999)
        );

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());

        assertThat(orderRepository.count()).isZero();
        assertThat(orderDetailRepository.count()).isZero();

        Stock unchangedZacuscaStock = stockRepository.findById(new StockId(PRODUCT_ZACUSCA, LOCATION_BUCURESTI)).orElseThrow();
        assertThat(unchangedZacuscaStock.getQuantity()).isEqualTo(120);
    }

    @Test
    void createOrder_whenProductDoesNotExist_returnsNotFound() throws Exception {
        UUID unknownProductId = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
        OrderCreateRequest request = createOrderRequest(
                USER_ANDREI,
                productQuantity(unknownProductId, 1)
        );

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Products not found with ids: [" + unknownProductId + "]"));
    }

    @Test
    void createOrder_whenUserDoesNotExist_returnsNotFound() throws Exception {
        UUID unknownUserId = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
        OrderCreateRequest request = createOrderRequest(
                unknownUserId,
                productQuantity(PRODUCT_ZACUSCA, 1)
        );

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found with id: " + unknownUserId));

        assertThat(orderRepository.count()).isZero();
        assertThat(orderDetailRepository.count()).isZero();
    }

    @Test
    void exportStocks_whenLocationExists_returnsCsv() throws Exception {
        mockMvc.perform(get("/api/stocks/export/{locationId}", LOCATION_BUCURESTI)
                        .accept("text/csv"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(content().string(org.hamcrest.Matchers.startsWith("productId,productName,quantity\n")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        PRODUCT_ZACUSCA + ",\"Zacuscă de vinete, 350 g\",120")));
    }

    @Test
    void exportStocks_whenLocationDoesNotExist_returnsNotFound() throws Exception {
        UUID unknownLocationId = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");

        mockMvc.perform(get("/api/stocks/export/{locationId}", unknownLocationId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Location not found with id: " + unknownLocationId));
    }

    private OrderCreateRequest createOrderRequest(UUID userId, ProductQuantityRequest... products) {
        return OrderCreateRequest.builder()
                .userId(userId)
                .deliveryAddress(AddressRequest.builder()
                        .country("România")
                        .city("București")
                        .county("București")
                        .streetAddress("Strada Lipscani 25")
                        .build())
                .products(List.of(products))
                .build();
    }

    private ProductQuantityRequest productQuantity(UUID productId, int quantity) {
        return ProductQuantityRequest.builder()
                .productId(productId)
                .quantity(quantity)
                .build();
    }
}