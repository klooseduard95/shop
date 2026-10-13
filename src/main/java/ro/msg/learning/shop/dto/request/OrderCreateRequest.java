package ro.msg.learning.shop.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @Valid
    @NotNull(message = "Delivery address is required")
    private AddressRequest deliveryAddress;

    @Valid
    @NotEmpty(message = "Orders must contain at least one product")
    private List<ProductQuantityRequest> products;
}
