package ro.msg.learning.shop.service.strategy;

import lombok.RequiredArgsConstructor;
import ro.msg.learning.shop.exception.NoSuitableLocationException;
import ro.msg.learning.shop.model.Stock;
import ro.msg.learning.shop.repository.StockRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class MostAbundantStrategy implements LocationStrategy {

    private final StockRepository stockRepository;

    @Override
    public List<ShipmentItem> findShipmentItems(Map<UUID, Integer> requested) {
        if (requested.isEmpty()) {
            throw new IllegalArgumentException("Requested products must not be empty");
        }

        return requested.entrySet().stream()
                .map(e -> toShipmentItem(e.getKey(), e.getValue(),
                        stockRepository.findFirstByProductIdOrderByQuantityDescLocation_IdAsc(e.getKey())))
                .toList();
    }

    private ShipmentItem toShipmentItem(UUID productId, int quantity, Optional<Stock> richest) {
        Stock stock = richest
                .filter(s -> s.getQuantity() >= quantity)
                .orElseThrow(() -> new NoSuitableLocationException(
                        "No location has enough stock of product " + productId + " (requested " + quantity + ")"));
        return new ShipmentItem(stock, quantity);
    }
}
