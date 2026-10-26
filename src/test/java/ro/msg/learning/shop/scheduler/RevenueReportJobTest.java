package ro.msg.learning.shop.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.msg.learning.shop.service.RevenueService;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RevenueReportJobTest {

    @Mock
    private RevenueService revenueService;

    @InjectMocks
    private RevenueReportJob revenueReportJob;

    @Test
    void reportYesterdaysRevenue_delegatesWithTheDateProvidedByTheService() {
        LocalDate yesterday = LocalDate.of(2024, 5, 20);
        when(revenueService.yesterday()).thenReturn(yesterday);
        when(revenueService.computeAndStore(yesterday)).thenReturn(List.of());

        revenueReportJob.reportYesterdaysRevenue();

        verify(revenueService).computeAndStore(yesterday);
    }

    @Test
    void reportYesterdaysRevenue_acrossAYearBoundary_reportsTheLastDayOfThePreviousYear() {
        LocalDate lastDayOfPreviousYear = LocalDate.of(2023, 12, 31);
        when(revenueService.yesterday()).thenReturn(lastDayOfPreviousYear);
        when(revenueService.computeAndStore(lastDayOfPreviousYear)).thenReturn(List.of());

        revenueReportJob.reportYesterdaysRevenue();

        verify(revenueService).computeAndStore(lastDayOfPreviousYear);
    }

    @Test
    void reportYesterdaysRevenue_whenAggregationFails_swallowsTheExceptionSoTheScheduleSurvives() {
        LocalDate yesterday = LocalDate.of(2024, 5, 20);
        when(revenueService.yesterday()).thenReturn(yesterday);
        when(revenueService.computeAndStore(yesterday)).thenThrow(new IllegalStateException("boom"));

        assertThatCode(() -> revenueReportJob.reportYesterdaysRevenue()).doesNotThrowAnyException();
    }
}
