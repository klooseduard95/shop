package ro.msg.learning.shop.security;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtUtilTest {

    private JwtUtil jwtUtil() {
        return new JwtUtil(
                new ClassPathResource("keys/jwt-private.pem"),
                new ClassPathResource("keys/jwt-public.pem"));
    }

    @Test
    void generateToken_isDecodableWithPersistentPublicKeyAndContainsIdentityAndRoles() {
        JwtUtil firstInstance = jwtUtil();
        JwtUtil secondInstance = jwtUtil();
        var authentication = new UsernamePasswordAuthenticationToken(
                "admin", "password", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        String token = firstInstance.generateToken(authentication);
        var decoded = secondInstance.jwtDecoder().decode(token);

        assertThat(decoded.getSubject()).isEqualTo("admin");
        assertThat(decoded.getClaimAsString("scope")).isEqualTo("ROLE_ADMIN");
        assertThat(decoded.getIssuedAt()).isNotNull();
        assertThat(decoded.getExpiresAt()).isAfter(decoded.getIssuedAt());
    }

    @Test
    void constructor_whenKeyResourceDoesNotExist_rejectsConfiguration() {
        Resource missingResource = mock(Resource.class);
        when(missingResource.exists()).thenReturn(false);

        assertThatThrownBy(() -> new JwtUtil(
                missingResource,
                new ClassPathResource("keys/jwt-public.pem")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invalid RSA JWT classpath key configuration")
                .hasRootCauseInstanceOf(IllegalArgumentException.class)
                .rootCause()
                .hasMessageContaining("JWT key resource does not exist");
    }

    @Test
    void constructor_whenPemContentIsNull_rejectsConfiguration() throws Exception {
        Resource nullPemResource = mock(Resource.class);
        when(nullPemResource.exists()).thenReturn(true);
        when(nullPemResource.getContentAsString(StandardCharsets.US_ASCII)).thenReturn(null);

        assertThatThrownBy(() -> new JwtUtil(
                nullPemResource,
                new ClassPathResource("keys/jwt-public.pem")))
                .isInstanceOf(IllegalStateException.class)
                .hasRootCauseInstanceOf(IllegalArgumentException.class)
                .rootCause()
                .hasMessage("JWT PRIVATE KEY key must be configured");
    }

    @Test
    void constructor_whenPemContentIsBlank_rejectsConfiguration() {
        Resource blankPemResource = new ByteArrayResource(" \n\t".getBytes(StandardCharsets.US_ASCII));

        assertThatThrownBy(() -> new JwtUtil(
                blankPemResource,
                new ClassPathResource("keys/jwt-public.pem")))
                .isInstanceOf(IllegalStateException.class)
                .hasRootCauseInstanceOf(IllegalArgumentException.class)
                .rootCause()
                .hasMessage("JWT PRIVATE KEY key must be configured");
    }
}
