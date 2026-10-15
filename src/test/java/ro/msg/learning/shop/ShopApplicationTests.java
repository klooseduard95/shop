package ro.msg.learning.shop;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ro.msg.learning.shop.config.TestcontainersConfig;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfig.class)
class ShopApplicationTests {

	@Test
	void contextLoads() {
	}
}