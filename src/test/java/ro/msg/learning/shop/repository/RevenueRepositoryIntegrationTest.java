package ro.msg.learning.shop.repository;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnitUtil;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ro.msg.learning.shop.IntegrationTest;
import ro.msg.learning.shop.model.Revenue;
import ro.msg.learning.shop.service.RevenueService;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class RevenueRepositoryIntegrationTest {

    private static final LocalDate REPORT_DATE = LocalDate.of(2024, 5, 20);

    @Autowired
    private RevenueService revenueService;

    @Autowired
    private RevenueRepository revenueRepository;

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
    void revenueFinder_initialisesLocation_soTheMapperCanReadItsName() {
        revenueService.computeAndStore(REPORT_DATE);

        List<Revenue> revenues = revenueRepository.findByDateOrderByLocation_NameAscLocation_IdAsc(REPORT_DATE);

        assertThat(revenues).isNotEmpty();
        revenues.forEach(revenue -> assertThat(isLoaded(revenue, "location"))
                .as("RevenueMapper reads location.name outside the persistence context")
                .isTrue());
    }

    private boolean isLoaded(Object entity, String attribute) {
        PersistenceUnitUtil util = entityManagerFactory.getPersistenceUnitUtil();
        return util.isLoaded(entity, attribute);
    }
}
