package com.fsweb.twitterapi.security.jwt;

import io.jsonwebtoken.*; // JWT kütüphanesinin ana sınıfları
import io.jsonwebtoken.io.Decoders; // Base64 çözücü (io.jsonwebtoken.io paketinden)
import org.slf4j.Logger; // Loglama için
import org.slf4j.LoggerFactory; // Loglama için
import org.springframework.beans.factory.annotation.Value; // application.properties'ten değer okumak için
import org.springframework.security.core.Authentication; // Spring Security kimlik doğrulama objesi için
import org.springframework.security.core.userdetails.UserDetails; // Kullanıcı detayları için
import org.springframework.stereotype.Component; // Spring bileşeni olduğunu belirtmek için

import java.util.Date; // Tarih ve saat için
// import java.nio.charset.StandardCharsets; // JJWT 0.11.x için Decoders.BASE64 yeterli, bu import'a gerek kalmadı.
// import java.util.Base64; // Base64 encoding için (Jwts.io.Decoders kullanıldığı için artık doğrudan gerek yok)

@Component // Bu anotasyon, Spring'e bu sınıfın bir bileşen olduğunu ve otomatik olarak yönetilmesi gerektiğini belirtir.
public class JwtProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtProvider.class); // Loglama için logger objesi

    // application.properties dosyasından JWT secret key'i ve token geçerlilik süresini okuyoruz.
    // Bu değerler güvende tutulmalı ve üretimde doğrudan koda yazılmamalıdır.
    // Secret key Base64 kodlanmış olmalıdır.
    @Value("${app.jwtSecret}") // application.properties'ten jwtSecret değerini oku
    private String jwtSecret;

    @Value("${app.jwtExpirationMs}") // application.properties'ten jwtExpirationMs değerini oku
    private int jwtExpirationMs;

    // JWT secret key'inden byte dizisi olarak anahtar döndürür.
    // JJWT 0.11.x versiyonları genellikle setSigningKey metodu için byte[] bekler.
    private byte[] getSigningKeyBytes() {
        // application.properties'den gelen secret key'i Base64'ten çözerek byte dizisine çeviririz.
        // io.jsonwebtoken.io.Decoders sınıfı Base64 çözme işlemini yapar.
        return Decoders.BASE64.decode(jwtSecret);
    }

    /**
     * Kimlik doğrulama objesinden (Authentication) JWT token oluşturur.
     *
     * @param authentication Spring Security'nin kimlik doğrulama objesi
     * @return Oluşturulan JWT token (String)
     */
    public String generateJwtToken(Authentication authentication) {
        // Kimlik doğrulaması yapılmış kullanıcının detaylarını al
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        // Token'ın oluşturulma zamanını ve geçerlilik bitiş zamanını ayarla
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        // JWT token'ını oluştur ve imzala
        return Jwts.builder()
                .setSubject((userPrincipal.getUsername())) // Token'ın konusu (kullanıcı adı)
                .setIssuedAt(now) // Token'ın oluşturulma zamanı
                .setExpiration(expiryDate) // Token'ın geçerlilik bitiş zamanı
                .signWith(SignatureAlgorithm.HS256, getSigningKeyBytes()) // JJWT 0.11.x için bu signature metodu kullanılır (algoritma sonra, anahtar önce).
                .compact(); // JWT'yi sıkıştırılmış, URL güvenli bir stringe dönüştür
    }

    /**
     * JWT token'ından kullanıcı adını çıkarır.
     *
     * @param token JWT token (String)
     * @return Kullanıcı adı (String)
     */
    public String getUserNameFromJwtToken(String token) {
        try {
            // JJWT 0.11.x versiyonlarında token ayrıştırmak için Jwts.parser() doğrudan kullanılır.
            // setSigningKey metodu byte[] tipi anahtar bekler.
            return Jwts.parser()
                    .setSigningKey(getSigningKeyBytes())
                    .parseClaimsJws(token) // JWS (JSON Web Signature) token'ını ayrıştır
                    .getBody().getSubject(); // Claims içindeki 'subject' (kullanıcı adı) bilgisini al
        } catch (Exception e) {
            logger.error("Failed to get username from JWT token: {}", e.getMessage());
            return null; // Veya daha uygun bir şekilde hata yönetimi
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
            // Token'ı ayrıştırmayı ve doğrulamayı dene. JJWT 0.11.x API'si kullanılıyor.
            Jwts.parser()
                    .setSigningKey(getSigningKeyBytes()) // setSigningKey metodu byte[] anahtar bekler.
                    .parseClaimsJws(authToken);
            return true; // Ayrıştırma başarılıysa token geçerlidir
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage()); // JWT token formatı hatalıysa
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage()); // JWT token süresi dolduysa
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage()); // Desteklenmeyen JWT token formatıysa
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage()); // JWT iddiaları boşsa
        } catch (SignatureException e) { // io.jsonwebtoken.SignatureException yakalanmalı
            logger.error("Invalid JWT signature: {}", e.getMessage()); // JWT imzası geçersizse (secret key uyuşmuyorsa)
        }
        return false; // Herhangi bir hata durumunda token geçersizdir
    }
}