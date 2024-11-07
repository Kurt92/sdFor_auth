package com.jt.sdfor_auth.framework.core.jwt;

import com.jt.sdfor_auth.biz.entity.UserMng;
import com.jt.sdfor_auth.framework.core.cookie.JwtTokenEnum;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
@Component
public class JwtUtil {

    //: todo 시크릿키 방식에서 비대칭키 방식으로 바꿀것

    @Value("${spring.data.jwt.secret}")
    private String SECRET_KEY;

    private Key getSigningKey(String secretKey) {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims extractAllClaims(String token) throws ExpiredJwtException {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey(SECRET_KEY))
                .build()

                .parseClaimsJws(token)
                .getBody();
    }

    public String getUsername(String token) {
        return extractAllClaims(token).get("username", String.class);
    }

    public Boolean isTokenExpired(String token) {
        final Date expiration = extractAllClaims(token).getExpiration();
        return expiration.before(new Date());
    }

    public String generateAccessToken(UserMng userMng) {
        return doGenerateToken(userMng, JwtTokenEnum.acc.getExpiredTime());
    }

    public String generateRefreshToken(UserMng userMng) {
        return doGenerateToken(userMng, JwtTokenEnum.ref.getExpiredTime());
    }

    public String doGenerateToken(UserMng userMng, long expireTime) {

        Claims claims = Jwts.claims();
        claims.put("accountId", userMng.getAccountId());
        claims.put("userNm", userMng.getUserNm());
        claims.put("email", userMng.getEmail());

        String jwt = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expireTime))
                .signWith(getSigningKey(SECRET_KEY), SignatureAlgorithm.HS256)
                .compact();

        return jwt;
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsername(token);

        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

}