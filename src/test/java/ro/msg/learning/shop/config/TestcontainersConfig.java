package ro.msg.learning.shop.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfig {

    public static final String IMAGE_PROPERTY = "testcontainers.postgres.image";

    private static final String DEFAULT_IMAGE = "postgres:16-alpine";

    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(
            DockerImageName.parse(System.getProperty(IMAGE_PROPERTY, DEFAULT_IMAGE)));

    static {
        POSTGRES.start();
    }

    public static PostgreSQLContainer container() {
        return POSTGRES;
    }

    @Bean(destroyMethod = "")
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return POSTGRES;
    }
}
