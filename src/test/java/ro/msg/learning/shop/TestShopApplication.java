package ro.msg.learning.shop;

import org.springframework.boot.SpringApplication;
import ro.msg.learning.shop.config.TestcontainersConfig;

public class TestShopApplication {

    public static void main(String[] args) {
        SpringApplication.from(ShopApplication::main)
                .with(TestcontainersConfig.class)
                .run(args);
    }
}
