package ro.msg.learning.shop.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.scheduling.support.CronExpression;
import ro.msg.learning.shop.scheduler.RevenueReportJob;
import ro.msg.learning.shop.service.RevenueService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SchedulingConfigTest {

    private static final String CRON = "0 5 0 * * *";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(TaskSchedulingAutoConfiguration.class))
            .withUserConfiguration(SchedulingConfig.class, ClockConfig.class, RevenueReportJob.class)
            .withBean(RevenueService.class, () -> mock(RevenueService.class))
            .withPropertyValues("shop.revenue.cron=" + CRON);

    @Test
    void cronRunsFiveMinutesAfterMidnightOfTheFollowingDay() {
        CronExpression cron = CronExpression.parse(CRON);

        LocalDateTime nextRun = cron.next(LocalDateTime.of(2024, 5, 20, 23, 0));

        assertThat(nextRun).isEqualTo(LocalDateTime.of(2024, 5, 21, 0, 5));
    }

    @Test
    void jobIsRegisteredByDefault() {
        contextRunner.withBean(ShopProperties.class)
                .run(context -> assertThat(context).hasSingleBean(RevenueReportJob.class));
    }

    @Test
    void jobIsAbsentWhenDisabled() {
        contextRunner.withBean(ShopProperties.class)
                .withPropertyValues("shop.revenue.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(RevenueReportJob.class));
    }

    @Test
    void clockDefaultsToTheSystemZone() {
        ShopProperties properties = new ShopProperties();

        assertThat(properties.resolveZoneId()).isEqualTo(ZoneId.systemDefault());
        assertThat(new ClockConfig().clock(properties).getZone()).isEqualTo(ZoneId.systemDefault());
    }

    @Test
    void clockDefaultsToTheSystemZoneWhenConfiguredZoneIsNull() {
        ShopProperties properties = new ShopProperties();
        properties.setTimeZone(null);

        assertThat(properties.resolveZoneId()).isEqualTo(ZoneId.systemDefault());
        assertThat(new ClockConfig().clock(properties).getZone()).isEqualTo(ZoneId.systemDefault());
    }

    @Test
    void clockUsesTheConfiguredZoneWhenOneIsSet() {
        ShopProperties properties = new ShopProperties();
        properties.setTimeZone("Europe/Bucharest");

        Clock clock = new ClockConfig().clock(properties);

        assertThat(clock.getZone()).isEqualTo(ZoneId.of("Europe/Bucharest"));
        assertThat(properties.resolveZoneId()).isEqualTo(ZoneId.of("Europe/Bucharest"));
    }
}
