package com.fsweb.twitterapi.security;

import com.fsweb.twitterapi.entity.User; // Kendi User entity'mizi import ediyoruz
import com.fsweb.twitterapi.repository.UserRepository; // UserRepository'yi enjekte edeceğiz
import org.springframework.security.core.GrantedAuthority; // Yetkilendirme (rol) için
import org.springframework.security.core.authority.SimpleGrantedAuthority; // Yetki objesi oluşturmak için
import org.springframework.security.core.userdetails.UserDetails; // Spring Security'nin kullanıcı detayları arayüzü
import org.springframework.security.core.userdetails.UserDetailsService; // Spring Security'nin ana UserDetailsService arayüzü
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Kullanıcı bulunamadığında fırlatılacak istisna
import org.springframework.stereotype.Service; // Spring Service bileşeni olduğunu belirtmek için

import java.util.ArrayList; // Dinamik liste için
import java.util.Collection; // Koleksiyon arayüzü için
import java.util.List; // List için

@Service // Bu anotasyon, Spring'e bu sınıfın bir servis bileşeni olduğunu ve otomatik olarak yönetilmesi gerektiğini belirtir.
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository; // UserRepository'yi enjekte ediyoruz (kullanıcı bilgilerini veritabanından çekmek için)

    // Constructor Injection
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Spring Security tarafından kimlik doğrulama sürecinde kullanılır.
     * Veritabanından kullanıcı adı (veya e-posta) ile kullanıcı detaylarını yükler.
     *
     * @param usernameOrEmail Kullanıcı adı veya e-posta
     * @return Spring Security'nin UserDetails objesi
     * @throws UsernameNotFoundException Kullanıcı bulunamazsa fırlatılır
     */
    @Override // UserDetailsService arayüzünden gelen metodu override ediyoruz
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // Veritabanında kullanıcıyı kullanıcı adı veya e-posta ile bulmaya çalış
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail) // Hem username hem email alanına aynı değeri gönderiyoruz
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail));

        // Kullanıcının yetkilerini (rollerini) belirle
        // Şimdilik tüm kullanıcılara varsayılan olarak "ROLE_USER" rolünü atıyoruz.
        // İleride User entity'sine 'roles' alanı ekleyebiliriz.
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER")); // Varsayılan olarak her kullanıcıya USER rolü veriyoruz

        // Spring Security'nin kendi User objesini döndür
        // Bu User objesi, Spring Security'nin kimlik doğrulama sürecinde kullanacağı UserDetails implementasyonudur.
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(), // Kullanıcı adı
                user.getPassword(), // Hashlenmiş şifre
                authorities         // Kullanıcının yetkileri/rolleri
        );
    }

    // NOT: İleride kullanıcıların rolleri (örn. ADMIN, USER) User entity'sine eklendiğinde,
    // bu metot, kullanıcının gerçek rollerini veritabanından çekip GrantedAuthority listesine ekleyecektir.
    // Örneğin: user.getRoles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
}