package ro.msg.learning.shop.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ro.msg.learning.shop.exception.GlobalExceptionHandler;
import ro.msg.learning.shop.exception.InvalidReportDateException;
import ro.msg.learning.shop.mapper.RevenueMapper;
import ro.msg.learning.shop.model.Location;
import ro.msg.learning.shop.model.Revenue;
import ro.msg.learning.shop.service.RevenueService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RevenueController.class)
@Import({RevenueMapper.class, GlobalExceptionHandler.class})
class RevenueControllerTest {

    private static final LocalDate REPORT_DATE = LocalDate.of(2024, 5, 20);
    private static final UUID LOCATION_ID = UUID.fromString("e0000000-0000-0000-0000-000000000001");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RevenueService revenueService;

    @Test
    void getRevenues_returnsTheStoredReport() throws Exception {
        when(revenueService.findByDate(REPORT_DATE)).thenReturn(List.of(revenue("Depozit București", "29.00")));

        mockMvc.perform(get("/api/revenues").param("date", "2024-05-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].locationId").value(LOCATION_ID.toString()))
                .andExpect(jsonPath("$[0].locationName").value("Depozit București"))
                .andExpect(jsonPath("$[0].date").value("2024-05-20"))
                .andExpect(jsonPath("$[0].sum").value(29.00));
    }

    @Test
    void export_returnsCsv() throws Exception {
        when(revenueService.exportCsv(REPORT_DATE)).thenReturn("locationId,locationName,date,sum\n");

        mockMvc.perform(get("/api/revenues/export").param("date", "2024-05-20").accept("text/csv"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(content().string("locationId,locationName,date,sum\n"));
    }

    @Test
    void getRevenues_whenDateIsMissing_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/revenues"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Missing required parameter 'date'"));
    }

    @Test
    void getRevenues_whenDateIsMalformed_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/revenues").param("date", "not-a-date"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid value for parameter 'date'"));
    }

    @Test
    void getRevenues_whenServiceRejectsFutureDate_returnsBadRequest() throws Exception {
        LocalDate futureDate = LocalDate.of(2024, 5, 23);
        String errorMessage = "Date must not be in the future, but was: " + futureDate;
        when(revenueService.findByDate(futureDate))
                .thenThrow(new InvalidReportDateException(errorMessage));

        mockMvc.perform(get("/api/revenues").param("date", futureDate.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void run_whenDayIsNotComplete_returnsBadRequest() throws Exception {
        when(revenueService.computeAndStore(any()))
                .thenThrow(new InvalidReportDateException("Revenue can only be computed for a completed day"));

        mockMvc.perform(post("/api/revenues/run").param("date", "2024-05-21"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Revenue can only be computed for a completed day"));
    }

    @Test
    void run_recomputesTheReport() throws Exception {
        when(revenueService.computeAndStore(REPORT_DATE)).thenReturn(List.of(revenue("Depozit București", "29.00")));

        mockMvc.perform(post("/api/revenues/run").param("date", "2024-05-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sum").value(29.00));
    }

    private Revenue revenue(String locationName, String sum) {
        return Revenue.builder()
                .location(Location.builder().id(LOCATION_ID).name(locationName).build())
                .date(REPORT_DATE)
                .sum(new BigDecimal(sum))
                .calculatedAt(LocalDateTime.now())
                .build();
    }
}
