package com.fsweb.twitterapi.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import com.fsweb.twitterapi.security.UserPrincipal; // UserPrincipal'ı import ediyoruz

import java.util.Date;
import java.util.UUID; // UUID için import


import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtProvider.class);

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationMs}")
    private int jwtExpirationMs;

    private byte[] getSigningKeyBytes() {
        return Decoders.BASE64.decode(jwtSecret);
    }

    /**
     * Kimlik doğrulama objesinden (Authentication) JWT token oluşturur.
     * JWT Subject olarak kullanıcının UUID'sini (String olarak) içerir.
     *
     * @param authentication Spring Security'nin kimlik doğrulama objesi
     * @return Oluşturulan JWT token (String)
     */
    public String generateJwtToken(Authentication authentication) {
        // Principal objesini UserPrincipal olarak alıyoruz, çünkü ID'ye ihtiyacımız var.
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(userPrincipal.getId().toString()) // KESİNLİKLE DÜZELTME: Subject olarak kullanıcının UUID'sini (String olarak) kullanıyoruz
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, getSigningKeyBytes())
                .compact();
    }

    /**
     * JWT token'ından kullanıcı ID'sini (UUID String olarak) çıkarır.
     *
     * @param token JWT token (String)
     * @return Kullanıcı ID'si (String olarak)
     */
    public String getUserIdFromJwtToken(String token) { // KESİNLİKLE DÜZELTME: Metot adı getUserIdFromJwtToken oldu.
        try {
            String userId = Jwts.parser()
                    .setSigningKey(getSigningKeyBytes())
                    .parseClaimsJws(token)
                    .getBody().getSubject(); // Subject'ten artık UUID String'i çekiyoruz
            logger.info("JwtProvider: Retrieved User ID from JWT: {}", userId); // Log mesajını güncelledik
            return userId;
        } catch (Exception e) {
            logger.error("JwtProvider: Failed to get user ID from JWT token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * JWT token'ını doğrular.
     *
     * @param authToken Doğrulanacak JWT token (String)
     * @return Token geçerliyse true, değilse false
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser()
                    .setSigningKey(getSigningKeyBytes())
                    .parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("JwtProvider: Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JwtProvider: JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JwtProvider: JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JwtProvider: JWT claims string is empty: {}", e.getMessage());
        } catch (SignatureException e) {
            logger.error("JwtProvider: Invalid JWT signature: {}", e.getMessage());
        }
        return false;
    }
}