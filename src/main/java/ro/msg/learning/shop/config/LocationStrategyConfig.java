package ro.msg.learning.shop.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ro.msg.learning.shop.repository.StockRepository;
import ro.msg.learning.shop.service.strategy.LocationStrategy;
import ro.msg.learning.shop.service.strategy.MostAbundantStrategy;
import ro.msg.learning.shop.service.strategy.SingleLocationStrategy;

@Configuration
public class LocationStrategyConfig {

    @Bean
    @ConditionalOnProperty(name = "shop.location-strategy", havingValue = "single-location", matchIfMissing = true)
    public LocationStrategy singleLocationStrategy(StockRepository stockRepository) {
        return new SingleLocationStrategy(stockRepository);
    }

    @Bean
    @ConditionalOnProperty(name = "shop.location-strategy", havingValue = "most-abundant")
    public LocationStrategy mostAbundandStrategy(StockRepository stockRepository) {
        return new MostAbundantStrategy(stockRepository);
    }
}
