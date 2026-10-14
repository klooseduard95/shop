package ro.msg.learning.shop.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ro.msg.learning.shop.exception.ErrorResponse;
import ro.msg.learning.shop.repository.*;

@Slf4j
@RestController
@RequestMapping("/api/test")
@Profile("test")
@RequiredArgsConstructor
public class TestSetupController {

    private final Flyway flyway;
    private final OrderDetailRepository orderDetailRepository;
    private final OrderRepository orderRepository;
    private final StockRepository stockRepository;
    private final LocationRepository locationRepository;
    private final ProductDetailRepository productDetailRepository;
    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @PostMapping("/populate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void populateDatabase() {
        try {
            flyway.clean();
            flyway.migrate();
        } catch (FlywayException ex) {
            throw new TestSetupException(
                    "Could not rebuild the test database. Start the application with "
                            + "mvn spring-boot:test-run -Dspring-boot.run.profiles=test so that the "
                            + "Testcontainers-backed datasource is used. Cause: " + ex.getMessage(), ex);
        }
    }

    @PostMapping("/clear")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void clearDatabase() {
        orderDetailRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
        stockRepository.deleteAllInBatch();
        locationRepository.deleteAllInBatch();
        productDetailRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        productCategoryRepository.deleteAllInBatch();
        addressRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @ExceptionHandler(TestSetupException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handleTestSetupFailure(TestSetupException ex) {
        log.error("Test set-up endpoint failed", ex);
        return ErrorResponse.of(ex.getMessage());
    }

    static class TestSetupException extends RuntimeException {
        TestSetupException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
