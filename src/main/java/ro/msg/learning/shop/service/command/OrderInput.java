package ro.msg.learning.shop.service.command;

import ro.msg.learning.shop.model.Address;

import java.util.Map;
import java.util.UUID;

public record OrderInput(
        UUID userId,
        Address deliveryAddress,
        Map<UUID, Integer> productQuantities
) {
}
