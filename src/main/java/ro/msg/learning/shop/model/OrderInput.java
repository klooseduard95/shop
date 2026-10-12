package ro.msg.learning.shop.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderInput {
    private LocalDateTime createdAt;
    private UUID userId;
    private Address deliveryAddress;
    private Map<UUID, Integer> productQuantities;
}
