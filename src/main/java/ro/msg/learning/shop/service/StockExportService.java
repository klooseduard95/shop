package ro.msg.learning.shop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.msg.learning.shop.exception.ResourceNotFoundException;
import ro.msg.learning.shop.model.Stock;
import ro.msg.learning.shop.repository.LocationRepository;
import ro.msg.learning.shop.repository.StockRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockExportService {

    private final LocationRepository locationRepository;
    private final StockRepository stockRepository;

    @Transactional(readOnly = true)
    public String export(UUID locationId) {
        if (!locationRepository.existsById(locationId)) {
            throw new ResourceNotFoundException("Location not found with id: " + locationId);
        }

        List<Stock> stocks = stockRepository.findByLocationIdOrderByProduct_NameAscProduct_IdAsc(locationId);
        StringBuilder csv = new StringBuilder("productId,productName,quantity\n");
        stocks.forEach(stock -> csv.append(stock.getProduct().getId())
                .append(',')
                .append(escape(stock.getProduct().getName()))
                .append(',')
                .append(stock.getQuantity())
                .append('\n'));
        return csv.toString();
    }

    private String escape(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
