package com.fsweb.twitterapi.config;

import org.springframework.context.annotation.Bean; // Bean tanımlamak için
import org.springframework.context.annotation.Configuration; // Konfigürasyon sınıfı olduğunu belirtmek için
import org.springframework.web.cors.CorsConfiguration; // CORS konfigürasyonu için
import org.springframework.web.cors.UrlBasedCorsConfigurationSource; // URL bazlı CORS kaynakları için
import org.springframework.web.filter.CorsFilter; // CORS filtresi oluşturmak için

import java.util.Arrays; // Listeleri kolayca oluşturmak için

//React ön yüzümüzle iletişim kurarken
// yaşayacağım CORS sorununu
// şimdiden önleyecek


@Configuration // Bu sınıfın bir Spring konfigürasyon sınıfı olduğunu belirtir.
public class CorsConfig {

    @Bean // Bu metot tarafından döndürülen objenin Spring IoC container'ında bir Spring Bean'i olarak yönetileceğini belirtir.
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Kredensiyellerin (çerezler, HTTP kimlik doğrulama veya istemci SSL sertifikaları)
        // CORS istekleriyle gönderilmesine izin verir.
        config.setAllowCredentials(true);

        // TODO:React uygulamanızın çalıştığı URL. Kendi React uygulamamn portunu (3200) buraya yazacağım
        //  Ayrıca, üretim ortamı için canlı URL'yi (örn: "https://your-react-app.com") buraya ekleyeceğim
        config.setAllowedOrigins(Arrays.asList("http://localhost:3200", "http://127.0.0.1:3200")); // React'in çalıştığı port

        // Tüm HTTP metotlarına (GET, POST, PUT, DELETE vb.) izin verir.
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));

        // Tüm başlıkların (headers) CORS isteklerinde kullanılmasına izin verir.
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With", "remember-me"));

        // Tarayıcının CORS yanıtının önbelleğe alınabileceği süreyi (saniye cinsinden) ayarlar.
        config.setMaxAge(3600L);

        // Tüm yollara (endpoints) bu CORS konfigürasyonunu uygular.
        source.registerCorsConfiguration("/**", config);

        // Oluşturulan CORS konfigürasyonunu kullanarak bir CorsFilter döndürür.
        return new CorsFilter(source);
    }
}
////