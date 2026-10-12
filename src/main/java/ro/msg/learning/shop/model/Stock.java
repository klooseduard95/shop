package ro.msg.learning.shop.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stocks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Stock {

    @EmbeddedId
    private StockId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("locationId")
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(nullable = false)
    private Integer quantity;
}