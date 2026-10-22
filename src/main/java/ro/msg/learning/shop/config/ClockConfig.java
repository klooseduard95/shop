package ro.msg.learning.shop.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ClockConfig {

    @Bean
    public Clock clock(TimeZoneResolver timeZoneResolver) {
        return Clock.system(timeZoneResolver.resolve());
    }
}
