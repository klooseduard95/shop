package ro.msg.learning.shop.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import ro.msg.learning.shop.model.*;

import java.util.*;

@Repository
public interface StockRepository extends JpaRepository<Stock, StockId> {
    List<Stock> findByProductIdInOrderByLocation_IdAsc(List<UUID> productsIds);
}
