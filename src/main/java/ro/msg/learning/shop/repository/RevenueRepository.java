package ro.msg.learning.shop.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.msg.learning.shop.model.Revenue;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface RevenueRepository extends JpaRepository<Revenue, UUID> {

    @EntityGraph(attributePaths = "location")
    List<Revenue> findByDateOrderByLocation_NameAscLocation_IdAsc(LocalDate date);
}
