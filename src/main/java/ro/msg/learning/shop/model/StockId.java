package ro.msg.learning.shop.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockId implements Serializable {
    private UUID productId;
    private UUID locationId;
}
