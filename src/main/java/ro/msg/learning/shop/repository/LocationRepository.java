package ro.msg.learning.shop.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import ro.msg.learning.shop.model.Location;

import java.util.List;
import java.util.UUID;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT location FROM Location location ORDER BY location.id")
    List<Location> findAllForRevenueUpdate();
}