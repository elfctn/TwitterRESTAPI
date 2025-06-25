package com.fsweb.twitterapi.security.jwt;

import com.fsweb.twitterapi.security.CustomUserDetailsService; // Kullanıcı detaylarını yüklemek için
import jakarta.servlet.FilterChain; // Servlet filtresi zinciri için
import jakarta.servlet.ServletException; // Servlet istisnası için
import jakarta.servlet.http.HttpServletRequest; // HTTP isteği için
import jakarta.servlet.http.HttpServletResponse; // HTTP yanıtı için
import org.slf4j.Logger; // Loglama için
import org.slf4j.LoggerFactory; // Loglama için
import org.springframework.beans.factory.annotation.Autowired; // Bağımlılık enjeksiyonu için
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Kimlik doğrulama objesi için
import org.springframework.security.core.context.SecurityContextHolder; // Spring Security context'ini yönetmek için
import org.springframework.security.core.userdetails.UserDetails; // Kullanıcı detayları arayüzü
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource; // Web kimlik doğrulama detayları için
import org.springframework.stereotype.Component; // Spring bileşeni olduğunu belirtmek için
import org.springframework.util.StringUtils; // String yardımcı metotları için
import org.springframework.web.filter.OncePerRequestFilter; // Her isteği bir kez filtrelemek için

import java.io.IOException; // I/O istisnası için

@Component // Bu anotasyon, Spring'e bu sınıfın bir bileşen olduğunu ve otomatik olarak yönetilmesi gerektiğini belirtir.
public class JwtAuthFilter extends OncePerRequestFilter {
    // OncePerRequestFilter, her HTTP isteğinin yalnızca bir kez filtreden geçmesini sağlar.
    // Bu, çoklu filtre zincirlerinde tekrar eden işlemleri önler.

    @Autowired // Spring, bu bağımlılıkları otomatik olarak enjekte eder. Field Injection kullanıldı, Constructor Injection da mümkündü.
    private JwtProvider jwtProvider;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class); // Loglama için logger objesi

    /**
     * Gelen her HTTP isteğini filtreler.
     * JWT tokenını çıkarır, doğrular ve kullanıcı kimlik doğrulamasını Spring Security context'ine set eder.
     *
     * @param request HTTP isteği objesi
     * @param response HTTP yanıtı objesi
     * @param filterChain Filtre zinciri
     * @throws ServletException Servlet hatası durumunda
     * @throws IOException G/Ç hatası durumunda
     */
    @Override // OncePerRequestFilter arayüzünden gelen metodu override ediyoruz
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. Authorization header'ından JWT tokenını al
            String jwt = parseJwt(request);

            // 2. Eğer token mevcut ve geçerliyse
            if (jwt != null && jwtProvider.validateJwtToken(jwt)) {
                // Token'dan kullanıcı adını (subject) çıkar
                String username = jwtProvider.getUserNameFromJwtToken(jwt);

                // Kullanıcı adına göre UserDetails objesini yükle (veritabanından)
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                // Spring Security'nin kimlik doğrulama tokenını oluştur
                // Bu token, kullanıcının kimlik doğrulamasının başarıyla tamamlandığını gösterir.
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, // Yüklenen UserDetails objesi
                                null,        // Kimlik bilgileri (şifre gibi), JWT'de şifre tutulmaz
                                userDetails.getAuthorities()); // Kullanıcının yetkileri/rolleri

                // Kimlik doğrulama detaylarını set et (isteğin IP adresi, session ID'si vb.)
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Spring Security context'inde kimlik doğrulamasını set et
                // Bu, geçerli istek için kullanıcının kimlik doğrulandığı anlamına gelir.
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage()); // Kimlik doğrulama hatasını logla
        }

        // Filtre zincirindeki bir sonraki filtreye isteği ve yanıtı ilet
        filterChain.doFilter(request, response);
    }

    /**
     * HTTP isteğinin Authorization header'ından JWT tokenını ayrıştırır.
     * "Bearer <TOKEN>" formatındaki tokenı alır.
     *
     * @param request HTTP isteği objesi
     * @return Ayrıştırılmış JWT token (String) veya null
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization"); // Authorization header'ını al

        // Authorization header'ı mevcutsa ve "Bearer " ile başlıyorsa
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            // "Bearer " kısmını atlayarak tokenı döndür
            return headerAuth.substring(7); // "Bearer ".length() = 7
        }
        return null; // Token bulunamazsa null döndür
    }
}