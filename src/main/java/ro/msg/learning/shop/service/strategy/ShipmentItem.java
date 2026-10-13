package ro.msg.learning.shop.service.strategy;

import ro.msg.learning.shop.model.Location;
import ro.msg.learning.shop.model.Product;

public record ShipmentItem(Location location, Product product, int quantity) {
}
