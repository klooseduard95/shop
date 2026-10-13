package ro.msg.learning.shop.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
