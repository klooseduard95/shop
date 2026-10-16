package ro.msg.learning.shop.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RevenueResponse(UUID locationId, String locationName, LocalDate date, BigDecimal sum) {
}
