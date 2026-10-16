package ro.msg.learning.shop.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.ZoneId;

@Getter
@Setter
@ConfigurationProperties(prefix = "shop")
public class ShopProperties {

    private LocationStrategyType locationStrategy = LocationStrategyType.SINGLE_LOCATION;

    private String timeZone = "";

    private final Revenue revenue = new Revenue();

    public ZoneId resolveZoneId() {
        return timeZone == null || timeZone.isBlank() ? ZoneId.systemDefault() : ZoneId.of(timeZone);
    }

    @Getter
    @Setter
    public static class Revenue {

        private boolean enabled = true;

        private String cron = "0 5 0 * * *";
    }
}
