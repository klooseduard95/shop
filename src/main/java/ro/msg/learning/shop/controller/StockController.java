package ro.msg.learning.shop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ro.msg.learning.shop.service.StockExportService;

import java.util.UUID;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockExportService stockExportService;

    @GetMapping(value = "/export/{locationId}", produces = "text/csv")
    public String export(@PathVariable UUID locationId) {
        return stockExportService.export(locationId);
    }
}
