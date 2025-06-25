package com.fsweb.twitterapi; // Paket adı com.fsweb.twitterapi.test olarak güncellendi.

import com.fsweb.twitterapi.entity.User; // User entity'sini import ediyoruz
import com.fsweb.twitterapi.exception.CustomValidationException; // Özel validasyon istisnamızı import ediyoruz
import com.fsweb.twitterapi.exception.ResourceNotFoundException; // Kaynak bulunamadı istisnamızı import ediyoruz
import com.fsweb.twitterapi.repository.UserRepository; // UserRepository'yi mock'layacağız
import com.fsweb.twitterapi.service.UserService; // Test edeceğimiz Service sınıfı
import com.fsweb.twitterapi.dto.user.UserRegisterRequest; // DTO'larımızı import ediyoruz
import com.fsweb.twitterapi.dto.user.UserResponse;
import com.fsweb.twitterapi.dto.user.UserUpdateRequest;

import org.junit.jupiter.api.BeforeEach; // Her test metodundan önce çalışacak kurulum için
import org.junit.jupiter.api.DisplayName; // Test metodlarına daha okunabilir isimler vermek için
import org.junit.jupiter.api.Test; // Bir test metodu olduğunu belirtmek için
import org.junit.jupiter.api.extension.ExtendWith; // Mockito'yu JUnit 5 ile entegre etmek için
import org.mockito.InjectMocks; // Mock'lanmış bağımlılıkları enjekte etmek için
import org.mockito.Mock; // Sahte (mock) bağımlılıklar oluşturmak için
import org.mockito.junit.jupiter.MockitoExtension; // Mockito uzantısı için
import org.springframework.security.crypto.password.PasswordEncoder; // PasswordEncoder'ı mock'layacağız

import java.time.LocalDateTime; // Tarih ve saat tipi için
import java.util.Optional; // Optional kullanmak için
import java.util.UUID; // UUID tipi için

import static org.junit.jupiter.api.Assertions.*; // JUnit assert metotları için (assertEquals, assertThrows vb.)
import static org.mockito.ArgumentMatchers.any; // Herhangi bir argümanı eşleştirmek için
import static org.mockito.Mockito.*; // Mockito metotları için (when, verify vb.)

@ExtendWith(MockitoExtension.class) // JUnit 5'i Mockito ile entegre eder. Bu, @Mock ve @InjectMocks anotasyonlarının çalışmasını sağlar.
@DisplayName("UserService Unit Tests") // Test sınıfına okunabilir bir isim verir
public class UserServiceTest {

    @Mock // UserRepository'nin sahte (mock) bir versiyonunu oluşturur. Gerçek veritabanına erişmez.
    private UserRepository userRepository;

    @Mock // PasswordEncoder'ın sahte (mock) bir versiyonunu oluşturur. Şifrelemeyi simüle eder.
    private PasswordEncoder passwordEncoder;

    // AuthenticationManager ve JwtProvider burada mock'lanmadı çünkü registerUser, getUserById, updateUser, deleteUser
    // metodları bu test sınıfında doğrudan onları kullanmıyor. Login metodu için ayrıca test yazılır.
    // @Mock
    // private AuthenticationManager authenticationManager;
    // @Mock
    // private JwtProvider jwtProvider;

    @InjectMocks
    // Test edilecek gerçek UserService nesnesini oluşturur ve içine @Mock ile işaretlenmiş bağımlılıkları enjekte eder.
    private UserService userService;

    // Testlerde kullanılacak örnek veri
    private User testUser;
    private UserRegisterRequest registerRequest;
    private UserUpdateRequest updateRequest;
    private UUID testUserId;

    @BeforeEach
        // Her test metodundan önce çalışacak kurulum metodu. Ortak test verilerini ve mock davranışlarını burada ayarlarız.
    void setUp() {
        testUserId = UUID.randomUUID(); // Rastgele bir kullanıcı ID'si oluştur
        testUser = User.builder()
                .id(testUserId)
                .username("testuser")
                .email("test@example.com")
                .password("encodedpassword") // Hashlenmiş şifre varsayımı
                .name("Test")
                .surname("User")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        registerRequest = UserRegisterRequest.builder()
                .username("newuser")
                .email("new@example.com")
                .password("password123")
                .build();

        updateRequest = UserUpdateRequest.builder()
                .name("UpdatedName")
                .bio("New bio content")
                .build();
    }

    // --- registerUser Metodu Testleri ---

