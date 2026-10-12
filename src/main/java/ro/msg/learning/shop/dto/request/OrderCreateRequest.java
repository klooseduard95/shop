package ro.msg.learning.shop.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.*;

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
