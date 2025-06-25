package com.fsweb.twitterapi.security.jwt;

import jakarta.servlet.ServletException; // Servlet API'den gelen istisna için
import jakarta.servlet.http.HttpServletRequest; // HTTP isteği için
import jakarta.servlet.http.HttpServletResponse; // HTTP yanıtı için
import org.slf4j.Logger; // Loglama için
import org.slf4j.LoggerFactory; // Loglama için
import org.springframework.security.core.AuthenticationException; // Spring Security kimlik doğrulama istisnası için
import org.springframework.security.web.AuthenticationEntryPoint; // Kimlik doğrulama giriş noktası arayüzü
import org.springframework.stereotype.Component; // Spring bileşeni olduğunu belirtmek için

import java.io.IOException; // I/O işlemleri için

@Component // Bu anotasyon, Spring'e bu sınıfın bir bileşen olduğunu ve otomatik olarak yönetilmesi gerektiğini belirtir.
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthEntryPoint.class); // Loglama için logger objesi

    /**
     * Kimlik doğrulaması yapılmamış bir kullanıcı güvenli bir kaynağa erişmeye çalıştığında çağrılır.
     *
     * @param request HTTP isteği objesi
     * @param response HTTP yanıtı objesi
     * @param authException Fırlatılan kimlik doğrulama istisnası
     * @throws IOException G/Ç hatası durumunda
     * @throws ServletException Servlet hatası durumunda
     */
    @Override // AuthenticationEntryPoint arayüzünden gelen metodu override ediyoruz
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        logger.error("Unauthorized error: {}", authException.getMessage()); // Hata mesajını logla
        // Kimlik doğrulaması yapılmamış isteklere 401 Unauthorized HTTP durum kodu ve hata mesajı döndür.
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized");
    }
}