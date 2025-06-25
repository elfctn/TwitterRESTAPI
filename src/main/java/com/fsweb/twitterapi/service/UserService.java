package com.fsweb.twitterapi.service;

import com.fsweb.twitterapi.entity.User; // Kullanıcı entity'si
import com.fsweb.twitterapi.repository.UserRepository; // UserRepository'yi enjekte edeceğiz
import com.fsweb.twitterapi.exception.ResourceNotFoundException; // Kaynak bulunamadığında fırlatılacak istisna
import com.fsweb.twitterapi.exception.CustomValidationException; // İş kuralı validasyonları için istisna (örn. kullanıcı adı/e-posta zaten var)
import com.fsweb.twitterapi.dto.user.UserRegisterRequest; // Kullanıcı kayıt isteği DTO'su
import com.fsweb.twitterapi.dto.user.UserResponse; // Kullanıcı yanıt DTO'su
import com.fsweb.twitterapi.dto.user.UserUpdateRequest; // Kullanıcı güncelleme isteği DTO'su
import com.fsweb.twitterapi.dto.user.UserLoginRequest; // YENİ: Kullanıcı giriş isteği DTO'su
import com.fsweb.twitterapi.dto.auth.JwtResponse; // YENİ: JWT yanıt DTO'su
import com.fsweb.twitterapi.security.jwt.JwtProvider; // YENİ: JWT Sağlayıcımızı import ediyoruz

import org.springframework.security.authentication.AuthenticationManager; // YENİ: Kimlik doğrulama yöneticisi
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // YENİ: Kimlik doğrulama tokenı
import org.springframework.security.core.Authentication; // YENİ: Kimlik doğrulama objesi
import org.springframework.security.core.context.SecurityContextHolder; // YENİ: Güvenlik bağlamını yönetmek için
import org.springframework.security.crypto.password.PasswordEncoder; // Şifreleme için PasswordEncoder'ı import ediyoruz
import org.springframework.stereotype.Service; // Bu sınıfın bir Spring Service bileşeni olduğunu belirtmek için
import org.springframework.transaction.annotation.Transactional; // İşlemleri (transaction) yönetmek için

import java.util.UUID; // UUID tipi için
import java.util.Optional; // Optional kullanmak için

@Service // Bu anotasyon, Spring'e bu sınıfın bir servis bileşeni olduğunu ve otomatik olarak yönetilmesi gerektiğini belirtir.
public class UserService {

    private final UserRepository userRepository; // UserRepository'yi enjekte ediyoruz (veri erişimi için)
    private final PasswordEncoder passwordEncoder; // PasswordEncoder'ı enjekte ediyoruz (şifre hashleme için)
    private final AuthenticationManager authenticationManager; // YENİ EKLENDİ: Spring Security'nin kimlik doğrulama yöneticisi
    private final JwtProvider jwtProvider; // YENİ EKLENDİ: JWT tokenları oluşturma ve doğrulama için

