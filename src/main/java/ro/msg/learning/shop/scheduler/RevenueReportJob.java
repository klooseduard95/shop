package ro.msg.learning.shop.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ro.msg.learning.shop.service.RevenueService;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "shop.revenue.enabled", havingValue = "true", matchIfMissing = true)
public class RevenueReportJob {

    private final RevenueService revenueService;

    @Scheduled(cron = "${shop.revenue.cron}", zone = "${shop.time-zone:}")
    public void reportYesterdaysRevenue() {
        LocalDate reportDate = revenueService.yesterday();
        try {
            revenueService.computeAndStore(reportDate);
        } catch (Exception ex) {
            log.error("Daily revenue report failed for date={}", reportDate, ex);
        }
    }
}
