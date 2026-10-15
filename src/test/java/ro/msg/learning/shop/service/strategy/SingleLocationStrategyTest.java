package ro.msg.learning.shop.service.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.msg.learning.shop.exception.NoSuitableLocationException;
import ro.msg.learning.shop.model.Location;
import ro.msg.learning.shop.model.Product;
import ro.msg.learning.shop.model.Stock;
import ro.msg.learning.shop.repository.StockRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SingleLocationStrategyTest {

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private SingleLocationStrategy strategy;

    private UUID product1Id;
    private UUID product2Id;
    private Product product1;
    private Product product2;
    private Location location1;
    private Location location2;

    @BeforeEach
    void setUp() {
        product1Id = UUID.fromString("d0000000-0000-0000-0000-000000000001");
        product2Id = UUID.fromString("d0000000-0000-0000-0000-000000000002");

        product1 = Product.builder().id(product1Id).name("Zacuscă").build();
        product2 = Product.builder().id(product2Id).name("Murături").build();

        location1 = Location.builder()
                .id(UUID.fromString("e0000000-0000-0000-0000-000000000001"))
                .name("Depozit București")
                .build();
        location2 = Location.builder()
                .id(UUID.fromString("e0000000-0000-0000-0000-000000000002"))
                .name("Depozit Cluj-Napoca")
                .build();
    }

    @Test
    void findShipmentItems_whenEmptyRequest_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> strategy.findShipmentItems(Collections.emptyMap()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Requested products must not be empty");
    }

    @Test
    void findShipmentItems_whenLocationHasAllProducts_returnsShipmentFromThatLocation() {
        Map<UUID, Integer> requested = Map.of(product1Id, 5, product2Id, 10);

        Stock loc1Stock1 = Stock.builder().location(location1).product(product1).quantity(10).build();
        Stock loc2Stock1 = Stock.builder().location(location2).product(product1).quantity(20).build();
        Stock loc2Stock2 = Stock.builder().location(location2).product(product2).quantity(15).build();

        when(stockRepository.findByProductIdInAndQuantityGreaterThanEqualOrderByLocation_IdAsc(anyCollection(), eq(5)))
                .thenReturn(List.of(loc1Stock1, loc2Stock1, loc2Stock2));

        List<ShipmentItem> result = strategy.findShipmentItems(requested);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(item -> item.location().equals(location2));
        assertThat(result).extracting(ShipmentItem::product).containsExactlyInAnyOrder(product1, product2);
    }

    @Test
    void findShipmentItems_whenNoSingleLocationHasAllProducts_throwsNoSuitableLocationException() {
        Map<UUID, Integer> requested = Map.of(product1Id, 5, product2Id, 10);

        Stock loc1Stock1 = Stock.builder().location(location1).product(product1).quantity(10).build();
        Stock loc2Stock2 = Stock.builder().location(location2).product(product2).quantity(15).build();

        when(stockRepository.findByProductIdInAndQuantityGreaterThanEqualOrderByLocation_IdAsc(anyCollection(), eq(5)))
                .thenReturn(List.of(loc1Stock1, loc2Stock2));

        assertThatThrownBy(() -> strategy.findShipmentItems(requested))
                .isInstanceOf(NoSuitableLocationException.class)
                .hasMessageContaining("No single location can fulfil the order");
    }
}