    @Test // Bu metodun bir JUnit test metodu olduğunu belirtir.
    @DisplayName("should register user successfully when username and email are unique")
        // Testin amacını açıklayan okunabilir isim
    void shouldRegisterUserSuccessfully() {
        // Mock davranışı tanımla: userRepository.existsByUsername ve existsByEmail çağrıldığında false dönsün (benzersizler)
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        // Mock davranışı tanımla: passwordEncoder.encode çağrıldığında "encodedpassword" dönsün
        when(passwordEncoder.encode(anyString())).thenReturn("encodedpassword");
        // Mock davranışı tanımla: userRepository.save çağrıldığında setUp'da tanımladığımız testUser'ı dönsün
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Metodu çağır ve sonucu al
        UserResponse result = userService.registerUser(registerRequest);

        // Sonuçları doğrula (Assert)
        assertNotNull(result); // Sonucun null olmadığını kontrol et
        assertEquals(testUser.getId(), result.getId()); // ID'lerin eşleştiğini kontrol et
        assertEquals(testUser.getUsername(), result.getUsername()); // Kullanıcı adlarının eşleştiğini kontrol et
        assertEquals(testUser.getEmail(), result.getEmail()); // E-postaların eşleştiğini kontrol et

        // Mock metodlarının doğru argümanlarla ve doğru sayıda çağrıldığını doğrula (Verify)
        verify(userRepository, times(1)).existsByUsername(registerRequest.getUsername());
        verify(userRepository, times(1)).existsByEmail(registerRequest.getEmail());
        verify(passwordEncoder, times(1)).encode(registerRequest.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("should throw CustomValidationException when username already exists")
    void shouldThrowExceptionWhenUsernameExists() {
        // Mock davranışı: username zaten var
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // Metodu çağırırken beklenen istisnanın fırlatıldığını doğrula
        CustomValidationException exception = assertThrows(CustomValidationException.class, () -> {
            userService.registerUser(registerRequest);
        });

        // Hata mesajını doğrula
        assertTrue(exception.getMessage().contains("Username '" + registerRequest.getUsername() + "' already exists."));

        // Mock metodlarının doğru çağrıldığını doğrula
        verify(userRepository, times(1)).existsByUsername(registerRequest.getUsername());
        // existsByEmail ve encode çağrılmamalı çünkü ilk kontrolde hata fırlatıldı
        verify(userRepository, never()).existsByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("should throw CustomValidationException when email already exists")
    void shouldThrowExceptionWhenEmailExists() {
        // Mock davranışı: username benzersiz ama email zaten var
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        CustomValidationException exception = assertThrows(CustomValidationException.class, () -> {
            userService.registerUser(registerRequest);
        });

        assertTrue(exception.getMessage().contains("Email '" + registerRequest.getEmail() + "' already exists."));

        verify(userRepository, times(1)).existsByUsername(registerRequest.getUsername());
        verify(userRepository, times(1)).existsByEmail(registerRequest.getEmail());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    // --- getUserById Metodu Testleri ---

    @Test
    @DisplayName("should return user by id successfully")
    void shouldReturnUserByIdSuccessfully() {
        // Mock davranışı: userRepository.findById çağrıldığında testUser'ı içeren Optional dönsün
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        UserResponse result = userService.getUserById(testUserId);

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());

        verify(userRepository, times(1)).findById(testUserId);
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when user not found by id")
    void shouldThrowExceptionWhenUserNotFoundById() {
        // Mock davranışı: userRepository.findById çağrıldığında boş Optional dönsün
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(UUID.randomUUID()); // Rastgele bir ID ile arama yap
        });

        assertTrue(exception.getMessage().contains("User not found with id"));

        verify(userRepository, times(1)).findById(any(UUID.class));
    }

    // --- updateUser Metodu Testleri ---

    @Test
    @DisplayName("should update user successfully")
    void shouldUpdateUserSuccessfully() {
        // Mock davranışı: findById mevcut kullanıcıyı dönsün
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        // Mock davranışı: save çağrıldığında güncellenmiş kullanıcıyı dönsün
        when(userRepository.save(any(User.class))).thenReturn(testUser); // Gerçekte burada güncellenmiş bir User döneriz

        UserResponse result = userService.updateUser(testUserId, updateRequest);

        assertNotNull(result);
        assertEquals(updateRequest.getName(), result.getName());
        assertEquals(updateRequest.getBio(), result.getBio());

        // verify(userRepository, times(1)).findById(testUserId); // Zaten setup'da var
        // verify(userRepository, times(1)).save(any(User.class)); // Zaten setup'da var
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when updating non-existent user")
    void shouldThrowExceptionWhenUpdatingNonExistentUser() {
        // Mock davranışı: findById boş dönsün
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser(UUID.randomUUID(), updateRequest);
        });

        assertTrue(exception.getMessage().contains("User not found with id"));

        verify(userRepository, times(1)).findById(any(UUID.class));
        verify(userRepository, never()).save(any(User.class)); // Hata fırlatıldığı için save çağrılmamalı
    }

    // --- deleteUser Metodu Testleri ---

    @Test
    @DisplayName("should delete user successfully")
    void shouldDeleteUserSuccessfully() {
        // Mock davranışı: existsById true dönsün (kullanıcı var)
        when(userRepository.existsById(testUserId)).thenReturn(true);
        // deleteById'nin çağrıldığını doğrulamak için özel bir when'e gerek yok, verify yeterli

        // Metodu çağır
        userService.deleteUser(testUserId);

        // deleteById metodunun çağrıldığını doğrula
        verify(userRepository, times(1)).existsById(testUserId);
        verify(userRepository, times(1)).deleteById(testUserId);
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when deleting non-existent user")
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        // Mock davranışı: existsById false dönsün (kullanıcı yok)
        when(userRepository.existsById(any(UUID.class))).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.deleteUser(UUID.randomUUID());
        });

        assertTrue(exception.getMessage().contains("User not found with id"));

        verify(userRepository, times(1)).existsById(any(UUID.class));
        verify(userRepository, never()).deleteById(any(UUID.class)); // Hata fırlatıldığı için deleteById çağrılmamalı
    }
}
