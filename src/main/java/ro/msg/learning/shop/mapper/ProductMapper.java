package ro.msg.learning.shop.mapper;

import org.springframework.stereotype.Component;
import ro.msg.learning.shop.dto.request.ProductRequest;
import ro.msg.learning.shop.dto.response.ProductResponse;
import ro.msg.learning.shop.model.Product;
import ro.msg.learning.shop.model.ProductCategory;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        if (product == null) {
            return null;
        }
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .weight(product.getWeight())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .categoryDescription(product.getCategory() != null ? product.getCategory().getDescription() : null)
                .build();
    }

    public Product toModel(ProductRequest dto) {
        if (dto == null) {
            return null;
        }
        return Product.builder()
                .name(dto.getName())
                .price(dto.getPrice())
                .weight(dto.getWeight())
                .build();
    }
}
