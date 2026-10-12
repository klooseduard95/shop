package ro.msg.learning.shop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.msg.learning.shop.exception.*;
import ro.msg.learning.shop.model.*;
import ro.msg.learning.shop.repository.*;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    @Transactional
    public Order createOrder(OrderInput input) {
        Map<UUID, Integer> requested = input.getProductQuantities();

        User user = findUser(input.getUserId());
        Map<UUID, Product> products = findProducts(requested.keySet());

        List<Stock> stocks = selectStocksFromSingleLocation(requested);
        Location location = stocks.getFirst().getLocation();

        decreaseStock(stocks, requested);

        Order order = saveOrder(user, input);
        orderDetailRepository.saveAll(buildOrderDetails(order, location, requested, products));
        log.info("Order created: orderId={}, userId={}, locationId={}, itemCount={}", order.getId(), user.getId(), location.getId(), requested.size());

        return order;
    }

    private Iterable<OrderDetail> buildOrderDetails(Order order, Location location, Map<UUID, Integer> requested, Map<UUID, Product> products) {
        return requested.entrySet().stream()
                .map(e -> {
                    Product product = products.get(e.getKey());
                    return OrderDetail.builder()
                            .id(new OrderDetailId(order.getId(), product.getId()))
                            .order(order)
                            .product(product)
                            .shippedFrom(location)
                            .quantity(e.getValue())
                            .purchasedPrice(product.getPrice())
                            .build();
                })
                .toList();
    }

    private Order saveOrder(User user, OrderInput input) {
        Address address = addressRepository.save(input.getDeliveryAddress());

        return orderRepository.save(Order.builder()
                .user(user)
                .deliveryAddress(address)
                .createdAt(input.getCreatedAt())
                .build());
    }

    private void decreaseStock(List<Stock> stocks, Map<UUID, Integer> requested) {
        stocks.forEach(stock ->
                stock.setQuantity(stock.getQuantity() - requested.get(stock.getProduct().getId())));
        stockRepository.saveAll(stocks);
        log.debug("Decreased stock for {} product(s) at locationId={}",
                stocks.size(), stocks.isEmpty() ? null : stocks.getFirst().getLocation().getId());
    }

    private List<Stock> selectStocksFromSingleLocation(Map<UUID, Integer> requested) {
        List<Stock> stocks = stockRepository.findByProductIdInOrderByLocation_IdAsc(List.copyOf(requested.keySet()));

        return stocks.stream()
                .collect(groupingBy(s -> s.getLocation().getId(), LinkedHashMap::new, toList()))
                .values().stream()
                .filter(locationStocks -> canFulfill(locationStocks, requested))
                .findFirst()
                .orElseThrow(() -> new NoSuitableLocationException(
                        "No single location can fulfil the order: " + requested));
    }

    private boolean canFulfill(List<Stock> locationStocks, Map<UUID, Integer> requested) {
        Map<UUID, Integer> available = locationStocks.stream()
                .collect(toMap(s -> s.getProduct().getId(), Stock::getQuantity));

        return requested.entrySet().stream()
                .allMatch(e -> available.getOrDefault(e.getKey(), 0) >= e.getValue());
    }

    private Map<UUID, Product> findProducts(Set<UUID> productIds) {
        Map<UUID, Product> products = productRepository.findAllById(productIds).stream()
                .collect(toMap(Product::getId, Function.identity()));

        Set<UUID> missing = new HashSet<>(productIds);
        missing.removeAll(products.keySet());
        if (!missing.isEmpty()) {
            throw new ResourceNotFoundException("Products not found with ids: " + missing);
        }

        return products;
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(()->new ResourceNotFoundException("User not found!"));
    }
}
