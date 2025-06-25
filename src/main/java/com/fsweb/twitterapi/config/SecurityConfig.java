package com.fsweb.twitterapi.config;

import org.springframework.context.annotation.Bean; // Bean tanımlamak için
import org.springframework.context.annotation.Configuration; // Konfigürasyon sınıfı olduğunu belirtmek için
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Şifreleme algoritması için
import org.springframework.security.crypto.password.PasswordEncoder; // Şifreleyici arayüzü için

@Configuration // Bu sınıfın bir Spring konfigürasyon sınıfı olduğunu belirtir. Spring, uygulamayı başlatırken bu sınıftaki `@Bean` metodlarını arayacaktır.
public class SecurityConfig {

    @Bean // Bu metot tarafından döndürülen objenin Spring IoC (Inversion of Control) container'ında bir Spring Bean'i olarak yönetileceğini belirtir.
    public PasswordEncoder passwordEncoder() {
        // BCryptPasswordEncoder, Spring Security tarafından önerilen ve kullanılan, güçlü bir şifre hash'leme algoritmasıdır.
        // Şifreleri düz metin olarak saklamak yerine, hashleyerek güvende tutarız.
        return new BCryptPasswordEncoder();
    }

    // TODO:Spring Security'nin diğer konfigürasyonları
    //  (HttpSecurity, AuthenticationManager vb.) daha sonra ekleyeceğim.
    //  Şimdilik sadece PasswordEncoder bean'i sağlıyoruz.
}