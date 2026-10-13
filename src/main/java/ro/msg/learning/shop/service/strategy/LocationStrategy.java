package ro.msg.learning.shop.service.strategy;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface LocationStrategy {

    /**
     *  Decide from which locations the requested products are taken.
     *
     * @return one item per requested product
     * @throws ro.msg.learning.shop.exception.NoSuitableLocationException if the order cannot be fulfilled
     */
    List<ShipmentItem> findShipmentItems(Map<UUID, Integer> requested);
}
