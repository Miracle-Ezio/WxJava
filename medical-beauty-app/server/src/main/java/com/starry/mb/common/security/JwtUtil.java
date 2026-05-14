package com.starry.mb.common.security;

import com.starry.mb.common.context.PrincipalContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expiresMillis;

    public JwtUtil(@Value("${starry.jwt.secret}") String secret,
                   @Value("${starry.jwt.expires-hours}") long expiresHours) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiresMillis = expiresHours * 3600_000L;
    }

    public String issue(PrincipalContext.Principal p) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(String.valueOf(p.getId()))
                .claim("type", p.getType().name())
                .claim("tid", p.getTenantId())
                .claim("sid", p.getStoreId())
                .issuedAt(new Date(now))
                .expiration(new Date(now + expiresMillis))
                .signWith(key)
                .compact();
    }

    public PrincipalContext.Principal parse(String token) {
        Claims c = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
        Long id  = Long.parseLong(c.getSubject());
        Long tid = c.get("tid", Number.class).longValue();
        Number sidNum = c.get("sid", Number.class);
        Long sid = sidNum == null ? null : sidNum.longValue();
        PrincipalContext.Type type = PrincipalContext.Type.valueOf(c.get("type", String.class));
        return new PrincipalContext.Principal(id, type, tid, sid);
    }
}
