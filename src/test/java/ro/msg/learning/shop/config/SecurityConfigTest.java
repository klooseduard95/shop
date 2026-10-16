package ro.msg.learning.shop.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    private final SecurityConfig securityConfig = new SecurityConfig();

    @Test
    void corsConfiguration_allowsConfiguredFrontendAndApiMethods() {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/products");

        var configuration = securityConfig.corsConfigurationSource().getCorsConfiguration(request);

        assertThat(configuration).isNotNull();
        assertThat(configuration.getAllowedOrigins()).containsExactly("http://localhost:3000");
        assertThat(configuration.getAllowedMethods()).containsExactly("GET", "POST", "PUT", "DELETE");
        assertThat(configuration.getAllowedHeaders())
                .containsExactly("Authorization", "Content-Type", "X-Requested-With");
        assertThat(configuration.getAllowCredentials()).isTrue();
    }

    @Test
    void jwtAuthenticationConverter_mapsScopeWithoutAddingPrefix() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("admin")
                .claim("scope", "ROLE_ADMIN ROLE_CUSTOMER")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();

        var authentication = securityConfig.jwtAuthenticationConverter().convert(jwt);

        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo("admin");
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .contains("ROLE_ADMIN", "ROLE_CUSTOMER")
                .doesNotContain("SCOPE_ROLE_ADMIN", "SCOPE_ROLE_CUSTOMER");
    }
}
