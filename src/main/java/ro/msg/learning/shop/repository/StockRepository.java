package ro.msg.learning.shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.msg.learning.shop.model.Stock;
import ro.msg.learning.shop.model.StockId;

import java.util.List;
import java.util.UUID;

@Repository
public interface StockRepository extends JpaRepository<Stock, StockId> {
    List<Stock> findByProductIdInOrderByLocation_IdAsc(List<UUID> productsIds);
}
