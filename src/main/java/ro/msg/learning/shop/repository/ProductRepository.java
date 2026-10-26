package ro.msg.learning.shop.repository;

import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import ro.msg.learning.shop.model.Product;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    @EntityGraph(attributePaths = "category")
    Page<Product> findAll(@NonNull Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "category")
    @NonNull
    Optional<Product> findById(@NonNull UUID id);
}
