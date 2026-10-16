package ro.msg.learning.shop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ro.msg.learning.shop.config.TestcontainersConfig;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class DatasourceIsolationIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void integrationTestsRunAgainstTheTestcontainersDatabase() throws SQLException {
        String actualUrl;
        try (var connection = dataSource.getConnection()) {
            actualUrl = connection.getMetaData().getURL();
        }

        assertThat(actualUrl)
                .as("integration tests must never touch a developer's local database, "
                    + "because flyway.clean() would drop it")
                .doesNotContain("5432")
                .doesNotContain("shop_db");

        var container = TestcontainersConfig.container();
        assertThat(container.isRunning()).isTrue();
        assertThat(actualUrl).contains(":" + container.getMappedPort(5432) + "/");
    }
}