    // Constructor Injection: Tüm bağımlılıkları Spring tarafından enjekte ediyoruz.
    // Bu, bağımlılık enjeksiyonunun önerilen yoludur.
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtProvider = jwtProvider;
    }

    /**
     * Yeni bir kullanıcıyı kaydeder.
     * Kullanıcı adı veya e-posta zaten varsa CustomValidationException fırlatır.
     * Şifreyi kaydetmeden önce hashler.
     *
     * @param request Kullanıcı kayıt bilgileri içeren DTO
     * @return Kaydedilen kullanıcının UserResponse DTO'su
     */
    @Transactional // Bu metot bir veritabanı işlemi başlatır. İşlem başarılı olursa commit, hata olursa rollback olur.
    public UserResponse registerUser(UserRegisterRequest request) {
        // 1. Kullanıcı adı veya e-posta zaten var mı kontrol et
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new CustomValidationException("Username '" + request.getUsername() + "' already exists.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomValidationException("Email '" + request.getEmail() + "' already exists.");
        }

        // 2. UserRegisterRequest DTO'sundan User entity'si oluştur
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // Şifreyi hash'le ve kaydet
                .name(request.getName())
                .surname(request.getSurname())
                .bio(request.getBio())
                .profileImageUrl(request.getProfileImageUrl())
                .build();

        // 3. Kullanıcıyı veritabanına kaydet
        User savedUser = userRepository.save(user);

        // 4. Kaydedilen User entity'sinden UserResponse DTO'su oluştur ve döndür
        return mapUserToUserResponse(savedUser);
    }

    /**
     * Kullanıcının kimlik bilgilerini doğrulayarak giriş yapmasını sağlar ve bir JWT token döndürür.
     * Bu metot, `/login` endpoint'i tarafından çağrılacak.
     *
     * @param request Kullanıcı giriş bilgileri içeren DTO (usernameOrEmail ve password)
     * @return Başarılı giriş durumunda JWT token ve kullanıcı detayları içeren JwtResponse DTO'su
     */
    public JwtResponse loginUser(UserLoginRequest request) {
        // 1. Kullanıcı adı ve şifreyi içeren bir kimlik doğrulama tokenı oluştur.
        // AuthenticationManager, bu tokenı CustomUserDetailsService ve PasswordEncoder kullanarak doğrulayacak.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(), // Kullanıcı adı veya e-posta
                        request.getPassword()        // Şifre
                )
        );

        // 2. Kimlik doğrulama başarılı olursa, Authentication objesini Spring Security ContextHolder'ına set et.
        // Bu, Spring Security'nin mevcut isteğin geri kalanında (örneğin Controller'da) kullanıcının kimliğini tanımasını sağlar.
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Kimlik doğrulaması yapılmış Authentication objesinden JWT tokenı oluştur.
        String jwt = jwtProvider.generateJwtToken(authentication);

        // 4. Kimlik doğrulama yapılmış kullanıcının detaylarını al.
        // Authentication objesi içindeki principal (ana kimlik) UserDetails implementasyonudur.
        // Bu UserDetails içindeki kullanıcı adını kullanarak veritabanından User entity'sini çekiyoruz
        // çünkü JwtResponse için User entity'sindeki id, email gibi diğer bilgilere ihtiyacımız var.
        String username = jwtProvider.getUserNameFromJwtToken(jwt); // JWT'den kullanıcı adını al
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username/email", username));

        // 5. JWT tokenı ve kullanıcı bilgilerini içeren JwtResponse DTO'sunu döndür.
        return JwtResponse.builder()
                .token(jwt)
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    /**
     * Belirli bir ID'ye sahip kullanıcıyı getirir.
     * Kullanıcı bulunamazsa ResourceNotFoundException fırlatır.
     *
     * @param id Getirilecek kullanıcının UUID ID'si
     * @return Bulunan kullanıcının UserResponse DTO'su
     */
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapUserToUserResponse(user);
    }

    /**
     * Belirli bir ID'ye sahip kullanıcının bilgilerini günceller.
     * Kullanıcı bulunamazsa ResourceNotFoundException fırlatır.
     * Yalnızca request'te gelen dolu alanları günceller (null veya boş olmayanlar).
     *
     * @param id Güncellenecek kullanıcının UUID ID'si
     * @param request Kullanıcı güncelleme bilgileri içeren DTO
     * @return Güncellenen kullanıcının UserResponse DTO'su
     */
    @Transactional
    public UserResponse updateUser(UUID id, UserUpdateRequest request) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Sadece DTO'da gelen (null veya boş olmayan) alanları güncelle
        Optional.ofNullable(request.getName()).filter(s -> !s.isEmpty()).ifPresent(existingUser::setName);
        Optional.ofNullable(request.getSurname()).filter(s -> !s.isEmpty()).ifPresent(existingUser::setSurname);
        Optional.ofNullable(request.getBio()).filter(s -> !s.isEmpty()).ifPresent(existingUser::setBio);
        Optional.ofNullable(request.getProfileImageUrl()).filter(s -> !s.isEmpty()).ifPresent(existingUser::setProfileImageUrl);

        User updatedUser = userRepository.save(existingUser);
        return mapUserToUserResponse(updatedUser);
    }

    /**
     * Belirli bir ID'ye sahip kullanıcıyı siler.
     * Kullanıcı bulunamazsa ResourceNotFoundException fırlatır.
     *
     * @param id Silinecek kullanıcının UUID ID'si
     */
    @Transactional
    public void deleteUser(UUID id) {
        // Silmeden önce kullanıcının varlığını kontrol et
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        userRepository.deleteById(id);
    }

    // --- Yardımcı Metotlar ---

    /**
     * User entity'sinden UserResponse DTO'suna dönüşüm yapar.
     *
     * @param user Dönüştürülecek User entity'si
     * @return UserResponse DTO'su
     */
    private UserResponse mapUserToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .surname(user.getSurname())
                .bio(user.getBio())
                .profileImageUrl(user.getProfileImageUrl())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
