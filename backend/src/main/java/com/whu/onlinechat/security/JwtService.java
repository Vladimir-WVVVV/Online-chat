package com.whu.onlinechat.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final JwtProperties properties;
    private final SecretKey key;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generate(CurrentUser user) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(String.valueOf(user.id()))
            .claim("username", user.username())
            .claim("role", user.role())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(properties.expireMinutes() * 60)))
            .signWith(key)
            .compact();
    }

    public CurrentUser parse(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
        return new CurrentUser(
            Long.valueOf(claims.getSubject()),
            claims.get("username", String.class),
            claims.get("role", String.class)
        );
    }
}

