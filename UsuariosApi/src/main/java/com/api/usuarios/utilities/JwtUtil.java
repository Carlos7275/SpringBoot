package com.api.usuarios.utilities;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.api.usuarios.entities.usuarios.Usuarios;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String JWT_SECRET;

    @Value("${jwt.expiration}")
    private Long JWT_EXPIRATION;

    public String generateToken(Usuarios user, Boolean sessionActive) {

        long oneYearInMillis = 365L * 24 * 60 * 60 * 1000;
        Date issuedAt = new Date();
        Date expiration = new Date(System.currentTimeMillis() + ((sessionActive) ? oneYearInMillis : JWT_EXPIRATION));

        Map<String, Object> claims = new HashMap<>();
        claims.put("id_usuario", user.getId());
        claims.put("email", user.getCorreo());

        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(String.valueOf(user.getId()))
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, Usuarios userDetails) {
        String subject = getUsernameFromToken(token);
        return subject.equals(userDetails.getId()) && !isTokenExpired(token);
    }

    public String getUsernameFromToken(String token) {
        return getClaim(token, Claims::getSubject);
    }

    private <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(JWT_SECRET.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claimsResolver.apply(claims);
    }

    private Boolean isTokenExpired(String token) {
        return getClaim(token, Claims::getExpiration).before(new Date());
    }
}
