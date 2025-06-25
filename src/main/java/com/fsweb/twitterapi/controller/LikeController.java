package com.fsweb.twitterapi.controller;

import com.fsweb.twitterapi.service.LikeService; // LikeService'i enjekte edeceğiz
import com.fsweb.twitterapi.entity.Like; // Like entity'sini import ediyoruz (response olarak entity dönecek)

import jakarta.validation.Valid; // Validasyon için
import org.springframework.http.HttpStatus; // HTTP durum kodları için
import org.springframework.http.ResponseEntity; // HTTP yanıtı oluşturmak için
import org.springframework.security.access.prepost.PreAuthorize; // Metot bazlı yetkilendirme için
import org.springframework.security.core.Authentication; // Mevcut kimlik doğrulama objesi için
import org.springframework.security.core.context.SecurityContextHolder; // Güvenlik bağlamından kullanıcı bilgilerini almak için
import org.springframework.web.bind.annotation.DeleteMapping; // DELETE isteklerini karşılamak için
import org.springframework.web.bind.annotation.GetMapping; // GET isteklerini karşılamak için
import org.springframework.web.bind.annotation.PathVariable; // URL yol değişkenlerini almak için
import org.springframework.web.bind.annotation.PostMapping; // POST isteklerini karşılamak için
import org.springframework.web.bind.annotation.RequestMapping; // İstekleri belirli bir URL yoluna eşlemek için
import org.springframework.web.bind.annotation.RestController; // RESTful Controller olduğunu belirtmek için

import java.util.List; // List tipini kullanmak için
import java.util.UUID; // UUID tipi için

@RestController // Bu sınıfın bir REST Controller olduğunu belirtir.
@RequestMapping("/likes") // Bu Controller'daki tüm endpoint'lerin "/likes" yoluyla başlayacağını belirtir.
public class LikeController {

    private final LikeService likeService; // LikeService'i enjekte ediyoruz

    // Constructor Injection
    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    /**
     * Belirli bir tweete beğeni (like) atar.
     * Endpoint: POST /likes/{tweetId}
     * Erişim: Kimliği doğrulanmış kullanıcılar.
     *
     * @param tweetId Beğeni atılacak tweet'in ID'si (URL'den gelir)
     * @return Oluşturulan Like entity'si ve HTTP 201 Created durumu
     */
    @PostMapping("/{tweetId}") // POST isteği için "/likes/{tweetId}" yolunu eşler
    @PreAuthorize("isAuthenticated()") // Bu metoda erişim için kullanıcının kimlik doğrulamasının yapılmış olması gerekir.
    public ResponseEntity<Like> likeTweet(@PathVariable UUID tweetId) {
        // Güvenlik bağlamından mevcut kullanıcının ID'sini alıyoruz.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID currentUserId = UUID.fromString(authentication.getName()); // authenticated.getName() genellikle username/email verir

        Like newLike = likeService.likeTweet(tweetId, currentUserId); // LikeService'i çağırarak tweet'i beğen
        return new ResponseEntity<>(newLike, HttpStatus.CREATED); // HTTP 201 Created durum kodu ile yanıt dön
    }

    /**
     * Belirli bir tweete atılan beğeniyi kaldırır (dislike).
     * Endpoint: DELETE /likes/{tweetId}
     * Erişim: Sadece beğeniyi atan kullanıcı kaldırabilir.
     *
     * @param tweetId Beğenisi kaldırılacak tweet'in ID'si (URL'den gelir)
     * @return HTTP 204 No Content durumu (başarılı silme)
     */
    @DeleteMapping("/{tweetId}") // DELETE isteği için "/likes/{tweetId}" yolunu eşler
    @PreAuthorize("isAuthenticated()") // Kimlik doğrulama gerektirir
    public ResponseEntity<Void> dislikeTweet(@PathVariable UUID tweetId) {
        // Güvenlik bağlamından mevcut kullanıcının ID'sini alıyoruz.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID currentUserId = UUID.fromString(authentication.getName());

        likeService.dislikeTweet(tweetId, currentUserId); // LikeService'i çağırarak beğeniyi kaldır
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // HTTP 204 No Content durum kodu ile yanıt dön
    }

    /**
     * Belirli bir tweete ait tüm beğenileri getirir.
     * Endpoint: GET /likes/tweet/{tweetId}
     * Erişim: Kimliği doğrulanmış herkes.
     *
     * @param tweetId Beğenileri getirilecek tweet'in ID'si
     * @return Beğenilerin List<Like> objesi ve HTTP 200 OK durumu
     */
    @GetMapping("/tweet/{tweetId}") // GET isteği için "/likes/tweet/{tweetId}" yolunu eşler
    @PreAuthorize("isAuthenticated()") // Kimlik doğrulama gerektirir
    public ResponseEntity<List<Like>> getLikesForTweet(@PathVariable UUID tweetId) {
        List<Like> likes = likeService.getLikesForTweet(tweetId); // LikeService'i çağırarak beğenileri getir
        return ResponseEntity.ok(likes); // HTTP 200 OK durum kodu ile yanıt dön
    }
}
