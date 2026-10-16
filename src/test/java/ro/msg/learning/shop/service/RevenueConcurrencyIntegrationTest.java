package ro.msg.learning.shop.service;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import ro.msg.learning.shop.IntegrationTest;
import ro.msg.learning.shop.model.Revenue;
import ro.msg.learning.shop.repository.LocationRepository;
import ro.msg.learning.shop.repository.RevenueRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@IntegrationTest
class RevenueConcurrencyIntegrationTest {

    private static final UUID LOCATION_BUCURESTI = UUID.fromString("e0000000-0000-0000-0000-000000000001");
    private static final UUID USER_ANDREI = UUID.fromString("a0000000-0000-0000-0000-000000000001");
    private static final UUID ADDRESS_ANDREI = UUID.fromString("b0000000-0000-0000-0000-000000000001");
    private static final UUID PRODUCT_ZACUSCA = UUID.fromString("d0000000-0000-0000-0000-000000000001");

    private static final LocalDate REPORT_DATE = LocalDate.of(2024, 5, 20);

    @Autowired
    private RevenueService revenueService;

    @Autowired
    private RevenueRepository revenueRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

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
    void computeAndStore_whenRunSimultaneously_storesOneCorrectRowPerLocation() throws Exception {
        placeSale(REPORT_DATE.atTime(10, 0), 2, "14.50");

        int threads = 4;
        CountDownLatch startSignal = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        try {
            List<Future<Integer>> results = new java.util.ArrayList<>();
            for (int i = 0; i < threads; i++) {
                results.add(executor.submit(() -> {
                    startSignal.await();
                    return revenueService.computeAndStore(REPORT_DATE).size();
                }));
            }
            startSignal.countDown();
            for (Future<Integer> result : results) {
                assertThat(result.get(30, TimeUnit.SECONDS)).isEqualTo((int) locationRepository.count());
            }
        } finally {
            executor.shutdownNow();
        }

        List<Revenue> stored = revenueService.findByDate(REPORT_DATE);
        assertThat(stored).hasSize((int) locationRepository.count());
        assertThat(stored).filteredOn(revenue -> revenue.getLocation().getId().equals(LOCATION_BUCURESTI))
                .singleElement()
                .satisfies(revenue -> assertThat(revenue.getSum()).isEqualByComparingTo("29.00"));
    }

    @Test
    void computeAndStore_whenTransactionFails_leavesNoPartialData() {
        placeSale(REPORT_DATE.atTime(10, 0), 2, "14.50");
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(status -> {
            revenueService.computeAndStore(REPORT_DATE);
            throw new IllegalStateException("boom");
        })).isInstanceOf(IllegalStateException.class);

        assertThat(revenueRepository.count()).isZero();
    }

    @Test
    void computeAndStore_whenFailingAfterAnEarlierSuccess_keepsTheStoredReport() {
        placeSale(REPORT_DATE.atTime(10, 0), 2, "14.50");
        revenueService.computeAndStore(REPORT_DATE);
        long storedRows = revenueRepository.count();

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(status -> {
            revenueService.computeAndStore(REPORT_DATE);
            throw new IllegalStateException("boom");
        })).isInstanceOf(IllegalStateException.class);

        assertThat(revenueRepository.count()).isEqualTo(storedRows);
        assertThat(revenueService.findByDate(REPORT_DATE))
                .filteredOn(revenue -> revenue.getLocation().getId().equals(LOCATION_BUCURESTI))
                .singleElement()
                .satisfies(revenue -> assertThat(revenue.getSum()).isEqualByComparingTo("29.00"));
    }

    private void placeSale(LocalDateTime createdAt, int quantity, String price) {
        UUID orderId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO orders (id, user_id, delivery_address_id, created_at) VALUES (?, ?, ?, ?)",
                orderId, USER_ANDREI, ADDRESS_ANDREI, createdAt);
        jdbcTemplate.update("""
                        INSERT INTO order_details (order_id, product_id, shipped_from_id, quantity, purchased_price)
                        VALUES (?, ?, ?, ?, ?)
                        """,
                orderId, PRODUCT_ZACUSCA, LOCATION_BUCURESTI, quantity, new BigDecimal(price));
    }
}
