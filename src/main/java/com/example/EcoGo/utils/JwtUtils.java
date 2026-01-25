package com.example.EcoGo.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
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

    // 在生产环境中，这个密钥应该存储在环境变量或配置文件中
    // 必须足够长以满足 HS256 算法要求
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // Token 有效期：7天
    private static final long EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000L;

    /**
     * 生成 Token
     * 
     * @param userId  用户ID
     * @param isAdmin 是否管理员
     * @return token字符串
     */
    public String generateToken(String userId, boolean isAdmin) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("isAdmin", isAdmin);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * 验证 Token 并获取 Claims
     * 
     * @param token token字符串
     * @return Claims 对象 (如果验证失败则抛出异常)
     */
    public Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            throw new RuntimeException("Invalid JWT token");
        }
    }

    /**
     * 获取过期时间
     */
    public Date getExpirationDate(String token) {
        return validateToken(token).getExpiration();
    }
}
