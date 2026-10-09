package ro.msg.learning.shop.mapper;

import org.springframework.stereotype.Component;
import ro.msg.learning.shop.dto.request.OrderCreateRequest;
import ro.msg.learning.shop.dto.request.ProductQuantityRequest;
import ro.msg.learning.shop.dto.response.OrderResponse;
import ro.msg.learning.shop.model.Address;
import ro.msg.learning.shop.model.Order;
import ro.msg.learning.shop.model.OrderInput;

import java.util.stream.Collectors;

@Component
public class OderMapper {

    public OrderInput toModel(OrderCreateRequest request) {
        Address address = Address.builder()
                .country(request.getDeliveryAddress().getCountry())
                .city(request.getDeliveryAddress().getCity())
                .county(request.getDeliveryAddress().getCounty())
                .streetAddress(request.getDeliveryAddress().getStreetAddress())
                .build();

        var productQuantitiesMap = request.getProducts().stream()
                .collect(Collectors.toMap(
                        ProductQuantityRequest::getProductId,
                        ProductQuantityRequest::getQuantity
                ));

        return OrderInput.builder()
                .createdAt(request.getCreatedAt())
                .userId(request.getUserId())
                .deliveryAddress(address)
                .productQuantities(productQuantitiesMap)
                .build();
    }

    public OrderResponse toResponse(Order order) {
        if (order == null) {
            return null;
        }
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .deliveryAddressId(order.getDeliveryAddress() != null ? order.getDeliveryAddress().getId() : null)
                .createdAt(order.getCreatedAt())
                .build();
    }
}
