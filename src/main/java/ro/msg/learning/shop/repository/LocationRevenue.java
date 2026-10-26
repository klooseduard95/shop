package ro.msg.learning.shop.repository;

import java.math.BigDecimal;
import java.util.UUID;

public record LocationRevenue(UUID locationId, BigDecimal sum) {
}
