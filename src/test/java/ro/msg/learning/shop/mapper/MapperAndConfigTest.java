package ro.msg.learning.shop.mapper;

import org.junit.jupiter.api.Test;
import ro.msg.learning.shop.config.LocationStrategyConfig;
import ro.msg.learning.shop.config.LocationStrategyType;
import ro.msg.learning.shop.config.ShopProperties;
import ro.msg.learning.shop.dto.request.AddressRequest;
import ro.msg.learning.shop.dto.request.OrderCreateRequest;
import ro.msg.learning.shop.dto.request.ProductQuantityRequest;
import ro.msg.learning.shop.model.Order;
import ro.msg.learning.shop.model.Product;
import ro.msg.learning.shop.service.strategy.MostAbundantStrategy;
import ro.msg.learning.shop.service.strategy.SingleLocationStrategy;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class MapperAndConfigTest {

    private final OrderMapper orderMapper = new OrderMapper();
    private final ProductMapper productMapper = new ProductMapper();

    @Test
    void orderMapper_handlesNullsAndDuplicateProducts() {
        assertThat(orderMapper.toResponse(null)).isNull();

        Order emptyOrder = Order.builder().id(UUID.randomUUID()).build();
        var response = orderMapper.toResponse(emptyOrder);
        assertThat(response.getUserId()).isNull();
        assertThat(response.getDeliveryAddressId()).isNull();

        UUID productId = UUID.randomUUID();
        OrderCreateRequest requestWithDuplicates = OrderCreateRequest.builder()
                .userId(UUID.randomUUID())
                .deliveryAddress(AddressRequest.builder().country("RO").city("CJ").county("CJ").streetAddress("Str").build())
                .products(List.of(
                        new ProductQuantityRequest(productId, 2),
                        new ProductQuantityRequest(productId, 3)
                ))
                .build();

        var input = orderMapper.toOrderInput(requestWithDuplicates);
        assertThat(input.productQuantities()).containsEntry(productId, 5);
    }

    @Test
    void productMapper_handlesNulls() {
        assertThat(productMapper.toResponse(null)).isNull();
        assertThat(productMapper.toModel(null)).isNull();

        Product productWithoutCategory = Product.builder().id(UUID.randomUUID()).name("Test").build();
        var response = productMapper.toResponse(productWithoutCategory);
        assertThat(response.getCategoryId()).isNull();
        assertThat(response.getCategoryName()).isNull();
        assertThat(response.getCategoryDescription()).isNull();
    }

    @Test
    void locationStrategyConfig_createsBothStrategies() {
        LocationStrategyConfig config = new LocationStrategyConfig();
        ShopProperties props = new ShopProperties();

        props.setLocationStrategy(LocationStrategyType.SINGLE_LOCATION);
        assertThat(config.locationStrategy(props, mock())).isInstanceOf(SingleLocationStrategy.class);

        props.setLocationStrategy(LocationStrategyType.MOST_ABUNDANT);
        assertThat(config.locationStrategy(props, mock())).isInstanceOf(MostAbundantStrategy.class);
    }
}