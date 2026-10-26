package ro.msg.learning.shop.service;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ro.msg.learning.shop.IntegrationTest;
import ro.msg.learning.shop.model.Revenue;
import ro.msg.learning.shop.repository.LocationRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class RevenueAggregationIntegrationTest {

    private static final UUID LOCATION_BUCURESTI = UUID.fromString("e0000000-0000-0000-0000-000000000001");
    private static final UUID LOCATION_CLUJ = UUID.fromString("e0000000-0000-0000-0000-000000000002");
    private static final UUID LOCATION_TIMISOARA = UUID.fromString("e0000000-0000-0000-0000-000000000003");
    private static final UUID USER_ANDREI = UUID.fromString("a0000000-0000-0000-0000-000000000001");
    private static final UUID ADDRESS_ANDREI = UUID.fromString("b0000000-0000-0000-0000-000000000001");
    private static final UUID PRODUCT_ZACUSCA = UUID.fromString("d0000000-0000-0000-0000-000000000001");
    private static final UUID PRODUCT_MURATURI = UUID.fromString("d0000000-0000-0000-0000-000000000002");

    private static final LocalDate REPORT_DATE = LocalDate.of(2024, 5, 20);

    @Autowired
    private RevenueService revenueService;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Flyway flyway;

    @BeforeEach
    void resetDatabaseSchema() {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    void computeAndStore_sumsQuantityTimesPurchasedPricePerLocation() {
        placeSale(LOCATION_BUCURESTI, REPORT_DATE.atTime(10, 0), PRODUCT_ZACUSCA, 2, "14.50");
        placeSale(LOCATION_BUCURESTI, REPORT_DATE.atTime(11, 0), PRODUCT_MURATURI, 3, "10.00");
        placeSale(LOCATION_CLUJ, REPORT_DATE.atTime(12, 0), PRODUCT_ZACUSCA, 1, "14.50");

        Map<UUID, BigDecimal> revenues = revenueByLocation(revenueService.computeAndStore(REPORT_DATE));

        assertThat(revenues.get(LOCATION_BUCURESTI)).isEqualByComparingTo("59.00");
        assertThat(revenues.get(LOCATION_CLUJ)).isEqualByComparingTo("14.50");
        assertThat(revenues.get(LOCATION_TIMISOARA)).isEqualByComparingTo("0.00");
    }

    @Test
    void computeAndStore_whenLocationHasHistoricalSalesButNoneOnReportDate_storesZeroRow() {
        placeSale(LOCATION_CLUJ, REPORT_DATE.minusDays(30).atTime(9, 0), PRODUCT_ZACUSCA, 7, "14.50");
        placeSale(LOCATION_BUCURESTI, REPORT_DATE.atTime(9, 0), PRODUCT_ZACUSCA, 1, "14.50");

        List<Revenue> stored = revenueService.computeAndStore(REPORT_DATE);

        assertThat(stored).hasSize((int) locationRepository.count());
        assertThat(revenueByLocation(stored).get(LOCATION_CLUJ)).isEqualByComparingTo("0.00");
    }

    @Test
    void computeAndStore_representsEveryLocationCategoryExactlyOnce() {
        UUID locationWithoutAnySale = insertLocation("Depozit Iași");

        placeSale(LOCATION_CLUJ, REPORT_DATE.minusDays(5).atTime(9, 0), PRODUCT_ZACUSCA, 4, "14.50");
        placeSale(LOCATION_TIMISOARA, REPORT_DATE.atTime(9, 0), PRODUCT_ZACUSCA, 2, "14.50");
        placeSale(LOCATION_BUCURESTI, REPORT_DATE.minusDays(5).atTime(9, 0), PRODUCT_ZACUSCA, 1, "14.50");
        placeSale(LOCATION_BUCURESTI, REPORT_DATE.atTime(9, 0), PRODUCT_MURATURI, 5, "10.00");

        Map<UUID, BigDecimal> revenues = revenueByLocation(revenueService.computeAndStore(REPORT_DATE));

        assertThat(revenues).hasSize(4);
        assertThat(revenues.get(locationWithoutAnySale)).isEqualByComparingTo("0.00");
        assertThat(revenues.get(LOCATION_CLUJ)).isEqualByComparingTo("0.00");
        assertThat(revenues.get(LOCATION_TIMISOARA)).isEqualByComparingTo("29.00");
        assertThat(revenues.get(LOCATION_BUCURESTI)).isEqualByComparingTo("50.00");
    }

    @Test
    void computeAndStore_includesMidnightOfReportDateAndExcludesMidnightOfNextDay() {
        placeSale(LOCATION_BUCURESTI, REPORT_DATE.atStartOfDay(), PRODUCT_ZACUSCA, 1, "10.00");
        placeSale(LOCATION_BUCURESTI, REPORT_DATE.atTime(23, 59, 59), PRODUCT_MURATURI, 1, "5.00");
        placeSale(LOCATION_BUCURESTI, REPORT_DATE.plusDays(1).atStartOfDay(), PRODUCT_ZACUSCA, 1, "99.00");
        placeSale(LOCATION_BUCURESTI, REPORT_DATE.minusDays(1).atTime(23, 59, 59), PRODUCT_ZACUSCA, 1, "77.00");

        Map<UUID, BigDecimal> revenues = revenueByLocation(revenueService.computeAndStore(REPORT_DATE));

        assertThat(revenues.get(LOCATION_BUCURESTI)).isEqualByComparingTo("15.00");
    }

    @Test
    void computeAndStore_excludesSalesWithoutShippingLocation() {
        placeSale(null, REPORT_DATE.atTime(10, 0), PRODUCT_ZACUSCA, 10, "14.50");
        placeSale(LOCATION_BUCURESTI, REPORT_DATE.atTime(10, 0), PRODUCT_MURATURI, 1, "10.00");

        Map<UUID, BigDecimal> revenues = revenueByLocation(revenueService.computeAndStore(REPORT_DATE));

        assertThat(revenues).hasSize((int) locationRepository.count());
        assertThat(revenues.get(LOCATION_BUCURESTI)).isEqualByComparingTo("10.00");
        assertThat(revenues.values()).allSatisfy(sum -> assertThat(sum).isNotEqualByComparingTo("145.00"));
    }

    @Test
    void computeAndStore_whenRepeated_updatesInsteadOfDuplicating() {
        placeSale(LOCATION_BUCURESTI, REPORT_DATE.atTime(10, 0), PRODUCT_ZACUSCA, 2, "14.50");

        revenueService.computeAndStore(REPORT_DATE);
        List<Revenue> afterSecondRun = revenueService.computeAndStore(REPORT_DATE);

        assertThat(afterSecondRun).hasSize((int) locationRepository.count());
        assertThat(revenueByLocation(afterSecondRun).get(LOCATION_BUCURESTI)).isEqualByComparingTo("29.00");

        placeSale(LOCATION_BUCURESTI, REPORT_DATE.atTime(12, 0), PRODUCT_MURATURI, 1, "10.00");
        List<Revenue> afterBackdatedSale = revenueService.computeAndStore(REPORT_DATE);

        assertThat(afterBackdatedSale).hasSize((int) locationRepository.count());
        assertThat(revenueByLocation(afterBackdatedSale).get(LOCATION_BUCURESTI)).isEqualByComparingTo("39.00");
    }

    private Map<UUID, BigDecimal> revenueByLocation(List<Revenue> revenues) {
        return revenues.stream().collect(Collectors.toMap(
                revenue -> revenue.getLocation().getId(), Revenue::getSum, (first, second) -> first));
    }

    private UUID insertLocation(String name) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO locations (id, name, address_id) VALUES (?, ?, ?)",
                id, name, UUID.fromString("b0000000-0000-0000-0000-000000000011"));
        return id;
    }

    private void placeSale(UUID locationId, LocalDateTime createdAt, UUID productId, int quantity, String price) {
        UUID orderId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO orders (id, user_id, delivery_address_id, created_at) VALUES (?, ?, ?, ?)",
                orderId, USER_ANDREI, ADDRESS_ANDREI, createdAt);
        jdbcTemplate.update("""
                        INSERT INTO order_details (order_id, product_id, shipped_from_id, quantity, purchased_price)
                        VALUES (?, ?, ?, ?, ?)
                        """,
                orderId, productId, locationId, quantity, new BigDecimal(price));
    }
}
