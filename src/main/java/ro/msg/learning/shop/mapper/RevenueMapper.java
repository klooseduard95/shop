package ro.msg.learning.shop.mapper;

import org.springframework.stereotype.Component;
import ro.msg.learning.shop.dto.response.RevenueResponse;
import ro.msg.learning.shop.model.Revenue;

@Component
public class RevenueMapper {

    public RevenueResponse toResponse(Revenue revenue) {
        return new RevenueResponse(
                revenue.getLocation().getId(),
                revenue.getLocation().getName(),
                revenue.getDate(),
                revenue.getSum());
    }
}
