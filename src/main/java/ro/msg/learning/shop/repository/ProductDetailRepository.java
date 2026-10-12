package ro.msg.learning.shop.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import ro.msg.learning.shop.model.ProductDetail;

import java.util.UUID;

@Repository
public interface ProductDetailRepository extends JpaRepository<ProductDetail, UUID> {
}
