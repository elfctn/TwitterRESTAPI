package com.fsweb.twitterapi.config;

import com.fsweb.twitterapi.security.CustomUserDetailsService; // Kendi UserDetailsService'imizi import ediyoruz
import com.fsweb.twitterapi.security.jwt.JwtAuthEntryPoint; // JWT hata giriş noktamızı import ediyoruz
import com.fsweb.twitterapi.security.jwt.JwtAuthFilter; // JWT filtrelemizi import ediyoruz

import org.springframework.context.annotation.Bean; // Bean tanımlamak için
import org.springframework.context.annotation.Configuration; // Konfigürasyon sınıfı olduğunu belirtmek için
import org.springframework.security.authentication.AuthenticationManager; // Kimlik doğrulama yöneticisi için
import org.springframework.security.authentication.dao.DaoAuthenticationProvider; // Veritabanı tabanlı kimlik doğrulama sağlayıcısı için
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration; // AuthenticationManager'ı almak için
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // Metot bazlı güvenlik (örn. @PreAuthorize) için
import org.springframework.security.config.annotation.web.builders.HttpSecurity; // HTTP güvenlik kurallarını yapılandırmak için
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity; // Web güvenliğini etkinleştirmek için
import org.springframework.security.config.http.SessionCreationPolicy; // Oturum yönetim politikası için
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Şifreleme algoritması için (zaten vardı)
import org.springframework.security.crypto.password.PasswordEncoder; // Şifreleyici arayüzü için (zaten vardı)
import org.springframework.security.web.SecurityFilterChain; // Güvenlik filtresi zinciri için
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // Varsayılan kullanıcı adı/şifre filtresi için (JWT filtresi bundan önce çalışacak)

import static org.springframework.security.config.Customizer.withDefaults; // Varsayılan yapılandırmaları kullanmak için (örneğin CORS için)

@Configuration // Bu sınıfın bir Spring konfigürasyon sınıfı olduğunu belirtir.
@EnableWebSecurity // Spring Security'yi etkinleştirir ve web güvenliği yapılandırmasına izin verir.
@EnableMethodSecurity // Metot bazlı güvenliği (örn. @PreAuthorize, @PostAuthorize) etkinleştirir.
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService; // Kendi UserDetailsService'imizi enjekte ediyoruz
    private final JwtAuthEntryPoint unauthorizedHandler; // Yetkisiz erişim hata giriş noktamızı enjekte ediyoruz
    private final JwtAuthFilter jwtAuthFilter; // JWT filtrelemizi enjekte ediyoruz
    // CorsConfig'ten gelen CorsFilter bean'ini otomatik olarak tanıyacaktır.

    // Constructor Injection: Spring, bağımlılıkları otomatik olarak sağlar.
    public SecurityConfig(CustomUserDetailsService userDetailsService, JwtAuthEntryPoint unauthorizedHandler, JwtAuthFilter jwtAuthFilter) {
        this.userDetailsService = userDetailsService;
        this.unauthorizedHandler = unauthorizedHandler;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean // PasswordEncoder bean'i (önceden vardı, şimdi de burada kalıyor)
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean // DaoAuthenticationProvider bean'i: Kullanıcı detaylarını yüklemek ve şifreleri doğrulamak için kullanılır.
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService); // Kendi UserDetailsService'imizi set ediyoruz
        authProvider.setPasswordEncoder(passwordEncoder()); // PasswordEncoder'ı set ediyoruz
        return authProvider;
    }

    @Bean // AuthenticationManager bean'i: Kimlik doğrulama sürecini yönetir. UserService'teki login metodu bunu kullanacak.
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean // SecurityFilterChain bean'i: HTTP güvenlik kurallarını ve filtre zincirini tanımlar.
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CORS'u etkinleştirir (CorsConfig sınıfımızda tanımladığımız CorsFilter bean'i otomatik olarak algılanır).
                .cors(withDefaults()) // Spring Security 6+ ile CORS'u etkinleştirmenin kolay yolu

                // CSRF (Cross-Site Request Forgery) korumasını devre dışı bırak.
                // REST API'lerde JWT kullandığımızda oturum (session) tutmadığımız için CSRF'ye genellikle ihtiyaç duyulmaz.
                .csrf(csrf -> csrf.disable())

                // Kimlik doğrulama hataları için giriş noktasını belirler.
                // Yetkisiz erişimlerde (örn. token yok veya geçersiz), UnauthorizedHandler devreye girer (401 Unauthorized döner).
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(unauthorizedHandler))

                // Oturum yönetimini STATELESS (durumsuz) olarak ayarlar.
                // JWT kullandığımız için sunucu tarafında oturum tutmuyoruz. Her istek token ile doğrulanır.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // İstek yetkilendirme kurallarını tanımlar
                .authorizeHttpRequests(auth -> auth
                        // /register ve /login endpoint'lerine kimlik doğrulaması olmadan erişime izin ver.
                        .requestMatchers("/auth/**").permitAll() // Genellikle /auth/register, /auth/login gibi endpoint'ler

                        // Geliştirme ortamında h2-console'a erişime izin ver (eğer kullanıyorsan)
                        // .requestMatchers("/h2-console/**").permitAll()

                        // Diğer tüm isteklere kimlik doğrulama gerektirir.
                        .anyRequest().authenticated()
                );

        // Veritabanı tabanlı kimlik doğrulama sağlayıcısını ekle.
        http.authenticationProvider(authenticationProvider());

        // JWT doğrulama filtremizi, Spring Security'nin UsernamePasswordAuthenticationFilter'ından önce ekler.
        // Böylece her istek geldiğinde önce JWT tokenı doğrulanır.
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build(); // Güvenlik filtre zincirini inşa et ve döndür
    }
}