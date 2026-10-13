package ro.msg.learning.shop.service.strategy;

import ro.msg.learning.shop.model.Location;
import ro.msg.learning.shop.model.Product;
import ro.msg.learning.shop.model.Stock;

public record ShipmentItem(Stock stock, int quantity) {

    public Location location() {
        return stock.getLocation();
    }

    public Product product() {
        return stock.getProduct();
    }
}
