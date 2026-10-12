package ro.msg.learning.shop.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private UUID id;
    private String name;
    private BigDecimal price;
    private Double weight;
    private UUID categoryId;
    private String categoryName;
    private String categoryDescription;
}
