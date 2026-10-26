package ro.msg.learning.shop;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTest
@AutoConfigureMockMvc
class RevenueSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Flyway flyway;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void resetDatabase() {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    void getRevenues_withoutAuthentication_isUnauthorized() throws Exception {
        mockMvc.perform(get("/api/revenues").param("date", yesterday()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void runRevenueReport_isForbiddenForCustomersAndAllowedForAdmins() throws Exception {
        mockMvc.perform(post("/api/revenues/run")
                        .param("date", yesterday())
                        .header("Authorization", "Bearer " + login("andrei.popescu")))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/revenues/run")
                        .param("date", yesterday())
                        .header("Authorization", "Bearer " + login("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getRevenues_afterAnAdminRun_returnsTheStoredReport() throws Exception {
        mockMvc.perform(post("/api/revenues/run")
                        .param("date", yesterday())
                        .header("Authorization", "Bearer " + login("admin")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/revenues")
                        .param("date", yesterday())
                        .header("Authorization", "Bearer " + login("andrei.popescu")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].locationName").exists())
                .andExpect(jsonPath("$[0].sum").value(0));
    }

    @Test
    void exportRevenues_returnsCsv() throws Exception {
        mockMvc.perform(get("/api/revenues/export")
                        .param("date", yesterday())
                        .accept("text/csv")
                        .header("Authorization", "Bearer " + login("admin")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(content().string(org.hamcrest.Matchers.startsWith("locationId,locationName,date,sum\n")));
    }

    private String yesterday() {
        return LocalDate.now().minusDays(1).toString();
    }

    private String login(String username) throws Exception {
        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", username, "password", "changeme"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }
}
