package ro.msg.learning.shop.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {
    @NotBlank(message = "Country is reqruired")
    private String country;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "County is required")
    private String county;

    @NotBlank(message = "Street address is required")
    private String streetAddress;
}
