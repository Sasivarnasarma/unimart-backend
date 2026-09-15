package varna.mit.kln.unimart.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;
import varna.mit.kln.unimart.auth.entity.User;

import java.time.Instant;

@Component
public class JwtTokenProvider {

    private final JwtEncoder jwtEncoder;
    private final long expiryDuration;

    public JwtTokenProvider(
            JwtEncoder jwtEncoder,
            @Value("${app.security.jwt-expiration-ms:86400000}") long expiryDuration
    ) {
        this.jwtEncoder = jwtEncoder;
        this.expiryDuration = expiryDuration;
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(expiryDuration);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("unimart-backend")
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(user.getUniversityEmail())
                .claim("userId", user.getId())
                .claim("fullName", user.getFullName())
                .claim("role", user.getRole().name())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
