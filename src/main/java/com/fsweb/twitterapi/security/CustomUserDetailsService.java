package com.fsweb.twitterapi.security;

import com.fsweb.twitterapi.entity.User;
import com.fsweb.twitterapi.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID; // UUID import'u


@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Spring Security tarafından kimlik doğrulama sürecinde kullanılır (örneğin AuthController'daki login).
     * Kullanıcının username/email'i ile UserDetails yükler.
     *
     * @param usernameOrEmail Kullanıcı adı veya e-posta
     * @return Kendi UserPrincipal objemiz (UserDetails implementasyonu)
     * @throws UsernameNotFoundException Kullanıcı bulunamazsa fırlatılır
     */
    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail));

        return UserPrincipal.create(user);
    }

    /**
     * JWT filtre tarafından çağrılacak YENİ metot. Kullanıcının ID'si ile UserDetails yükler.
     * Bu metot, JWT tokenının subject'inde kullanıcının UUID'si olduğu durumda çağrılır.
     *
     * @param userId Kullanıcının UUID'si
     * @return Kendi UserPrincipal objemiz
     * @throws UsernameNotFoundException Kullanıcı bulunamazsa fırlatılır (Spring Security hatası)
     */
    public UserDetails loadUserById(UUID userId) throws UsernameNotFoundException { // YENİ METOT İMZASI
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));
        return UserPrincipal.create(user);
    }
}