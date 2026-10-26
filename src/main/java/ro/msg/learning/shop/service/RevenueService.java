
package ro.msg.learning.shop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.msg.learning.shop.exception.InvalidReportDateException;
import ro.msg.learning.shop.model.Location;
import ro.msg.learning.shop.model.Revenue;
import ro.msg.learning.shop.repository.LocationRepository;
import ro.msg.learning.shop.repository.LocationRevenue;
import ro.msg.learning.shop.repository.OrderDetailRepository;
import ro.msg.learning.shop.repository.RevenueRepository;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RevenueService {

    private static final String CSV_HEADER = "locationId,locationName,date,sum\n";

    private final RevenueRepository revenueRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final LocationRepository locationRepository;
    private final Clock clock;

    @Transactional
    public List<Revenue> computeAndStore(LocalDate reportDate) {
        requireCompletedDay(reportDate);

        LocalDateTime start = reportDate.atStartOfDay();
        LocalDateTime end = reportDate.plusDays(1).atStartOfDay();

        warnAboutUnattributableSales(start, end);

        long startedAt = System.nanoTime();

        List<Location> locations = locationRepository.findAllForRevenueUpdate();

        Map<UUID, BigDecimal> totalsByLocation = calculateTotalsByLocation(start, end);

        Map<UUID, Revenue> existingByLocation = indexExistingRevenue(reportDate);

        LocalDateTime calculatedAt = LocalDateTime.now(clock);

        List<Revenue> report = locations.stream()
                .map(location -> mergeRevenue(
                        existingByLocation.get(location.getId()),
                        location,
                        reportDate,
                        totalsByLocation.getOrDefault(
                                location.getId(), BigDecimal.ZERO),
                        calculatedAt))
                .toList();

        revenueRepository.saveAll(report);
        log.info("Revenue aggregated: date={}, locations={}, durationMs={}", reportDate, report.size(), (System.nanoTime() - startedAt) / 1_000_000);

        return findByDate(reportDate);
    }

    @Transactional(readOnly = true)
    public List<Revenue> findByDate(LocalDate date) {
        requireNotFutureDate(date);
        return revenueRepository.findByDateOrderByLocation_NameAscLocation_IdAsc(date);
    }

    private void requireNotFutureDate(LocalDate date) {
        if (date.isAfter(LocalDate.now(clock))) {
            throw new InvalidReportDateException(
                    "Date must not be in the future, but was: " + date
            );
        }
    }

    @Transactional(readOnly = true)
    public String exportCsv(LocalDate date) {
        StringBuilder csv = new StringBuilder(CSV_HEADER);

        findByDate(date).forEach(revenue -> appendCsvRow(csv, revenue));

        return csv.toString();
    }

    public LocalDate yesterday() {
        return LocalDate.now(clock).minusDays(1);
    }

    private Map<UUID, BigDecimal> calculateTotalsByLocation(LocalDateTime start, LocalDateTime end) {

        return orderDetailRepository.sumRevenueByLocation(start, end).stream()
                .collect(Collectors.toMap(
                        LocationRevenue::locationId,
                        LocationRevenue::sum
                ));
    }

    private Map<UUID, Revenue> indexExistingRevenue(LocalDate reportDate) {

        return findByDate(reportDate).stream()
                .collect(Collectors.toMap(
                        revenue -> revenue.getLocation().getId(),
                        Function.identity()
                ));
    }

    private void requireCompletedDay(LocalDate reportDate) {
        if (!reportDate.isBefore(LocalDate.now(clock))) {
            throw new InvalidReportDateException("Revenue can only be computed for a completed day, but was: " + reportDate);
        }
    }

    private void warnAboutUnattributableSales(
            LocalDateTime start,
            LocalDateTime end) {

        long count = orderDetailRepository
                .countByShippedFromIsNullAndOrder_CreatedAtGreaterThanEqualAndOrder_CreatedAtLessThan(start, end);

        if (count > 0) {
            log.warn("Excluding {} sold item(s) without a shipping location from the revenue of {}", count, start.toLocalDate());
        }
    }

    private Revenue mergeRevenue(Revenue existing, Location location, LocalDate reportDate, BigDecimal sum, LocalDateTime calculatedAt) {

        Revenue revenue = existing != null ? existing : Revenue.builder()
                .location(location)
                .date(reportDate)
                .build();

        revenue.setSum(sum);
        revenue.setCalculatedAt(calculatedAt);

        return revenue;
    }

    private void appendCsvRow(StringBuilder csv, Revenue revenue) {

        csv.append(revenue.getLocation().getId())
                .append(',')
                .append(escapeCsv(revenue.getLocation().getName()))
                .append(',')
                .append(revenue.getDate())
                .append(',')
                .append(revenue.getSum())
                .append('\n');
    }

    private String escapeCsv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
