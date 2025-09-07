// src/main/java/com/example/demoapp/util/JwtUtil.java
package com.example.demoapp.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    // ✅ Generate a secure 512-bit key for HS512
    private final SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    private long expiration = 86400000; // 24 hours

    // ✅ Generate JWT token
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key) // Uses 512-bit key
                .compact();
    }

    // ✅ Validate token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()  // ✅ Correct: Jwts (not Jws)
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ✅ Extract username from token
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()  // ✅ Correct: Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
}
