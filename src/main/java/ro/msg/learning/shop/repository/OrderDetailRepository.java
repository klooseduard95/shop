package ro.msg.learning.shop.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ro.msg.learning.shop.model.*;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, OrderDetailId> {

    @Query("""
            SELECT new ro.msg.learning.shop.repository.LocationRevenue(
                    od.shippedFrom.id, SUM(od.quantity * od.purchasedPrice))
            FROM OrderDetail od
            WHERE od.shippedFrom IS NOT NULL
              AND od.order.createdAt >= :startInclusive
              AND od.order.createdAt < :endExclusive
            GROUP BY od.shippedFrom.id
            """)
    List<LocationRevenue> sumRevenueByLocation(
            @Param("startInclusive") LocalDateTime startInclusive,
            @Param("endExclusive") LocalDateTime endExclusive);

    long countByShippedFromIsNullAndOrder_CreatedAtGreaterThanEqualAndOrder_CreatedAtLessThan(
            LocalDateTime startInclusive, LocalDateTime endExclusive);
}
