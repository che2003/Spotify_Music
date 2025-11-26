package org.example.spotify_music.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {

    // 密钥 (每次重启生成新密钥，生产环境应固定)
    private static final Key KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    // 过期时间 24小时
    private static final long EXPIRATION = 1000 * 60 * 60 * 24;

    /**
     * 生成 Token (已修正：支持传入 role 参数)
     * @param username 用户名
     * @param userId 用户ID
     * @param role 角色 (如 "admin", "musician", "user")
     */
    public String generateToken(String username, Long userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role); // 【关键】把角色存入 Token

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(KEY)
                .compact();
    }

    /**
     * 解析 Token 获取用户名
     */
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * 解析 Token 获取通用 Claims
     */
    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}