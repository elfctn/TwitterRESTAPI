package com.fsweb.twitterapi.controller;

import com.fsweb.twitterapi.dto.tweet.TweetCreateRequest; // Tweet oluşturma isteği DTO'su
import com.fsweb.twitterapi.dto.tweet.TweetUpdateRequest; // Tweet güncelleme isteği DTO'su
import com.fsweb.twitterapi.dto.tweet.TweetResponse; // Tweet yanıt DTO'su
import com.fsweb.twitterapi.service.TweetService; // TweetService'i enjekte edeceğiz

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
import org.springframework.web.bind.annotation.PutMapping; // PUT isteklerini karşılamak için
import org.springframework.web.bind.annotation.RequestBody; // HTTP isteğinin body'sini Java objesine dönüştürmek için
import org.springframework.web.bind.annotation.RequestMapping; // İstekleri belirli bir URL yoluna eşlemek için
import org.springframework.web.bind.annotation.RequestParam; // URL sorgu parametrelerini almak için
import org.springframework.web.bind.annotation.RestController; // RESTful Controller olduğunu belirtmek için

import java.util.List; // List tipini kullanmak için
import java.util.UUID; // UUID tipi için

@RestController // Bu sınıfın bir REST Controller olduğunu belirtir.
@RequestMapping("/tweets") // Bu Controller'daki tüm endpoint'lerin "/tweets" yoluyla başlayacağını belirtir.
public class TweetController {

    private final TweetService tweetService; // TweetService'i enjekte ediyoruz

    // Constructor Injection
    public TweetController(TweetService tweetService) {
        this.tweetService = tweetService;
    }

    /**
     * Yeni bir tweet oluşturur.
     * Endpoint: POST /tweets
     * Erişim: Kimliği doğrulanmış kullanıcılar.
     *
     * @param request Yeni tweet bilgileri içeren TweetCreateRequest DTO
     * @return Oluşturulan tweet'in TweetResponse DTO'su ve HTTP 201 Created durumu
     */
    @PostMapping // POST isteği için "/tweets" yolunu eşler
    @PreAuthorize("isAuthenticated()") // Bu metoda erişim için kullanıcının kimlik doğrulamasının yapılmış olması gerekir.
    public ResponseEntity<TweetResponse> createTweet(@Valid @RequestBody TweetCreateRequest request) {
        // Güvenlik bağlamından mevcut kullanıcının ID'sini alıyoruz.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID currentUserId = UUID.fromString(authentication.getName()); // authenticated.getName() genellikle username/email verir

        TweetResponse newTweet = tweetService.createTweet(request, currentUserId); // TweetService'i çağırarak tweet oluştur
        return new ResponseEntity<>(newTweet, HttpStatus.CREATED); // HTTP 201 Created durum kodu ile yanıt dön
    }

    /**
     * Belirli bir tweet'i ID'sine göre getirir.
     * Endpoint: GET /tweets/{id}
     * Erişim: Kimliği doğrulanmış herkes.
     *
     * @param id Getirilecek tweet'in UUID ID'si
     * @return Bulunan tweet'in TweetResponse DTO'su ve HTTP 200 OK durumu
     */
    @GetMapping("/{id}") // GET isteği için "/tweets/{id}" yolunu eşler
    @PreAuthorize("isAuthenticated()") // Kimlik doğrulama gerektirir
    public ResponseEntity<TweetResponse> getTweetById(@PathVariable UUID id) {
        TweetResponse tweet = tweetService.getTweetById(id); // TweetService'i çağırarak tweet'i getir
        return ResponseEntity.ok(tweet); // HTTP 200 OK durum kodu ile yanıt dön
    }

    /**
     * Bir kullanıcının tüm tweetlerini getirir.
     * Endpoint: GET /tweets/user/{userId} (Proje gereksinimindeki findByUserId)
     * Erişim: Kimliği doğrulanmış herkes.
     *
     * @param userId Tweetleri getirilecek kullanıcının UUID ID'si
     * @return Kullanıcının tweetlerinin List<TweetResponse> DTO'su ve HTTP 200 OK durumu
     */
    @GetMapping("/user/{userId}") // GET isteği için "/tweets/user/{userId}" yolunu eşler
    @PreAuthorize("isAuthenticated()") // Kimlik doğrulama gerektirir
    public ResponseEntity<List<TweetResponse>> getTweetsByUserId(@PathVariable UUID userId) {
        List<TweetResponse> tweets = tweetService.getTweetsByUserId(userId); // TweetService'i çağırarak tweetleri getir
        return ResponseEntity.ok(tweets); // HTTP 200 OK durum kodu ile yanıt dön
    }

    /**
     * Belirli bir tweet'i ID'sine göre günceller.
     * Endpoint: PUT /tweets/{id}
     * Erişim: Sadece tweet sahibi güncelleyebilir.
     *
     * @param id Güncellenecek tweet'in UUID ID'si
     * @param request Tweet güncelleme bilgileri içeren TweetUpdateRequest DTO
     * @return Güncellenen tweet'in TweetResponse DTO'su ve HTTP 200 OK durumu
     */
    @PutMapping("/{id}") // PUT isteği için "/tweets/{id}" yolunu eşler
    @PreAuthorize("isAuthenticated()") // Kimlik doğrulama gerektirir
    public ResponseEntity<TweetResponse> updateTweet(@PathVariable UUID id,
                                                     @Valid @RequestBody TweetUpdateRequest request) {
        // Güvenlik bağlamından mevcut kullanıcının ID'sini alıyoruz.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID currentUserId = UUID.fromString(authentication.getName());

        TweetResponse updatedTweet = tweetService.updateTweet(id, request, currentUserId); // TweetService'i çağırarak tweet'i güncelle
        return ResponseEntity.ok(updatedTweet); // HTTP 200 OK durum kodu ile yanıt dön
    }

    /**
     * Belirli bir tweet'i ID'sine göre siler.
     * Endpoint: DELETE /tweets/{id}
     * Erişim: Sadece tweet sahibi silebilir.
     *
     * @param id Silinecek tweet'in UUID ID'si
     * @return HTTP 204 No Content durumu (başarılı silme)
     */
    @DeleteMapping("/{id}") // DELETE isteği için "/tweets/{id}" yolunu eşler
    @PreAuthorize("isAuthenticated()") // Kimlik doğrulama gerektirir
    public ResponseEntity<Void> deleteTweet(@PathVariable UUID id) {
        // Güvenlik bağlamından mevcut kullanıcının ID'sini alıyoruz.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID currentUserId = UUID.fromString(authentication.getName());

        tweetService.deleteTweet(id, currentUserId); // TweetService'i çağırarak tweet'i sil
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // HTTP 204 No Content durum kodu ile yanıt dön
    }
}
