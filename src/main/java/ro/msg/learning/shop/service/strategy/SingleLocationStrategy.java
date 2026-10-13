package ro.msg.learning.shop.service.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ro.msg.learning.shop.exception.NoSuitableLocationException;
import ro.msg.learning.shop.model.Stock;
import ro.msg.learning.shop.repository.StockRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Locale.filter;
import static java.util.stream.Collectors.*;

@RequiredArgsConstructor
public class SingleLocationStrategy implements LocationStrategy {

    private final StockRepository stockRepository;

    @Override
    public List<ShipmentItem> findShipmentItems(Map<UUID, Integer> requested) {
        List<Stock> stocks = stockRepository.findByProductIdInOrderByLocation_IdAsc(List.copyOf(requested.keySet()));

        return stocks.stream()
                .collect(groupingBy(s -> s.getLocation().getId(), LinkedHashMap::new, toList()))
                .values().stream()
                .filter(locationStocks -> canFullfill(locationStocks, requested))
                .findFirst()
                .map(locationStocks -> toShipmentItems(locationStocks, requested))
                .orElseThrow(() -> new NoSuitableLocationException("No single location can fulfil the order: " + requested));
    }

    private List<ShipmentItem> toShipmentItems(List<Stock> locationStocks, Map<UUID, Integer> requested) {
        return locationStocks.stream()
                .map(s -> new ShipmentItem(s.getLocation(), s.getProduct(), requested.get(s.getProduct().getId())))
                .toList();
    }

    private boolean canFullfill(List<Stock> locationStocks, Map<UUID, Integer> requested) {
        Map<UUID, Integer> available = locationStocks.stream()
                .collect(toMap(s -> s.getProduct().getId(), Stock::getQuantity));

        return requested.entrySet().stream()
                .allMatch(e -> available.getOrDefault(e.getKey(), 0) >= e.getValue());
    }
}
