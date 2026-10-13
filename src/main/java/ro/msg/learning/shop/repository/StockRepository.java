package ro.msg.learning.shop.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ro.msg.learning.shop.model.*;

import java.util.*;

@Repository
public interface StockRepository extends JpaRepository<Stock, StockId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select s from Stock s
            join fetch s.product
            join fetch s.location
            where s.product.id in :productIds
            order by s.location.id asc, s.product.id asc
            """)
    List<Stock> findByProductIdInOrderByLocation_IdAsc(@Param("productIds") Collection<UUID> productIds);
}
