package com.fsweb.twitterapi.controller;

import com.fsweb.twitterapi.service.RetweetService; // RetweetService'i enjekte edeceğiz
import com.fsweb.twitterapi.entity.Retweet; // Retweet entity'sini import ediyoruz (response olarak entity dönecek)

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
@RequestMapping("/retweets") // Bu Controller'daki tüm endpoint'lerin "/retweets" yoluyla başlayacağını belirtir.
public class RetweetController {

    private final RetweetService retweetService; // RetweetService'i enjekte ediyoruz

    // Constructor Injection
    public RetweetController(RetweetService retweetService) {
        this.retweetService = retweetService;
    }

    /**
     * Belirli bir tweeti retweet eder.
     * Endpoint: POST /retweets/{originalTweetId}
     * Erişim: Kimliği doğrulanmış kullanıcılar.
     *
     * @param originalTweetId Retweet edilecek orijinal tweet'in ID'si (URL'den gelir)
     * @return Oluşturulan Retweet entity'si ve HTTP 201 Created durumu
     */
    @PostMapping("/{originalTweetId}") // POST isteği için "/retweets/{originalTweetId}" yolunu eşler
    @PreAuthorize("isAuthenticated()") // Bu metoda erişim için kullanıcının kimlik doğrulamasının yapılmış olması gerekir.
    public ResponseEntity<Retweet> retweetTweet(@PathVariable UUID originalTweetId) {
        // Güvenlik bağlamından mevcut kullanıcının ID'sini alıyoruz.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID currentUserId = UUID.fromString(authentication.getName()); // authenticated.getName() genellikle username/email verir

        Retweet newRetweet = retweetService.retweetTweet(originalTweetId, currentUserId); // RetweetService'i çağırarak tweeti retweet et
        return new ResponseEntity<>(newRetweet, HttpStatus.CREATED); // HTTP 201 Created durum kodu ile yanıt dön
    }

    /**
     * Retweet edilmiş bir tweeti kaldırır (unretweet).
     * Endpoint: DELETE /retweets/{originalTweetId}
     * Erişim: Sadece retweeti atan kullanıcı kaldırabilir.
     *
     * @param originalTweetId Retweeti kaldırılacak orijinal tweet'in ID'si (URL'den gelir)
     * @return HTTP 204 No Content durumu (başarılı silme)
     */
    @DeleteMapping("/{originalTweetId}") // DELETE isteği için "/retweets/{originalTweetId}" yolunu eşler
    @PreAuthorize("isAuthenticated()") // Kimlik doğrulama gerektirir
    public ResponseEntity<Void> unretweetTweet(@PathVariable UUID originalTweetId) {
        // Güvenlik bağlamından mevcut kullanıcının ID'sini alıyoruz.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID currentUserId = UUID.fromString(authentication.getName());

        retweetService.unretweetTweet(originalTweetId, currentUserId); // RetweetService'i çağırarak retweeti kaldır
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // HTTP 204 No Content durum kodu ile yanıt dön
    }

    /**
     * Belirli bir orijinal tweete ait tüm retweetleri getirir.
     * Endpoint: GET /retweets/original/{originalTweetId}
     * Erişim: Kimliği doğrulanmış herkes.
     *
     * @param originalTweetId Retweetleri getirilecek orijinal tweet'in ID'si
     * @return Retweetlerin List<Retweet> objesi ve HTTP 200 OK durumu
     */
    @GetMapping("/original/{originalTweetId}") // GET isteği için "/retweets/original/{originalTweetId}" yolunu eşler
    @PreAuthorize("isAuthenticated()") // Kimlik doğrulama gerektirir
    public ResponseEntity<List<Retweet>> getRetweetsForOriginalTweet(@PathVariable UUID originalTweetId) {
        List<Retweet> retweets = retweetService.getRetweetsForOriginalTweet(originalTweetId); // RetweetService'i çağırarak retweetleri getir
        return ResponseEntity.ok(retweets); // HTTP 200 OK durum kodu ile yanıt dön
    }
}
