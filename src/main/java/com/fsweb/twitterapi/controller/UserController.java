package com.fsweb.twitterapi.controller;

import com.fsweb.twitterapi.dto.user.UserResponse; // Kullanıcı yanıt DTO'su
import com.fsweb.twitterapi.dto.user.UserUpdateRequest; // Kullanıcı güncelleme isteği DTO'su
import com.fsweb.twitterapi.service.UserService; // UserService'i enjekte edeceğiz
import com.fsweb.twitterapi.exception.UnauthorizedException; // Yetkilendirme hatası için

import jakarta.validation.Valid; // Validasyon için
import org.springframework.http.HttpStatus; // HTTP durum kodları için
import org.springframework.http.ResponseEntity; // HTTP yanıtı oluşturmak için
import org.springframework.security.access.prepost.PreAuthorize; // Metot bazlı yetkilendirme için
import org.springframework.security.core.Authentication; // Mevcut kimlik doğrulama objesi için
import org.springframework.security.core.context.SecurityContextHolder; // Güvenlik bağlamından kullanıcı bilgilerini almak için
import org.springframework.web.bind.annotation.DeleteMapping; // DELETE isteklerini karşılamak için
import org.springframework.web.bind.annotation.GetMapping; // GET isteklerini karşılamak için
import org.springframework.web.bind.annotation.PathVariable; // URL yol değişkenlerini almak için
import org.springframework.web.bind.annotation.PutMapping; // PUT isteklerini karşılamak için
import org.springframework.web.bind.annotation.RequestBody; // HTTP isteğinin body'sini Java objesine dönüştürmek için
import org.springframework.web.bind.annotation.RequestMapping; // İstekleri belirli bir URL yoluna eşlemek için
import org.springframework.web.bind.annotation.RestController; // RESTful Controller olduğunu belirtmek için

import java.util.UUID; // UUID tipi için

@RestController // Bu sınıfın bir REST Controller olduğunu belirtir.
@RequestMapping("/users") // Bu Controller'daki tüm endpoint'lerin "/users" yoluyla başlayacağını belirtir.
public class UserController {

    private final UserService userService; // UserService'i enjekte ediyoruz

    // Constructor Injection
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Belirli bir kullanıcıyı ID'sine göre getirir.
     * Endpoint: GET /users/{id}
     * Erişim: Kimliği doğrulanmış herkes.
     *
     * @param id Getirilecek kullanıcının UUID ID'si
     * @return Bulunan kullanıcının UserResponse DTO'su ve HTTP 200 OK durumu
     */
    @GetMapping("/{id}") // GET isteği için "/users/{id}" yolunu eşler
    @PreAuthorize("isAuthenticated()") // Bu metoda erişim için kullanıcının kimlik doğrulamasının yapılmış olması gerekir.
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        // @PathVariable: URL yolundaki "{id}" kısmını UUID id parametresine eşler.
        UserResponse user = userService.getUserById(id); // UserService'i çağırarak kullanıcıyı getir
        return ResponseEntity.ok(user); // HTTP 200 OK durum kodu ile yanıt dön
    }

    /**
     * Mevcut kullanıcının profil bilgilerini günceller.
     * Endpoint: PUT /users/{id}
     * Erişim: Sadece kendi profilini güncelleyebilir.
     *
     * @param id Güncellenecek kullanıcının UUID ID'si (URL'den gelir)
     * @param request Kullanıcı güncelleme bilgileri içeren UserUpdateRequest DTO
     * @return Güncellenen kullanıcının UserResponse DTO'su ve HTTP 200 OK durumu
     */
    @PutMapping("/{id}") // PUT isteği için "/users/{id}" yolunu eşler
    @PreAuthorize("isAuthenticated()") // Kimlik doğrulama gerektirir
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID id,
                                                   @Valid @RequestBody UserUpdateRequest request) {
        // Güvenlik Kontrolü: İşlemi yapan kullanıcının kendi profilini güncellediğinden emin ol.
        // Spring Security context'inden mevcut kullanıcının ID'sini alıyoruz.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID currentUserId = UUID.fromString(authentication.getName()); // authenticated.getName() genellikle username/email verir

        if (!currentUserId.equals(id)) {
            // Eğer URL'deki ID ile işlemi yapan kullanıcının ID'si uyuşmuyorsa
            throw new UnauthorizedException("You are not authorized to update this user's profile.");
        }

        UserResponse updatedUser = userService.updateUser(id, request); // UserService'i çağırarak kullanıcıyı güncelle
        return ResponseEntity.ok(updatedUser); // HTTP 200 OK durum kodu ile yanıt dön
    }

    /**
     * Belirli bir kullanıcıyı siler.
     * Endpoint: DELETE /users/{id}
     * Erişim: Sadece kendi profilini silebilir. (Admin rolü de eklenebilir ileride)
     *
     * @param id Silinecek kullanıcının UUID ID'si
     * @return HTTP 204 No Content durumu (başarılı silme)
     */
    @DeleteMapping("/{id}") // DELETE isteği için "/users/{id}" yolunu eşler
    @PreAuthorize("isAuthenticated()") // Kimlik doğrulama gerektirir
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        // Güvenlik Kontrolü: İşlemi yapan kullanıcının kendi profilini sildiğinden emin ol.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID currentUserId = UUID.fromString(authentication.getName());

        if (!currentUserId.equals(id)) {
            // Eğer URL'deki ID ile işlemi yapan kullanıcının ID'si uyuşmuyorsa
            throw new UnauthorizedException("You are not authorized to delete this user's profile.");
        }

        userService.deleteUser(id); // UserService'i çağırarak kullanıcıyı sil
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // HTTP 204 No Content durum kodu ile yanıt dön (Başarılı silme, yanıt body'si yok)
    }
}
