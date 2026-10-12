package ro.msg.learning.shop.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    @NotBlank(message = "Product name cannot be blank")
    private String name;

    @NotNull(message = "Product price cannot be null")
    @DecimalMin(value = "0.01", message = "Price must be strictly positive")
    private BigDecimal price;

    @NotNull(message = "Product weight is required")
    private Double weight;

    @NotNull(message = "Category ID is required")
    private UUID categoryId;
}
