package ro.msg.learning.shop.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import ro.msg.learning.shop.model.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface StockRepository extends JpaRepository<Stock, StockId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Stock> findByProductIdInAndQuantityGreaterThanEqualOrderByLocation_IdAsc(Collection<UUID> productIds, Integer quantity);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Stock> findFirstByProductIdOrderByQuantityDescLocation_IdAsc(UUID productId);

    List<Stock> findByLocationIdOrderByProduct_NameAscProduct_IdAsc(UUID locationId);
    boolean existsByProductId(UUID productId);
    void deleteAllByProductId(UUID productId);
}
