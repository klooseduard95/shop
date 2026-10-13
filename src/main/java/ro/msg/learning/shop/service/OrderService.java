package ro.msg.learning.shop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.msg.learning.shop.exception.ResourceNotFoundException;
import ro.msg.learning.shop.model.*;
import ro.msg.learning.shop.repository.*;
import ro.msg.learning.shop.service.command.OrderInput;
import ro.msg.learning.shop.service.strategy.LocationStrategy;
import ro.msg.learning.shop.service.strategy.ShipmentItem;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;

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
    private final Clock clock;
    private final LocationStrategy locationStrategy;

    @Transactional
    public Order createOrder(OrderInput input) {
        Map<UUID, Integer> requested = input.productQuantities();

        User user = findUser(input.userId());
        ensureProductsExist(requested.keySet());

        List<ShipmentItem> shipment = locationStrategy.findShipmentItems(requested);
        decreaseStock(shipment);

        Order order = saveOrder(user, input);
        orderDetailRepository.saveAll(buildOrderDetails(order, shipment));
        log.info("Order created: orderId={}, userId={}, itemCount={}", order.getId(), user.getId(), requested.size());

        return order;
    }

    private void ensureProductsExist(Set<UUID> productIds) {
        Set<UUID> missing = new HashSet<>(productIds);
        productRepository.findAllById(productIds).forEach(
                p -> missing.remove(p.getId())
        );
        if (!missing.isEmpty()) {
            throw new ResourceNotFoundException("Products not found with ids: " + missing);
        }
    }

    private List<OrderDetail> buildOrderDetails(Order order, List<ShipmentItem> shipment) {
        return shipment.stream()
                .map(item ->OrderDetail.builder()
                        .id(new OrderDetailId(order.getId(), item.product().getId()))
                        .order(order)
                        .product(item.product())
                        .shippedFrom(item.location())
                        .quantity(item.quantity())
                        .purchasedPrice(item.product().getPrice())
                        .build())
                .toList();
    }

    private Order saveOrder(User user, OrderInput input) {
        Address address = addressRepository.save(input.deliveryAddress());

        return orderRepository.save(Order.builder()
                .user(user)
                .deliveryAddress(address)
                .createdAt(LocalDateTime.now(clock))
                .build());
    }

    private void decreaseStock(List<ShipmentItem> shipment) {
        shipment.forEach(item -> {
            Stock stock = stockRepository
                    .findByProductIdAndLocationId(item.product().getId(), item.location().getId())
                    .orElseThrow(() -> new IllegalStateException("Stock not found for product " + item.product().getId() + " at location " + item.location().getId()));
            stock.setQuantity(stock.getQuantity() - item.quantity());
        });
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }
}
