package ro.msg.learning.shop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ro.msg.learning.shop.dto.response.RevenueResponse;
import ro.msg.learning.shop.mapper.RevenueMapper;
import ro.msg.learning.shop.model.Revenue;
import ro.msg.learning.shop.service.RevenueService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/revenues")
@RequiredArgsConstructor
public class RevenueController {

    private final RevenueService revenueService;
    private final RevenueMapper revenueMapper;

    @GetMapping
    public List<RevenueResponse> getRevenues(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return toResponses(revenueService.findByDate(date));
    }

    @GetMapping(value = "/export", produces = "text/csv")
    public String export(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return revenueService.exportCsv(date);
    }

    @PostMapping("/run")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<RevenueResponse> run(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return toResponses(revenueService.computeAndStore(date));
    }

    private List<RevenueResponse> toResponses(List<Revenue> revenues) {
        return revenues.stream().map(revenueMapper::toResponse).toList();
    }
}
