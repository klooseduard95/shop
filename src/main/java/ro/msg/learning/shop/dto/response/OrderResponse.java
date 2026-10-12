package ro.msg.learning.shop.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private UUID id;
    private UUID userId;
    private UUID deliveryAddressId;
    private LocalDateTime createdAt;
}
