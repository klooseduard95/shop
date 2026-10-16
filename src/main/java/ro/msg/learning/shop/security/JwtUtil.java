package ro.msg.learning.shop.security;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;

    public JwtUtil(
            @Value("${jwt.private-key}") Resource privateKeyResource,
            @Value("${jwt.public-key}") Resource publicKeyResource
    ) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            this.privateKey = (RSAPrivateKey) keyFactory.generatePrivate(
                    new PKCS8EncodedKeySpec(decodePem(readPem(privateKeyResource), "PRIVATE KEY"))
            );
            this.publicKey = (RSAPublicKey) keyFactory.generatePublic(
                    new X509EncodedKeySpec(decodePem(readPem(publicKeyResource), "PUBLIC KEY"))
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Invalid RSA JWT classpath key configuration", exception);
        }
    }

    private static String readPem(Resource resource) throws java.io.IOException {
        if (!resource.exists()) {
            throw new IllegalArgumentException("JWT key resource does not exist: " + resource);
        }
        return resource.getContentAsString(StandardCharsets.US_ASCII);
    }

    private static byte[] decodePem(String pem, String type) {
        if (pem == null || pem.isBlank()) {
            throw new IllegalArgumentException("JWT " + type + " key must be configured");
        }

        String normalized = pem.trim()
                .replace("\\n", "\n")
                .replace("-----BEGIN " + type + "-----", "")
                .replace("-----END " + type + "-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(normalized);
    }

    public String generateToken(Authentication authentication) {
        Instant now = Instant.now();
        Instant expiration = now.plus(24, ChronoUnit.HOURS);

        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        return Jwts.builder()
                .subject(authentication.getName())
                .claim("scope", roles)
                .claim("iat", now.getEpochSecond())
                .claim("exp", expiration.getEpochSecond())
                .signWith(privateKey)
                .compact();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(this.publicKey).build();
    }
}
