package ro.msg.learning.shop.config;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class TimeZoneResolver {

    private final ShopProperties shopProperties;

    public ZoneId resolve() {
        String timeZone = shopProperties.getTimeZone();

        return timeZone == null || timeZone.isBlank() ? ZoneId.systemDefault() : ZoneId.of(timeZone);
    }
}
