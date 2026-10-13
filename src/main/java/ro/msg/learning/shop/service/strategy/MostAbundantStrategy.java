package ro.msg.learning.shop.service.strategy;

import lombok.RequiredArgsConstructor;
import ro.msg.learning.shop.exception.NoSuitableLocationException;
import ro.msg.learning.shop.model.Stock;
import ro.msg.learning.shop.repository.StockRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
public class MostAbundantStrategy implements LocationStrategy {

    private final StockRepository stockRepository;
    
    @Override
    public List<ShipmentItem> findShipmentItems(Map<UUID, Integer> requested) {
        if (requested.isEmpty()) {
            throw new IllegalArgumentException("Requested products must not be empty");
        }

        Map<UUID, Stock> richestStockByProduct = stockRepository
                .findByProductIdInOrderByLocation_IdAsc(List.copyOf(requested.keySet())).stream()
                .collect(toMap(
                        s -> s.getProduct().getId(),
                        Function.identity(),
                        BinaryOperator.maxBy(Comparator.comparingInt(Stock::getQuantity))));

        return requested.entrySet().stream()
                .map(e -> toShipmentItem(e.getKey(), e.getValue(), richestStockByProduct.get(e.getKey())))
                .toList();
    }

    private ShipmentItem toShipmentItem(UUID productId, int quantity, Stock richest) {
        if (richest == null || richest.getQuantity() < quantity) {
            throw new NoSuitableLocationException(
                    "No location has enough stock of product " + productId + " (requested " + quantity + ")");
        }
        return new ShipmentItem(richest, quantity);
    }
}
