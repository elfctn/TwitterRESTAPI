package com.fsweb.twitterapi.service;

import org.springframework.stereotype.Service; // @Service anotasyonu için

@Service // Bu sınıfın bir Spring Service bileşeni olduğunu belirtir. Spring'in bağımlılık enjeksiyonu için önemlidir.
public class UserService {
    // Kullanıcılarla ilgili iş mantığı buraya gelecek.
    // Örneğin:
    // private final UserRepository userRepository; // UserRepository'yi enjekte edeceğiz
    // private final PasswordEncoder passwordEncoder; // Şifreleme için
    // private final JwtProvider jwtProvider; // JWT için

    // public User registerUser(UserRegisterRequest request) { ... }
    // public User loginUser(UserLoginRequest request) { ... }
    // public User getUserById(UUID id) { ... }
    // public User updateUser(UUID id, UserUpdateRequest request) { ... }
    // public void deleteUser(UUID id) { ... }
}