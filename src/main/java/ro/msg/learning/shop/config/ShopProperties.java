package ro.msg.learning.shop.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "shop")
public class ShopProperties {

    private LocationStrategyType locationStrategy = LocationStrategyType.SINGLE_LOCATION;
}
