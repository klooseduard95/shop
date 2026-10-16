package ro.msg.learning.shop.controller;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ro.msg.learning.shop.IntegrationTest;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
@AutoConfigureMockMvc
class ProductControllerLazyLoadingIntegrationTest {

    private static final UUID PRODUCT_ZACUSCA = UUID.fromString("d0000000-0000-0000-0000-000000000001");

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
    void getProductById_resolvesTheLazyCategory() throws Exception {
        mockMvc.perform(get("/api/products/{id}", PRODUCT_ZACUSCA)
                        .header("Authorization", "Bearer " + login("andrei.popescu")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Zacuscă de vinete, 350 g"))
                .andExpect(jsonPath("$.categoryName").value("Conserve și murături"));
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
