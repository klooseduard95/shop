package ro.msg.learning.shop.service.strategy;

import lombok.RequiredArgsConstructor;
import ro.msg.learning.shop.exception.NoSuitableLocationException;
import ro.msg.learning.shop.model.Stock;
import ro.msg.learning.shop.repository.StockRepository;

import java.util.*;

import static java.util.stream.Collectors.*;

@RequiredArgsConstructor
public class SingleLocationStrategy implements LocationStrategy {

    private final StockRepository stockRepository;

    @Override
    public List<ShipmentItem> findShipmentItems(Map<UUID, Integer> requested) {
        if (requested.isEmpty()) {
            throw new IllegalArgumentException("Requested products must not be empty");
        }

        Integer minRequestedQuantity = Collections.min(requested.values());
        List<Stock> stocks = stockRepository.findByProductIdInAndQuantityGreaterThanEqualOrderByLocation_IdAsc(
                List.copyOf(requested.keySet()),
                minRequestedQuantity
        );

        return stocks.stream()
                .collect(groupingBy(s -> s.getLocation().getId(), LinkedHashMap::new, toList()))
                .values().stream()
                .filter(locationStocks -> canFulfill(locationStocks, requested))
                .findFirst()
                .map(locationStocks -> toShipmentItems(locationStocks, requested))
                .orElseThrow(() -> new NoSuitableLocationException("No single location can fulfil the order: " + requested));
    }

    private List<ShipmentItem> toShipmentItems(List<Stock> locationStocks, Map<UUID, Integer> requested) {
        return locationStocks.stream()
                .map(s -> new ShipmentItem(s, requested.get(s.getProduct().getId())))
                .toList();
    }

    private boolean canFulfill(List<Stock> locationStocks, Map<UUID, Integer> requested) {
        Map<UUID, Integer> available = locationStocks.stream()
                .collect(toMap(s -> s.getProduct().getId(), Stock::getQuantity));

        return requested.entrySet().stream()
                .allMatch(e -> available.getOrDefault(e.getKey(), 0) >= e.getValue());
    }
}
