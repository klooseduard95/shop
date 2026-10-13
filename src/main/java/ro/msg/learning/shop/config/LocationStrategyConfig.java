package ro.msg.learning.shop.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ro.msg.learning.shop.repository.StockRepository;
import ro.msg.learning.shop.service.strategy.LocationStrategy;
import ro.msg.learning.shop.service.strategy.MostAbundantStrategy;
import ro.msg.learning.shop.service.strategy.SingleLocationStrategy;

@Configuration
@EnableConfigurationProperties(ShopProperties.class)
public class LocationStrategyConfig {

    @Bean
    public LocationStrategy locationStrategy(ShopProperties shopProperties, StockRepository stockRepository) {
        return switch (shopProperties.getLocationStrategy()) {
            case SINGLE_LOCATION -> new SingleLocationStrategy(stockRepository);
            case MOST_ABUNDANT -> new MostAbundantStrategy(stockRepository);
        };
    }
}
