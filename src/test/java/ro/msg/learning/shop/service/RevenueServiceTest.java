package ro.msg.learning.shop.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.msg.learning.shop.exception.InvalidReportDateException;
import ro.msg.learning.shop.model.Location;
import ro.msg.learning.shop.model.Revenue;
import ro.msg.learning.shop.repository.LocationRepository;
import ro.msg.learning.shop.repository.LocationRevenue;
import ro.msg.learning.shop.repository.OrderDetailRepository;
import ro.msg.learning.shop.repository.RevenueRepository;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RevenueServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2024, 5, 21);
    private static final LocalDate REPORT_DATE = TODAY.minusDays(1);

    @Mock
    private RevenueRepository revenueRepository;

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @Mock
    private LocationRepository locationRepository;

    @Spy
    private Clock clock = Clock.fixed(TODAY.atTime(0, 5).toInstant(ZoneOffset.UTC), ZoneOffset.UTC);

    @InjectMocks
    private RevenueService revenueService;

    @Test
    void computeAndStore_usesHalfOpenDayBoundaries() {
        Location location = location("Depozit Nord");
        when(locationRepository.findAllForRevenueUpdate()).thenReturn(List.of(location));
        when(orderDetailRepository.sumRevenueByLocation(
                REPORT_DATE.atStartOfDay(), REPORT_DATE.plusDays(1).atStartOfDay()))
                .thenReturn(List.of(new LocationRevenue(location.getId(), new BigDecimal("29.00"))));

        revenueService.computeAndStore(REPORT_DATE);

        verify(orderDetailRepository).sumRevenueByLocation(
                eq(REPORT_DATE.atStartOfDay()),
                eq(REPORT_DATE.plusDays(1).atStartOfDay()));
        verify(revenueRepository).saveAll(argThat(revenues -> {
            Revenue stored = revenues.iterator().next();
            return stored.getLocation().equals(location)
                    && stored.getDate().equals(REPORT_DATE)
                    && stored.getSum().compareTo(new BigDecimal("29.00")) == 0
                    && stored.getCalculatedAt() != null;
        }));
    }

    @Test
    void computeAndStore_updatesExistingRevenueAndStoresZeroForLocationWithoutSales() {
        Location soldFrom = location("Depozit Nord");
        Location withoutSales = location("Depozit Sud");
        Revenue existing = Revenue.builder()
                .id(UUID.randomUUID())
                .location(soldFrom)
                .date(REPORT_DATE)
                .sum(BigDecimal.ONE)
                .calculatedAt(REPORT_DATE.minusDays(1).atStartOfDay())
                .build();
        when(locationRepository.findAllForRevenueUpdate()).thenReturn(List.of(soldFrom, withoutSales));
        when(orderDetailRepository.sumRevenueByLocation(any(), any()))
                .thenReturn(List.of(new LocationRevenue(soldFrom.getId(), new BigDecimal("29.00"))));
        when(revenueRepository.findByDateOrderByLocation_NameAscLocation_IdAsc(REPORT_DATE))
                .thenReturn(List.of(existing));

        revenueService.computeAndStore(REPORT_DATE);

        verify(revenueRepository).saveAll(argThat(revenues -> {
            List<Revenue> stored = (List<Revenue>) revenues;
            return stored.size() == 2
                    && stored.getFirst() == existing
                    && stored.getFirst().getSum().compareTo(new BigDecimal("29.00")) == 0
                    && stored.get(1).getSum().compareTo(BigDecimal.ZERO) == 0;
        }));
    }

    @Test
    void computeAndStore_whenDateIsTodayOrLater_isRejected() {
        assertThatThrownBy(() -> revenueService.computeAndStore(TODAY))
                .isInstanceOf(InvalidReportDateException.class);
        assertThatThrownBy(() -> revenueService.computeAndStore(TODAY.plusDays(1)))
                .isInstanceOf(InvalidReportDateException.class);

        verifyNoInteractions(revenueRepository);
    }

    @Test
    void yesterday_isDerivedFromTheClock() {
        assertThat(revenueService.yesterday()).isEqualTo(REPORT_DATE);
    }

    @Test
    void exportCsv_rendersHeaderAndEscapesQuotes() {
        when(revenueRepository.findByDateOrderByLocation_NameAscLocation_IdAsc(REPORT_DATE))
                .thenReturn(List.of(revenue("Depozit \"Nord\"", "29.00")));

        String csv = revenueService.exportCsv(REPORT_DATE);

        assertThat(csv).startsWith("locationId,locationName,date,sum\n");
        assertThat(csv).contains(",\"Depozit \"\"Nord\"\"\",2024-05-20,29.00");
    }

    @Test
    void findByDate_whenDateIsInTheFuture_isRejected() {
        LocalDate futureDate = TODAY.plusDays(1);

        assertThatThrownBy(() -> revenueService.findByDate(futureDate))
                .isInstanceOf(InvalidReportDateException.class)
                .hasMessage("Date must not be in the future, but was: " + futureDate);

        verifyNoInteractions(revenueRepository);
    }

    @Test
    void exportCsv_whenDateIsInTheFuture_isRejected() {
        LocalDate futureDate = TODAY.plusDays(1);

        assertThatThrownBy(() -> revenueService.exportCsv(futureDate))
                .isInstanceOf(InvalidReportDateException.class);

        verifyNoInteractions(revenueRepository);
    }

    private Revenue revenue(String locationName, String sum) {
        Location location = location(locationName);
        return Revenue.builder()
                .location(location)
                .date(REPORT_DATE)
                .sum(new BigDecimal(sum))
                .calculatedAt(LocalDateTime.now())
                .build();
    }

    private Location location(String name) {
        return Location.builder().id(UUID.randomUUID()).name(name).build();
    }
}
